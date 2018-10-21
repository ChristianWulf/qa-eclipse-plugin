package qa.eclipse.plugin.bundles.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

public final class ClassLoaderUtil {

	private ClassLoaderUtil() {
		// utility class
	}

	public static ClassLoader newClassLoader(URL[] urls, ClassLoader parentClassLoader) {
		URLClassLoader osgiClassLoaderWithUrls = new URLClassLoader(urls, parentClassLoader);
		return osgiClassLoaderWithUrls;
	}

	public static <T> T executeWithContextClassLoader(ClassLoader classLoader, Supplier<T> function) {
		Thread currentThread = Thread.currentThread();
		ClassLoader oldClassLoader = currentThread.getContextClassLoader();

		currentThread.setContextClassLoader(classLoader);
		try {
			return function.get();
		} finally {
			currentThread.setContextClassLoader(oldClassLoader);
		}
	}
}
