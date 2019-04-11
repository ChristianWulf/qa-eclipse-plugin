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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

public final class ClassLoaderUtil {

	private ClassLoaderUtil() {
		// utility class
	}

	public static URLClassLoader newClassLoader(final URL[] urls, final ClassLoader parentClassLoader) {
		final URLClassLoader osgiClassLoaderWithUrls = new URLClassLoader(urls, parentClassLoader);
		return osgiClassLoaderWithUrls;
	}

	public static <T> T executeWithContextClassLoader(final ClassLoader classLoader, final Supplier<T> function) {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader oldClassLoader = currentThread.getContextClassLoader();

		currentThread.setContextClassLoader(classLoader);
		try {
			return function.get();
		} finally {
			currentThread.setContextClassLoader(oldClassLoader);
		}
	}
}
