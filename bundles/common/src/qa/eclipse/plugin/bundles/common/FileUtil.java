/***************************************************************************
 * Copyright (C) 2019
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

			URL fileUrl;
			try {
				fileUrl = jarFile.toURI().toURL();
			} catch (final MalformedURLException e) {
				// jarFile is filled by the user, so continue loop upon exception
				Logger.logThrowable("Cannot convert file to URL: " + jarFile, e);
				continue;
			}

			if (fileUrl.toString().endsWith("/")) {
				final String message = String.format("The passed jar file '%s' may not end with a slash ('/').",
						fileUrl.toString());
				throw new IllegalStateException(message);
			}

			urls[i] = fileUrl;
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
