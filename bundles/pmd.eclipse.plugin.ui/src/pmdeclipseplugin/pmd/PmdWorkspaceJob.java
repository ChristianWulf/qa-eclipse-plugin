package pmdeclipseplugin.pmd;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
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
import net.sourceforge.pmd.util.datasource.FileDataSource;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmdeclipseplugin.PmdUIPlugin;
import pmdeclipseplugin.eclipse.FileUtil;
import pmdeclipseplugin.eclipse.ProjectUtil;
import pmdeclipseplugin.settings.RuleSetFileLoader;
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
	private final RuleSetFileLoader ruleSetFileLoader;
	private final Map<IProject, RuleSet> ruleSetCache = new HashMap<>();

	public PmdWorkspaceJob(String name, List<IFile> eclipseFiles) {
		super(name);
		this.eclipseFiles = eclipseFiles;
		Bundle bundle = PmdUIPlugin.getDefault().getBundle();
		this.settingsFileCache = new SettingsFileCache(bundle);
		this.ruleSetFileLoader = new RuleSetFileLoader();
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		final IResource someEclipseFile = eclipseFiles.get(0);
		final IProject eclipseProject = someEclipseFile.getProject();

		// load custom rules from settings file
		// but only if the settings file has changed since last loading
		if (!settingsFileCache.isUpToDate(eclipseProject)) {
			final Properties properties = settingsFileCache.load(eclipseProject);
			final File eclipseProjectFile = eclipseProject.getRawLocation().makeAbsolute().toFile();

			RuleSet ruleSet = loadUpdatedRuleSet(properties, eclipseProjectFile);

			ruleSetCache.put(eclipseProject, ruleSet);
		}

		final Map<String, IFile> eclipseFilesMap = new HashMap<>();

		// collect data sources
		List<DataSource> dataSources = new ArrayList<>();
		for (IFile eclipseFile : eclipseFiles) {
			final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();
			final DataSource dataSource = new FileDataSource(sourceCodeFile);
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
		final MonoThreadProcessor pmdProcessor = new MonoThreadProcessor(configuration);

		RuleSet ruleSet = ruleSetCache.get(eclipseProject);
		final RuleSetFactory ruleSetFactory = new ConstantRuleSetFactory(ruleSet);

		final RuleContext context = new RuleContext();

		Renderer progressRenderer = new ProgressRenderer(subMonitor);
		PmdProblemRenderer problemRenderer = new PmdProblemRenderer();
		final List<Renderer> collectingRenderers = Arrays.asList(progressRenderer, problemRenderer);

		pmdProcessor.processFiles(ruleSetFactory, dataSources, context, collectingRenderers);

		Report report = problemRenderer.getProblemReport();
		if (report.size() > 0) {
			for (RuleViolation violation : report.getViolationTree()) {
				String violationFilename = violation.getFilename();
				IFile eclipseFile = eclipseFilesMap.get(violationFilename);
				PmdMarkers.appendViolationMarker(eclipseFile, violation);
			}
		}

		return Status.OK_STATUS;
	}

	private RuleSet loadUpdatedRuleSet(Properties properties, File eclipseProjectFile) {
		final String customRulesJarsValue = properties.getProperty(PROP_KEY_CUSTOM_RULES_JARS);

		// load custom rules into a new class loader
		String[] customRulesJars = customRulesJarsValue.split(",");
		URL[] urls = FileUtil.filePathsToUrls(eclipseProjectFile, customRulesJars);
		URLClassLoader osgiClassLoaderWithCustomRules = new URLClassLoader(urls, getClass().getClassLoader());

		String ruleSetFilePathValue = properties.getProperty(PROP_KEY_RULE_SET_FILE_PATH);
		File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectFile);
		String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		try {
			return ruleSetFileLoader.load(ruleSetFilePath, osgiClassLoaderWithCustomRules);
		} catch (RuleSetNotFoundException e) {
			// RuleSetNotFoundException at this place means: fiel not found.
			// Since PMD does not work without any ruleset file, we stop the loop here.
			String message = String.format("Ruleset file not found on file path '%s'", ruleSetFilePathValue);
			PmdUIPlugin.getDefault().logException(message, e);
			throw new IllegalStateException(message, e);
		}
	}

}