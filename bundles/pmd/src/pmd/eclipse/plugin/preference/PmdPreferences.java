package pmd.eclipse.plugin.preference;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
// architectural hints: do not use plugin-specific types to avoid a cascade of class compilings
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import net.sourceforge.pmd.RuleSets;
import qa.eclipse.plugin.bundles.common.FileUtil;
import qa.eclipse.plugin.bundles.common.ProjectUtil;

public class PmdPreferences {

	/** split pattern */
	static final String BY_COMMA_AND_TRIM = "\\s*,\\s*";

	public static final String INVALID_RULESET_FILE_PATH = "invalid/ruleset/file/path";

	public static final PmdPreferences INSTANCE = new PmdPreferences("qa.eclipse.plugin.pmd");

	public static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	public static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";
	public static final String PROP_KEY_ENABLED = "enabled";

	private final Map<IProject, IScopeContext> projectScopeByProject = new HashMap<>();
	private final RuleSetFileLoader ruleSetFileLoader = new RuleSetFileLoader();

	private final String node;

	private PmdPreferences(String node) {
		// private singleton constructor
		this.node = node;
	}

	public IEclipsePreferences getDefaultPreferences() {
		IEclipsePreferences preferences = DefaultScope.INSTANCE.getNode(node);
		return preferences;
	}

	public IEclipsePreferences getEclipseScopedPreferences() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(node);
		return preferences;
	}

	public IEclipsePreferences getEclipseEditorPreferences() {
		return InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
	}

	public synchronized IEclipsePreferences getProjectScopedPreferences(IProject project) {
		IEclipsePreferences preferences;

		IScopeContext projectPref;
		if (projectScopeByProject.containsKey(project)) {
			projectPref = projectScopeByProject.get(project);
			preferences = projectPref.getNode(node);
		} else {
			projectPref = new ProjectScope(project);
			projectScopeByProject.put(project, projectPref);

			preferences = projectPref.getNode(node);
			preferences.addPreferenceChangeListener(new PmdPreferenceChangeListener(project));
		}

		return preferences;
	}

	public RuleSets loadRuleSetFrom(IProject project) {
		IScopeContext projectPref = projectScopeByProject.get(project);
		IEclipsePreferences preferences = projectPref.getNode(node);
		File eclipseProjectPath = ProjectUtil.getProjectPath(project);
		RuleSets ruleSets = loadUpdatedRuleSet(preferences, eclipseProjectPath);
		return ruleSets;
	}

	private RuleSets loadUpdatedRuleSet(IEclipsePreferences preferences, File eclipseProjectPath) {
		URL[] urls;

		// load custom rules into a new class loader
		final String customRulesJarsValue = preferences.get(PROP_KEY_CUSTOM_RULES_JARS, "");
		if (customRulesJarsValue.trim().isEmpty()) {
			urls = new URL[0];
		} else {
			String[] customRulesJars = customRulesJarsValue.split(BY_COMMA_AND_TRIM);
			FileUtil.checkFilesExist("Jar file with custom rules", eclipseProjectPath, customRulesJars);
			urls = FileUtil.filePathsToUrls(eclipseProjectPath, customRulesJars);
		}

		URLClassLoader osgiClassLoaderWithCustomRules = new URLClassLoader(urls, getClass().getClassLoader());

		String ruleSetFilePathValue = preferences.get(PROP_KEY_RULE_SET_FILE_PATH, INVALID_RULESET_FILE_PATH);
		File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectPath);
		String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		return ruleSetFileLoader.load(ruleSetFilePath, osgiClassLoaderWithCustomRules);
	}

}
