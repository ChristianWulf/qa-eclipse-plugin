package qa.eclipse.plugin.bundles.checkstyle.preference;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class CheckstylePreferences {

	/** split pattern */
	static final String BY_COMMA_AND_TRIM = "\\s*,\\s*";

	public static final CheckstylePreferences INSTANCE = new CheckstylePreferences("qa.eclipse.plugin.checkstyle");

	public static final String PROP_KEY_ENABLED = "enabled";
	public static final String PROP_KEY_CONFIG_FILE_PATH = "configFilePath";
	public static final String INVALID_CONFIG_FILE_PATH = "invalid/config/file/path";
	public static final String PROP_KEY_CUSTOM_MODULES_JAR_PATHS = "customModulesJarPaths";

	private final Map<IProject, IScopeContext> projectScopeByProject = new HashMap<>();

	private final String node;

	private CheckstylePreferences(String node) {
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
			preferences.addPreferenceChangeListener(new CheckstylePreferenceChangeListener(project));
			// updateRulsetCache(project, preferences);
		}

		return preferences;
	}

	/**
	 * @return a new array containing zero or more jar paths
	 */
	public String[] loadCustomModuleJarPaths(IEclipsePreferences preferences) {
		final String customModulesJarPathsValue = preferences.get(PROP_KEY_CUSTOM_MODULES_JAR_PATHS, "");
		String[] customRulesJarPaths = customModulesJarPathsValue.split(BY_COMMA_AND_TRIM);

		// FileUtil.checkFilesExist("Jar file with custom rules", eclipseProjectPath,
		// customRulesJars);
		// customModuleJarPaths = FileUtil.filePathsToUrls(eclipseProjectPath,
		// customRulesJars);

		return customRulesJarPaths;
	}

	/**
	 * @return the file path of the Checkstyle configuration file, or an empty
	 *         string.
	 */
	public String loadConfigFilePath(IEclipsePreferences preferences) {
		return preferences.get(PROP_KEY_CONFIG_FILE_PATH, INVALID_CONFIG_FILE_PATH);
	}

}
