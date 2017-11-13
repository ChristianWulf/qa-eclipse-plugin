package pmdeclipseplugin.pmd;

//architectural hint: may use eclipse packages
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import net.sourceforge.pmd.PMD;
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

public class PmdTool {

	private static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	private static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";

	// @Inject
	// private final UISynchronize sync;
	private final Map<File, Object> fileCache = new HashMap<>();

	private final SettingsFileCache settingsFileCache;
	private final ClassLoaderCache classLoaderCache;
	private final RuleSetCache ruleSetCache;

	public PmdTool() {
		Bundle bundle = PmdUIPlugin.getDefault().getBundle();
		settingsFileCache = new SettingsFileCache(bundle);
		classLoaderCache = new ClassLoaderCache();
		ruleSetCache = new RuleSetCache();
	}

	public void startAsyncAnalysis(List<IFile> eclipseFile) {

	}

	public void startAsyncAnalysis(IFile eclipseFile) {
		final IProject eclipseProject = eclipseFile.getProject();

		// load custom rules from settings file
		// but only if the settings file has changed since last loading
		if (!settingsFileCache.isUpToDate(eclipseProject)) {
			loadUpdatedRuleSet(eclipseProject);
		}

		Job job = new WorkspaceJob("Analysis by PMD") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();
				String taskName = "Analyzing " + sourceCodeFile.toString();

				SubMonitor subMonitor = SubMonitor.convert(monitor, taskName, 1);

				final RuleSet ruleSet = ruleSetCache.getCachedRuleSet(eclipseProject);
				if (!ruleSet.applies(sourceCodeFile)) {
					return Status.OK_STATUS;
				}

				// remove previous PMD markers
				eclipseFile.deleteMarkers(PmdMarkers.PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION, false, IResource.DEPTH_ZERO);

				String compilerCompliance = ProjectUtil.getCompilerCompliance(eclipseProject);
				PMDConfiguration configuration = new CustomPMDConfiguration(compilerCompliance);

				RuleSetFactory ruleSetFactory = new RuleSetFactory() {
					@Override
					public synchronized RuleSets createRuleSets(String referenceString)
							throws RuleSetNotFoundException {
						return new RuleSets(ruleSet);
					}
				};

				RuleContext context = PMD.newRuleContext(sourceCodeFile.getName(), sourceCodeFile);
				// context.setLanguageVersion(configuration.getla);

				PmdProblemRenderer problemRenderer = new PmdProblemRenderer();
				List<Renderer> collectingRenderers = Arrays.asList(problemRenderer);

				InputStreamReader input;
				try {
					input = new InputStreamReader(eclipseFile.getContents(), eclipseFile.getCharset());
				} catch (UnsupportedEncodingException e) {
					Status status = new Status(IStatus.ERROR, PmdUIPlugin.PLUGIN_ID,
							"Aborted PMD analysis of " + sourceCodeFile.toString(), e);
					throw new CoreException(status);
				}
				DataSource dataSource = new ReaderDataSource(input, eclipseFile.getName());
				List<DataSource> dataSources = Arrays.asList(dataSource);

				MonoThreadProcessor pmdProcessor = new MonoThreadProcessor(configuration);

				subMonitor.split(1);
				pmdProcessor.processFiles(ruleSetFactory, dataSources, context, collectingRenderers);

				Report report = problemRenderer.getProblemReport();
				if (report.size() > 0) {
					for (RuleViolation violation : report.getViolationTree()) {
						appendViolationMarker(eclipseFile, violation);
					}
				}

				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void appendViolationMarker(IFile eclipseFile, RuleViolation violation) throws CoreException {
		IMarker marker = eclipseFile.createMarker(PmdMarkers.PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION);
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

	public void getHighestPriorityForFile(File file) {
		Object object = fileCache.get(file);

		// TODO return PmdPriority instance
	}
}
