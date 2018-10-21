package qa.eclipse.plugin.bundles.common;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public final class PreferencesUtil {

	/** split pattern */
	public static final String BY_COMMA_AND_TRIM = "\\s*,\\s*";

	private PreferencesUtil() {
		// utility class
	}

	/**
	 * @param key
	 * 
	 * @return a new array containing zero or more jar paths
	 */
	public static String[] loadCustomJarPaths(IEclipsePreferences preferences, String key) {
		final String customJarPathsValue = preferences.get(key, "");
		if (customJarPathsValue.trim().isEmpty()) {
			return new String[0];
		}

		String[] customRulesJarPaths = customJarPathsValue.split(BY_COMMA_AND_TRIM);

		// FileUtil.checkFilesExist("Jar file with custom rules", eclipseProjectPath,
		// customRulesJars);
		// customModuleJarPaths = FileUtil.filePathsToUrls(eclipseProjectPath,
		// customRulesJars);

		return customRulesJarPaths;
	}
}
