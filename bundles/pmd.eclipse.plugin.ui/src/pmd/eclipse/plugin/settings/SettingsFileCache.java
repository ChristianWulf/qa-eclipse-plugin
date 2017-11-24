package pmd.eclipse.plugin.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;

import pmd.eclipse.plugin.experimental.PreferenceInitializer;

public class SettingsFileCache {

	// constants for the project-specific settings file
	private static final long NO_CACHED_TIMESTAMP = -1L;
	private static final String PROJECT_SETTINGS_FOLDER = ".settings";
	private static final String PMD_SETTINGS_FILE_NAME = "pmd.eclipse.plugin2.properties";
	private static final String PMD_DEFAULT_SETTINGS_FILE_NAME = "META-INF/pmd.eclipse.plugin.default.properties";

	private final Bundle bundle;
	// <IProject, ModificationStamp>
	private final Map<IProject, Long> modificationStamps = new HashMap<>();
	// <IProject, Properties>
	private final Map<IProject, Properties> propertiesObjects = new HashMap<>();

	public SettingsFileCache(Bundle bundle) {
		this.bundle = bundle;
	}

	public boolean isUpToDate(IProject eclipseProject) {
		final IFile settingsFileHandle = getSettingsFileHandle(eclipseProject);
		File settingsFile = settingsFileHandle.getRawLocation().toFile();

		// re-load settings file if it is not up-to-date (includes "not in cache")
		long lastModificationStamp = modificationStamps.getOrDefault(eclipseProject, NO_CACHED_TIMESTAMP);
		// Unlike IFile.getModificationStamp(), File.lastModified always returns the
		// actual, non-cached modification stamp
		long currentModificationStamp = settingsFile.lastModified();
		return (lastModificationStamp == currentModificationStamp);
	}

	public Properties getCachedProperties(IProject eclipseProject) {
		return propertiesObjects.get(eclipseProject);
	}

	public Properties load(IProject eclipseProject) throws FileNotFoundException {
		final IFile settingsFileHandle = getSettingsFileHandle(eclipseProject);
		File settingsFile = settingsFileHandle.getRawLocation().toFile();

		Properties properties = new Properties();
		try (InputStream is = settingsFileHandle.getContents(true)) {
			properties.load(is);
		} catch (CoreException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				throw (FileNotFoundException) e.getCause();
			}
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		propertiesObjects.put(eclipseProject, properties);
		// Unlike IFile.getModificationStamp(), File.lastModified always returns the
		// actual, non-cached modification stamp
		modificationStamps.put(eclipseProject, settingsFile.lastModified());

		return properties;
	}

	public void createDefaultPropertiesFile(IProject eclipseProject) {
		String defaultPropertiesFilePath = "/META-INF/pmd.eclipse.plugin.default.properties";
		URL defaultPropertiesFileUrl = getClass().getResource(defaultPropertiesFilePath);
		// URL defaultPropertiesFileUrl =
		// PmdUIPlugin.getDefault().getBundle().getEntry(defaultPropertiesFilePath);
		URI defaultPropertiesFileUri;
		try {
			defaultPropertiesFileUri = FileLocator.resolve(defaultPropertiesFileUrl).toURI();
		} catch (URISyntaxException | IOException e) {
			throw new IllegalStateException(e);
		}

		final IFile settingsFileHandle = getSettingsFileHandle(eclipseProject);
		String settingsFilePath = settingsFileHandle.getRawLocation().toString();

		try {
			Files.copy(Paths.get(defaultPropertiesFileUri), Paths.get(settingsFilePath));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private IFile getSettingsFileHandle(IProject eclipseProject) {
		IFolder settingsFolder = eclipseProject.getFolder(PROJECT_SETTINGS_FOLDER);
		if (!settingsFolder.exists()) {
			try {
				settingsFolder.create(false, true, null);
			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}
		}

		IFile settingsFileHandle = settingsFolder.getFile(PMD_SETTINGS_FILE_NAME);
		if (!settingsFileHandle.exists()) {
			// create project-specific settings file by using the default settings file from
			// the bundle
			URL defaultSettingsFileUrl = bundle.getEntry(PMD_DEFAULT_SETTINGS_FILE_NAME);

			IScopeContext projectScope = new ProjectScope(eclipseProject);
			IEclipsePreferences preferences = projectScope.getNode(PreferenceInitializer.PREFERENCE_NODE);
			preferences.putBoolean("works", true);
			try {
				preferences.flush();
			} catch (BackingStoreException e) {
				throw new IllegalStateException(e);
			}

			try (InputStream is = defaultSettingsFileUrl.openStream()) {
				settingsFileHandle.create(is, false, null);
			} catch (IOException | CoreException e) {
				throw new IllegalStateException(e);
			}
		}
		return settingsFileHandle;
	}
}
