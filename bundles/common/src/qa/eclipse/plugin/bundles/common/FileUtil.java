package qa.eclipse.plugin.bundles.common;
// architectural hint: may use eclipse packages

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class FileUtil {

	private FileUtil() {
		// utility class
	}

	public static File makeAbsoluteFile(final String absoluteOrRelativeFilePath, final File parentFile) {
		File customRulesJarFile = new File(absoluteOrRelativeFilePath);
		if (!customRulesJarFile.isAbsolute()) {
			customRulesJarFile = new File(parentFile, absoluteOrRelativeFilePath);
		}
		return customRulesJarFile;
	}

	public static URL[] filePathsToUrls(final File parentFile, final String[] jarFilePaths) {
		final URL[] urls = new URL[jarFilePaths.length];
		for (int i = 0; i < jarFilePaths.length; i++) {
			final File jarFile = FileUtil.makeAbsoluteFile(jarFilePaths[i], parentFile);
			try {
				urls[i] = jarFile.toURI().toURL();
			} catch (final MalformedURLException e) {
				// jarFile is filled by the user, so continue loop upon exception
				Logger.logThrowable("Cannot convert file to URL: " + jarFile, e);
			}
		}
		return urls;
	}

	public static void checkFilesExist(final String messagePrefix, final File parentFile, final String[] filePaths) {
		for (final String filePath : filePaths) {
			final File file = FileUtil.makeAbsoluteFile(filePath, parentFile);
			if (!file.exists()) {
				final String message = String.format("%s not found on file path '%s'.", messagePrefix, filePath);
				Logger.logWarning(message);
			}
		}
	}

}
