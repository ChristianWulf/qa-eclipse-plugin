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
package qa.eclipse.plugin.bundles.checkstyle.tool;
// may not contain anything from the Eclipse API

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;
import qa.eclipse.plugin.bundles.common.EclipsePlatformUtil;
import qa.eclipse.plugin.bundles.common.FileUtils;
import qa.eclipse.plugin.bundles.common.MessagePopupUtils;
import qa.eclipse.plugin.bundles.common.PreferencesUtil;
import qa.eclipse.plugin.bundles.common.ProjectUtils;

/**
 *
 * @author Christian Wulf
 *
 */
public class CheckstyleTool {

	private final Checker checker;

	/**
	 * Setup checkstyle tool.
	 */
	public CheckstyleTool() {
		this.checker = new Checker();
	}

	/**
	 * You need to catch potential runtime exceptions if you call this method.
	 *
	 * @param eclipseFiles
	 *            collection of files
	 * @param checkstyleListener
	 *            listener for checkstyle
	 */
	public void startAsyncAnalysis(final List<IFile> eclipseFiles, final CheckstyleListener checkstyleListener) {
		final IFile file = eclipseFiles.get(0);
		final IProject project = file.getProject();

		this.checker.setBasedir(null);

		try {
			this.checker.setCharset(project.getDefaultCharset());
		} catch (final UnsupportedEncodingException | CoreException e) {
			MessagePopupUtils.displayError("Checkstyle Configuration Error", e.getLocalizedMessage());
		}

		final IEclipsePreferences projectPreferences = CheckstylePreferences.INSTANCE
				.getProjectScopedPreferences(project);
		final File eclipseProjectPath = ProjectUtils.getProjectPath(project);

		final Locale platformLocale = EclipsePlatformUtil.getLocale();
		this.checker.setLocaleLanguage(platformLocale.getLanguage());
		this.checker.setLocaleCountry(platformLocale.getCountry());

		final String configFilePath = CheckstylePreferences.INSTANCE.loadConfigFilePath(projectPreferences);
		final File configFile = FileUtils.makeAbsoluteFile(configFilePath, eclipseProjectPath);
		final String absoluteConfigFilePath = configFile.toString();

		/** Auto-set the config loc directory. */
		final Properties properties = new Properties();
		properties.put("config_loc", configFile.getAbsoluteFile().getParent());

		final PropertyResolver propertyResolver = new PropertiesExpander(properties);

		final IgnoredModulesOptions ignoredModulesOptions = IgnoredModulesOptions.OMIT;
		final ThreadModeSettings threadModeSettings = ThreadModeSettings.SINGLE_THREAD_MODE_INSTANCE;
		final Configuration configuration;
		try {
			configuration = ConfigurationLoader.loadConfiguration(absoluteConfigFilePath, propertyResolver,
					ignoredModulesOptions, threadModeSettings);

			final String[] customModuleJarPaths = PreferencesUtil.loadCustomJarPaths(projectPreferences,
					CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS);

			final URL[] moduleClassLoaderUrls = FileUtils.filePathsToUrls(eclipseProjectPath, customModuleJarPaths);

			try (URLClassLoader moduleClassLoader = new URLClassLoader(moduleClassLoaderUrls,
					this.getClass().getClassLoader())) {

				this.configureChecker(configuration, checkstyleListener, moduleClassLoader);

				this.runChecker(eclipseFiles);

			} catch (final IOException e) {
				throw new IllegalStateException(e);
			}

		} catch (final CheckstyleException e) {
			final String message = String.format("Could not load Checkstyle configuration from '%s'.",
					absoluteConfigFilePath);
			MessagePopupUtils.displayError("Checkstyle Configuration Error", message);
		}
	}

	private void runChecker(final List<IFile> eclipseFiles) {
		// https://github.com/checkstyle/eclipse-cs/blob/master/net.sf.eclipsecs.core/src/net/sf/eclipsecs/core/builder/CheckerFactory.java#L275

		final List<File> files = new ArrayList<>();

		for (final IFile eclipseFile : eclipseFiles) {
			final File sourceCodeFile = eclipseFile.getLocation().toFile().getAbsoluteFile();
			files.add(sourceCodeFile);
		}

		try {
			this.checker.process(files);
		} catch (final CheckstyleException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configureChecker(final Configuration configuration, final CheckstyleListener checkstyleListener, final URLClassLoader moduleClassLoader) {
		this.checker.setModuleClassLoader(moduleClassLoader);

		try {
			this.checker.configure(configuration);
		} catch (final CheckstyleException e) {
			throw new IllegalStateException(e);
		}

		this.checker.addListener(checkstyleListener);
		this.checker.addBeforeExecutionFileFilter(checkstyleListener);
	}

}
