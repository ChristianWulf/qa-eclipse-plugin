package pmd.eclipse.plugin.settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.Bundle;

public class SettingsFileCache {

	// constants for the project-specific settings file
	private static final long NO_CACHED_TIMESTAMP = 0L;
	private static final String PROJECT_SETTINGS_FOLDER = ".settings";
	private static final String PMD_SETTINGS_FILE_NAME = "pmd.eclipse.plugin.properties";
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

	public Properties load(IProject eclipseProject) {
		final IFile settingsFileHandle = getSettingsFileHandle(eclipseProject);
		File settingsFile = settingsFileHandle.getRawLocation().toFile();

		Properties properties = new Properties();
		try (InputStream is = settingsFileHandle.getContents(true)) {
			properties.load(is);
		} catch (IOException | CoreException e) {
			throw new IllegalStateException(e);
		}

		propertiesObjects.put(eclipseProject, properties);
		// Unlike IFile.getModificationStamp(), File.lastModified always returns the
		// actual, non-cached modification stamp
		modificationStamps.put(eclipseProject, settingsFile.lastModified());

		return properties;
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
			try (InputStream is = defaultSettingsFileUrl.openStream()) {
				settingsFileHandle.create(is, false, null);
			} catch (IOException | CoreException e) {
				throw new IllegalStateException(e);
			}
		}
		return settingsFileHandle;
	}
}
