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

import qa.eclipse.plugin.bundles.checkstyle.EclipsePlatform;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;
import qa.eclipse.plugin.bundles.common.FileUtil;
import qa.eclipse.plugin.bundles.common.PreferencesUtil;
import qa.eclipse.plugin.bundles.common.ProjectUtil;

public class CheckstyleTool {

	private final Checker checker;

	public CheckstyleTool() {
		this.checker = new Checker();
	}

	/**
	 * You need to catch potential runtime exceptions if you call this method.
	 * 
	 * @param eclipseFiles
	 * @param checkstyleListener
	 */
	// FIXME remove Eclipse API
	public void startAsyncAnalysis(List<IFile> eclipseFiles, CheckstyleListener checkstyleListener) {
		IFile file = eclipseFiles.get(0);
		IProject project = file.getProject();

		checker.setBasedir(null);
		// checker.setCacheFile(fileName);

		try {
			checker.setCharset(project.getDefaultCharset());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

		IEclipsePreferences projectPreferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(project);
		File eclipseProjectPath = ProjectUtil.getProjectPath(project);

		Locale platformLocale = EclipsePlatform.getLocale();
		checker.setLocaleLanguage(platformLocale.getLanguage());
		checker.setLocaleCountry(platformLocale.getCountry());

		// ClassLoader classLoader2 = CommonUtils.class.getClassLoader();
		// URL emptyResourceName = CommonUtils.class.getResource("");
		// URL slashResourceName = CommonUtils.class.getResource("/");
		// URL relResourceName =
		// CommonUtils.class.getResource("config/cs-suppressions.xml");
		// URL absResourceName =
		// CommonUtils.class.getResource("/config/cs-suppressions.xml");
		// adds the Eclipse project's path to Checkstyle's class loader to find the file
		// of the SuppressFilter module
		// DOES NOT WORK since the class loader is not used to resolve the file path

		// Possibilities: pass URL, absolute file path, or class path file path

		// URL[] classLoaderUrls;
		// try {
		// classLoaderUrls = new URL[] { eclipseProjectPath.toURI().toURL() };
		// } catch (MalformedURLException e) {
		// throw new IllegalStateException(e);
		// }
		// ClassLoader classLoader = new URLClassLoader(classLoaderUrls,
		// Thread.currentThread().getContextClassLoader());
		// checker.setClassLoader(classLoader);

		String configFilePath = CheckstylePreferences.INSTANCE.loadConfigFilePath(projectPreferences);
		File configFile = FileUtil.makeAbsoluteFile(configFilePath, eclipseProjectPath);
		String absoluteConfigFilePath = configFile.toString();

		/** Auto-set the config loc directory. */
		Properties properties = new Properties();
		properties.put("config_loc", configFile.getAbsoluteFile().getParent());

		PropertyResolver propertyResolver = new PropertiesExpander(properties);

		IgnoredModulesOptions ignoredModulesOptions = IgnoredModulesOptions.OMIT;
		ThreadModeSettings threadModeSettings = ThreadModeSettings.SINGLE_THREAD_MODE_INSTANCE;
		Configuration configuration;
		try {
			configuration = ConfigurationLoader.loadConfiguration(absoluteConfigFilePath, propertyResolver,
					ignoredModulesOptions, threadModeSettings);
		} catch (CheckstyleException e) {
			String message = String.format("Could not load Checkstyle configuration from '%s'.",
					absoluteConfigFilePath);
			throw new IllegalStateException(message, e);
		}

		// Configuration suppressFilterConfiguration =
		// resolveSuppressFilterConfiguration(configuration);
		// if (suppressFilterConfiguration != null) {
		// try {
		// String filePath = suppressFilterConfiguration.getAttribute(CONFIG_PROP_FILE);
		//
		// } catch (CheckstyleException e) {
		// throw new IllegalStateException(e);
		// }
		// }

		String[] customModuleJarPaths = PreferencesUtil.loadCustomJarPaths(projectPreferences,
				CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS);

		URL[] moduleClassLoaderUrls = FileUtil.filePathsToUrls(eclipseProjectPath, customModuleJarPaths);
		try (URLClassLoader moduleClassLoader = new URLClassLoader(moduleClassLoaderUrls,
				getClass().getClassLoader())) {
			checker.setModuleClassLoader(moduleClassLoader);

			try {
				checker.configure(configuration);
			} catch (CheckstyleException e) {
				throw new IllegalStateException(e);
			}

			checker.addListener(checkstyleListener);
			checker.addBeforeExecutionFileFilter(checkstyleListener);

			// https://github.com/checkstyle/eclipse-cs/blob/master/net.sf.eclipsecs.core/src/net/sf/eclipsecs/core/builder/CheckerFactory.java#L275

			List<File> files = new ArrayList<>();

			for (IFile eclipseFile : eclipseFiles) {
				final File sourceCodeFile = eclipseFile.getLocation().toFile().getAbsoluteFile();
				files.add(sourceCodeFile);
			}

			try {
				checker.process(files);
			} catch (CheckstyleException e) {
				throw new IllegalStateException(e);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		// process each file separately to be able to skip it
		// for (File fileToCheck : files) {
		// for (IFile eclipseFile : eclipseFiles) {
		// final File sourceCodeFile =
		// eclipseFile.getLocation().toFile().getAbsoluteFile();
		//
		// List<File> filesToCheck = Arrays.asList(sourceCodeFile);
		// try {
		// checker.process(filesToCheck);
		// } catch (CheckstyleException e) {
		// if (e.getCause() instanceof OperationCanceledException) {
		// throw new IllegalStateException(e);
		// } else {
		// // skip file upon syntax error
		// try {
		// CheckstyleMarkers.appendProcessingErrorMarker(eclipseFile, e);
		// } catch (CoreException e1) {
		// // ignore if marker could not be created
		// }
		// }
		// }
		// }
	}

}
