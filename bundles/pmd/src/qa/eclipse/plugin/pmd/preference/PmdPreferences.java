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
package qa.eclipse.plugin.pmd.preference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import qa.eclipse.plugin.bundles.common.ConfigurationErrorException;
import qa.eclipse.plugin.bundles.common.FileUtil;
import qa.eclipse.plugin.bundles.common.ProjectUtil;

import net.sourceforge.pmd.RuleSets;

/**
 * Preferences for PMD.
 *
 * Note: architectural hints: do not use plugin-specific types to avoid a cascade of class compilings
 *
 * @author Christian Wulf
 *
 */
public final class PmdPreferences {

	public static final String INVALID_RULESET_FILE_PATH = "invalid/ruleset/file/path";

	public static final PmdPreferences INSTANCE = new PmdPreferences("qa.eclipse.plugin.pmd");

	public static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	public static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";
	public static final String PROP_KEY_ENABLED = "enabled";

	/** split pattern. */
	private static final String BY_COMMA_AND_TRIM = "\\s*,\\s*";

	private final Map<IProject, IScopeContext> projectScopeByProject = new HashMap<>();
	private final RuleSetFileLoader ruleSetFileLoader = new RuleSetFileLoader();

	private final String node;

	private URLClassLoader osgiClassLoaderWithCustomRules;

	private PmdPreferences(final String node) {
		// private singleton constructor
		this.node = node;
		this.osgiClassLoaderWithCustomRules = new URLClassLoader(new URL[0]); // NullObjectPattern
	}

	/**
	 * Get default preferences.
	 *
	 * @return return the default preferences
	 */
	public IEclipsePreferences getDefaultPreferences() {
		return DefaultScope.INSTANCE.getNode(this.node);
	}

	/**
	 * Get the eclipse wide preferences.
	 *
	 * @return returns the preferences
	 */
	public IEclipsePreferences getEclipseScopedPreferences() {
		return InstanceScope.INSTANCE.getNode(this.node);
	}

	public IEclipsePreferences getEclipseEditorPreferences() {
		return InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
	}

	/**
	 * Get the project related preferences.
	 *
	 * @param project
	 *            the project for the preferences
	 *
	 * @return returns the preferences
	 */
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
			preferences.addPreferenceChangeListener(new PmdPreferenceChangeListener(project));
		}

		return preferences;
	}

	/**
	 * Load all rulesets for a specific project.
	 *
	 * @param project
	 *            the project
	 * @return return the rulesets
	 * @throws ConfigurationErrorException
	 */
	public RuleSets loadRuleSetsFrom(final IProject project) throws ConfigurationErrorException {
		final IScopeContext projectPref = this.projectScopeByProject.get(project);
		final IEclipsePreferences preferences = projectPref.getNode(this.node);
		final File eclipseProjectPath = ProjectUtil.getProjectPath(project);
		final RuleSets ruleSets = this.loadUpdatedRuleSet(preferences, project, eclipseProjectPath);

		return ruleSets;
	}

	private RuleSets loadUpdatedRuleSet(final IEclipsePreferences preferences, final IProject project,
			final File eclipseProjectPath) throws ConfigurationErrorException {
		final URL[] urls;

		// load custom rules into a new class loader
		final String customRulesJarsValue = preferences.get(PmdPreferences.PROP_KEY_CUSTOM_RULES_JARS, "");
		if (customRulesJarsValue.trim().isEmpty()) {
			urls = new URL[0];
		} else {
			final String[] customRulesJars = customRulesJarsValue.split(PmdPreferences.BY_COMMA_AND_TRIM);
			FileUtil.checkFilesExist("Jar file with custom rules", eclipseProjectPath, customRulesJars);
			urls = FileUtil.filePathsToUrls(eclipseProjectPath, customRulesJars);
		}

		final ClassLoader parentClassLoader;
		// parentClassLoader = Thread.currentThread().getContextClassLoader();
		parentClassLoader = this.getClass().getClassLoader(); // equinox class loader with jars from the lib folder
		this.osgiClassLoaderWithCustomRules = new URLClassLoader(urls, parentClassLoader);

		final String ruleSetFilePathValue = preferences.get(PmdPreferences.PROP_KEY_RULE_SET_FILE_PATH,
				PmdPreferences.INVALID_RULESET_FILE_PATH);
		final File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectPath);
		final String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		return this.ruleSetFileLoader.load(ruleSetFilePath, project, this.osgiClassLoaderWithCustomRules);
	}

	/**
	 * Cleanup preferences view.
	 */
	public void close() {
		try {
			this.osgiClassLoaderWithCustomRules.close();
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
