package pmdeclipseplugin.pmd;

import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.processor.MonoThreadProcessor;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.ReaderDataSource;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmdeclipseplugin.PmdUIPlugin;
import pmdeclipseplugin.eclipse.FileUtil;
import pmdeclipseplugin.eclipse.ProjectUtil;
import pmdeclipseplugin.settings.ClassLoaderCache;
import pmdeclipseplugin.settings.RuleSetCache;
import pmdeclipseplugin.settings.SettingsFileCache;

class PmdWorkspaceJob extends WorkspaceJob {

	private static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	private static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";

	private static class ConstantRuleSetFactory extends RuleSetFactory {
		private final RuleSet ruleSet;

		private ConstantRuleSetFactory(RuleSet ruleSet) {
			this.ruleSet = ruleSet;
		}

		@Override
		public synchronized RuleSets createRuleSets(String referenceString) throws RuleSetNotFoundException {
			return new RuleSets(ruleSet);
		}
	}

	// @Inject
	// private final UISynchronize sync;
	private final List<IFile> eclipseFiles;
	private final SettingsFileCache settingsFileCache;
	private final ClassLoaderCache classLoaderCache;
	private final RuleSetCache ruleSetCache;

	public PmdWorkspaceJob(String name, List<IFile> eclipseFiles) {
		super(name);
		this.eclipseFiles = eclipseFiles;
		Bundle bundle = PmdUIPlugin.getDefault().getBundle();
		this.settingsFileCache = new SettingsFileCache(bundle);
		this.classLoaderCache = new ClassLoaderCache();
		this.ruleSetCache = new RuleSetCache();
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		final IResource someEclipseFile = eclipseFiles.get(0);
		final IProject eclipseProject = someEclipseFile.getProject();

		// load custom rules from settings file
		// but only if the settings file has changed since last loading
		if (!settingsFileCache.isUpToDate(eclipseProject)) {
			loadUpdatedRuleSet(eclipseProject);
		}

		final Map<String, IFile> eclipseFilesMap = new HashMap<>();

		// collect data sources
		List<DataSource> dataSources = new ArrayList<>();
		for (IFile eclipseFile : eclipseFiles) {
			DataSource dataSource = convertToDataSource(eclipseFile);
			dataSources.add(dataSource);

			// map file name to eclipse file: necessary for adding markers at the end
			String niceFileName = dataSource.getNiceFileName(false, "");
			eclipseFilesMap.put(niceFileName, eclipseFile);

			// also remove previous PMD markers
			eclipseFile.deleteMarkers(PmdMarkers.PMD_VIOLATION_MARKER, false, IResource.DEPTH_ZERO);
		}

		String taskName = "Analyzing " + eclipseFiles.size() + " files...";
		final SubMonitor subMonitor = SubMonitor.convert(monitor, taskName, eclipseFiles.size());

		String compilerCompliance = ProjectUtil.getCompilerCompliance(eclipseProject);
		final PMDConfiguration configuration = new CustomPMDConfiguration(compilerCompliance);

		RuleSet ruleSet = ruleSetCache.getCachedRuleSet(eclipseProject);
		final RuleSetFactory ruleSetFactory = new ConstantRuleSetFactory(ruleSet);

		final RuleContext context = new RuleContext();

		Renderer progressRenderer = new ProgressRenderer(subMonitor);
		PmdProblemRenderer problemRenderer = new PmdProblemRenderer();
		final List<Renderer> collectingRenderers = Arrays.asList(progressRenderer, problemRenderer);

		final MonoThreadProcessor pmdProcessor = new MonoThreadProcessor(configuration);

		pmdProcessor.processFiles(ruleSetFactory, dataSources, context, collectingRenderers);

		Report report = problemRenderer.getProblemReport();
		if (report.size() > 0) {
			for (RuleViolation violation : report.getViolationTree()) {
				String violationFilename = violation.getFilename();
				IFile eclipseFile = eclipseFilesMap.get(violationFilename);
				appendViolationMarker(eclipseFile, violation);
			}
		}

		return Status.OK_STATUS;
	}

	private DataSource convertToDataSource(IFile eclipseFile) throws CoreException {
		final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();

		InputStreamReader input;
		try {
			input = new InputStreamReader(eclipseFile.getContents(), eclipseFile.getCharset());
		} catch (UnsupportedEncodingException e) {
			Status status = new Status(IStatus.ERROR, PmdUIPlugin.PLUGIN_ID,
					"Aborted PMD analysis of " + sourceCodeFile.toString(), e);
			throw new CoreException(status);
		}
		// TODO use FileDataSource instead
		DataSource dataSource = new ReaderDataSource(input, sourceCodeFile.getPath());
		return dataSource;
	}

	protected void appendViolationMarker(IFile eclipseFile, RuleViolation violation) throws CoreException {
		IMarker marker = eclipseFile.createMarker(PmdMarkers.PMD_VIOLATION_MARKER);
		marker.setAttribute(IMarker.MESSAGE, violation.getDescription());
		marker.setAttribute(PmdMarkers.ATTR_KEY_PRIORITY, violation.getRule().getPriority().getPriority());
		// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
		marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

	private void loadUpdatedRuleSet(final IProject eclipseProject) {
		final Properties properties = settingsFileCache.load(eclipseProject);
		final String customRulesJarsValue = properties.getProperty(PROP_KEY_CUSTOM_RULES_JARS);
		final File eclipseProjectFile = eclipseProject.getRawLocation().makeAbsolute().toFile();

		// load custom rules into a new class loader
		String[] customRulesJars = customRulesJarsValue.split(",");
		URL[] urls = filePathsToUrls(eclipseProjectFile, customRulesJars);
		URLClassLoader osgiClassLoaderWithCustomRules = new URLClassLoader(urls, getClass().getClassLoader());

		// assign the new class loader with the project
		classLoaderCache.putClassLoader(eclipseProject, osgiClassLoaderWithCustomRules);

		String ruleSetFilePathValue = properties.getProperty(PROP_KEY_RULE_SET_FILE_PATH);
		File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectFile);
		String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		try {
			ruleSetCache.load(eclipseProject, ruleSetFilePath, osgiClassLoaderWithCustomRules);
		} catch (RuleSetNotFoundException e) {
			// RuleSetNotFoundException at this place means: fiel not found.
			// Since PMD does not work without any ruleset file, we stop the loop here.
			// TODO log error to problems view
			String message = String.format("Ruleset file not found on file path '%s'", ruleSetFilePathValue);
			throw new IllegalStateException(message, e);
		}
	}

	private URL[] filePathsToUrls(final File parentFile, String[] customRulesJars) {
		URL[] urls = new URL[customRulesJars.length];
		for (int i = 0; i < customRulesJars.length; i++) {
			File customRulesJarFile = FileUtil.makeAbsoluteFile(customRulesJars[i], parentFile);
			try {
				urls[i] = customRulesJarFile.toURI().toURL();
			} catch (MalformedURLException e) {
				// customRulesJars is filled by the user, so continue loop upon exception
				// TODO log unknown jar path
			}
		}
		return urls;
	}
}