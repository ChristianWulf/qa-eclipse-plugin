/***************************************************************************
 * Copyright (C) 2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
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
	public static String[] loadCustomJarPaths(final IEclipsePreferences preferences, final String key) {
		final String customJarPathsValue = preferences.get(key, "");
		if (customJarPathsValue.trim().isEmpty()) {
			return new String[0];
		}

		final String[] customRulesJarPaths = customJarPathsValue.split(PreferencesUtil.BY_COMMA_AND_TRIM);

		// FileUtil.checkFilesExist("Jar file with custom rules", eclipseProjectPath,
		// customRulesJars);
		// customModuleJarPaths = FileUtil.filePathsToUrls(eclipseProjectPath,
		// customRulesJars);

		return customRulesJarPaths;
	}
}
