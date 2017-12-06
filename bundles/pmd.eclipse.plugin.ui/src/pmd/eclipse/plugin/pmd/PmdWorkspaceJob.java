package pmd.eclipse.plugin.pmd;

import java.io.File;
import java.io.FileNotFoundException;
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
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.processor.MonoThreadProcessor;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.eclipse.FileUtil;
import pmd.eclipse.plugin.eclipse.ProjectUtil;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.settings.RuleSetFileLoader;
import pmd.eclipse.plugin.settings.SettingsFileCache;

class PmdWorkspaceJob extends WorkspaceJob {

	private static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	private static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";

	private static class ConstantRuleSetFactory extends RuleSetFactory {
		private final RuleSets ruleSets;

		// ConstantRuleSetFactory(RuleSet ruleSet) {
		// this.ruleSets = new RuleSets(ruleSet);
		// }

		ConstantRuleSetFactory(RuleSets ruleSets) {
			this.ruleSets = ruleSets;
		}

		@Override
		public synchronized RuleSets createRuleSets(String referenceString) throws RuleSetNotFoundException {
			return ruleSets;
		}
	}

	// @Inject
	// private final UISynchronize sync;
	private final List<IFile> eclipseFiles;
	private final SettingsFileCache settingsFileCache;
	private final RuleSetFileLoader ruleSetFileLoader;
	private final Map<IProject, RuleSets> ruleSetCache = new HashMap<>();

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

		// IScopeContext projectScope = new ProjectScope(eclipseProject);
		// IEclipsePreferences preferences =
		// projectScope.getNode(PreferenceInitializer.PREFERENCE_NODE);

		// load custom rules from settings file
		// but only if the settings file has changed since last loading
		if (!settingsFileCache.isUpToDate(eclipseProject)) {
			Properties properties;
			try {
				properties = settingsFileCache.load(eclipseProject);
			} catch (FileNotFoundException e) {
				settingsFileCache.createDefaultPropertiesFile(eclipseProject);
				try {
					properties = settingsFileCache.load(eclipseProject);
				} catch (FileNotFoundException e1) {
					throw new IllegalStateException(e);
				}
			}
			final File eclipseProjectFile = eclipseProject.getRawLocation().makeAbsolute().toFile();

			RuleSets ruleSets = loadUpdatedRuleSet(properties, eclipseProjectFile);

			ruleSetCache.put(eclipseProject, ruleSets);
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

		RuleSets ruleSets = ruleSetCache.get(eclipseProject);
		final RuleSetFactory ruleSetFactory = new ConstantRuleSetFactory(ruleSets);

		final RuleContext context = new RuleContext();

		Renderer progressRenderer = new PmdProgressRenderer(subMonitor);
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

	private RuleSets loadUpdatedRuleSet(Properties properties, File eclipseProjectFile) {
		URL[] urls;

		// load custom rules into a new class loader
		final String customRulesJarsValue = properties.getProperty(PROP_KEY_CUSTOM_RULES_JARS);
		if (customRulesJarsValue.trim().isEmpty()) {
			urls = new URL[0];
		} else {
			String[] customRulesJars = customRulesJarsValue.split(",");
			urls = FileUtil.filePathsToUrls(eclipseProjectFile, customRulesJars);
		}

		URLClassLoader osgiClassLoaderWithCustomRules = new URLClassLoader(urls, getClass().getClassLoader());

		String ruleSetFilePathValue = properties.getProperty(PROP_KEY_RULE_SET_FILE_PATH);
		File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectFile);
		String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		return ruleSetFileLoader.load(ruleSetFilePath, osgiClassLoaderWithCustomRules);
	}

}