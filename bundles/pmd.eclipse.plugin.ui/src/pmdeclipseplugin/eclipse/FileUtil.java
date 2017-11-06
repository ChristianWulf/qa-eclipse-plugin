package pmdeclipseplugin.eclipse;
// architectural hint: may use eclipse packages

import java.io.File;
import java.net.URL;

import org.osgi.framework.Bundle;

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
}
