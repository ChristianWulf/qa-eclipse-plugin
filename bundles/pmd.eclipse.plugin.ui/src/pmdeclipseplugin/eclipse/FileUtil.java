package pmdeclipseplugin.eclipse;
// architectural hint: may use eclipse packages

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.framework.Bundle;

import pmdeclipseplugin.PmdUIPlugin;

public final class FileUtil {

	private FileUtil() {
		// utility class
	}

	public static URL getResourceFromClasspath(Bundle bundle, String resourceName) {
		return bundle.getResource(resourceName);
	}

	public static File makeAbsoluteFile(String absoluteOrRelativeFilePath, File parentFile) {
		File customRulesJarFile = new File(absoluteOrRelativeFilePath);
		if (!customRulesJarFile.isAbsolute()) {
			customRulesJarFile = new File(parentFile, absoluteOrRelativeFilePath);
		}
		return customRulesJarFile;
	}

	public static URL[] filePathsToUrls(final File parentFile, String[] jarFilePaths) {
		URL[] urls = new URL[jarFilePaths.length];
		for (int i = 0; i < jarFilePaths.length; i++) {
			File jarFile = FileUtil.makeAbsoluteFile(jarFilePaths[i], parentFile);
			try {
				urls[i] = jarFile.toURI().toURL();
			} catch (MalformedURLException e) {
				// jarFile is filled by the user, so continue loop upon exception
				PmdUIPlugin.getDefault().logException("Cannot convert file to URL: " + jarFile, e);
			}
		}
		return urls;
	}
}
