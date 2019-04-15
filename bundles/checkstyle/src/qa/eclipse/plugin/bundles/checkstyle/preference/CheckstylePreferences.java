/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
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
package qa.eclipse.plugin.bundles.checkstyle.preference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 *
 * @author Christian Wulf
 *
 */
public final class CheckstylePreferences {

	public static final CheckstylePreferences INSTANCE = new CheckstylePreferences("qa.eclipse.plugin.checkstyle");

	public static final String PROP_KEY_ENABLED = "enabled";
	public static final String PROP_KEY_CONFIG_FILE_PATH = "configFilePath";
	public static final String INVALID_CONFIG_FILE_PATH = "invalid/config/file/path";
	public static final String PROP_KEY_CUSTOM_MODULES_JAR_PATHS = "customModulesJarPaths";

	/** split pattern. */
	static final String BY_COMMA_AND_TRIM = "\\s*,\\s*";

	private final Map<IProject, IScopeContext> projectScopeByProject = new ConcurrentHashMap<>();

	private final String node;

	private CheckstylePreferences(final String node) {
		// private singleton constructor
		this.node = node;
	}

	public IEclipsePreferences getDefaultPreferences() {
		return DefaultScope.INSTANCE.getNode(this.node);
	}

	public IEclipsePreferences getEclipseScopedPreferences() {
		return InstanceScope.INSTANCE.getNode(this.node);
	}

	public IEclipsePreferences getEclipseEditorPreferences() {
		return InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
	}

	public synchronized IEclipsePreferences getProjectScopedPreferences(final IProject project) {
		final IEclipsePreferences preferences;

		final IScopeContext projectPref;
		if (this.projectScopeByProject.containsKey(project)) {
			projectPref = this.projectScopeByProject.get(project);
			preferences = projectPref.getNode(this.node);
		} else {
			projectPref = new ProjectScope(project);
			this.projectScopeByProject.put(project, projectPref);

			preferences = projectPref.getNode(this.node);
			preferences.addPreferenceChangeListener(new CheckstylePreferenceChangeListener(project));
			// updateRulsetCache(project, preferences);
		}

		return preferences;
	}

	/**
	 * @return a new array containing zero or more jar paths
	 */
	public String[] loadCustomModuleJarPaths(final IEclipsePreferences preferences) {
		final String customModulesJarPathsValue = preferences
				.get(CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS, "");
		final String[] customRulesJarPaths = customModulesJarPathsValue.split(CheckstylePreferences.BY_COMMA_AND_TRIM);

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
	public String loadConfigFilePath(final IEclipsePreferences preferences) {
		return preferences.get(CheckstylePreferences.PROP_KEY_CONFIG_FILE_PATH,
				CheckstylePreferences.INVALID_CONFIG_FILE_PATH);
	}

}
