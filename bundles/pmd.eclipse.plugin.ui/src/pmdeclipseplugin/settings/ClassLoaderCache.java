package pmdeclipseplugin.settings;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class ClassLoaderCache {

	// <IProject, ClassLoader>
	private final Map<IProject, ClassLoader> classLoaders = new HashMap<>();

	public ClassLoader getClassLoader(IProject eclipseProject) {
		return classLoaders.get(eclipseProject);
	}

	public void putClassLoader(IProject eclipseProject, ClassLoader classLoader) {
		classLoaders.put(eclipseProject, classLoader);
	}
}
