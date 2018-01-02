package pmd.eclipse.plugin.settings;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
// architectural hints: do not use plugin-specific types to avoid a cascade of class compilings
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import net.sourceforge.pmd.RuleSets;
import pmd.eclipse.plugin.eclipse.FileUtil;

public class PmdPreferences {

	public static final String INVALID_RULESET_FILE_PATH = "invalid/ruleset/file/path";

	public static final PmdPreferences INSTANCE = new PmdPreferences("qa.eclipse.plugin.pmd");

	public static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	public static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";
	public static final String PROP_KEY_ENABLED = "enabled";

	private static final IScopeContext INSTANCE_SCOPE = InstanceScope.INSTANCE;

	private final Map<IProject, IScopeContext> projectScopeByProject = new HashMap<>();
	private final RuleSetFileLoader ruleSetFileLoader = new RuleSetFileLoader();
	private final Map<IProject, RuleSets> ruleSetCache = new ConcurrentHashMap<>();

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
		IEclipsePreferences preferences = INSTANCE_SCOPE.getNode(node);
		return preferences;
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
			preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
				@Override
				public void preferenceChange(PreferenceChangeEvent event) {
					// event.getNewValue();
					// event.getOldValue();
					updateRulsetCache(project, preferences);
				}
			});
			updateRulsetCache(project, preferences);
		}

		return preferences;
	}

	private void updateRulsetCache(IProject project, IEclipsePreferences preferences) {
		File eclipseProjectPath = project.getRawLocation().makeAbsolute().toFile();
		RuleSets ruleSets = loadUpdatedRuleSet(preferences, eclipseProjectPath);

		// set or replace ruleset
		ruleSetCache.put(project, ruleSets);
	}

	public RuleSets getRuleSets(IProject eclipseProject) {
		return ruleSetCache.get(eclipseProject);
	}

	private RuleSets loadUpdatedRuleSet(IEclipsePreferences preferences, File eclipseProjectPath) {
		URL[] urls;

		// load custom rules into a new class loader
		final String customRulesJarsValue = preferences.get(PROP_KEY_CUSTOM_RULES_JARS, "");
		if (customRulesJarsValue.trim().isEmpty()) {
			urls = new URL[0];
		} else {
			String[] customRulesJars = customRulesJarsValue.split(",");
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
