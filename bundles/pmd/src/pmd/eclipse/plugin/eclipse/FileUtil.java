package pmd.eclipse.plugin.eclipse;
// architectural hint: may use eclipse packages

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import pmd.eclipse.plugin.PmdUIPlugin;

public final class FileUtil {

	private FileUtil() {
		// utility class
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
				PmdUIPlugin.getDefault().logThrowable("Cannot convert file to URL: " + jarFile, e);
			}
		}
		return urls;
	}

	public static void checkFilesExist(String messagePrefix, File parentFile, String[] filePaths) {
		for (String filePath : filePaths) {
			File file = FileUtil.makeAbsoluteFile(filePath, parentFile);
			if (!file.exists()) {
				String message = String.format("%s not found on file path '%s'.", messagePrefix, filePath);
				PmdUIPlugin.getDefault().logWarning(message);
			}
		}
	}
}
