package pmd.eclipse.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class JavaUtil {

	private final Map<IProject, IJavaProject> javaProjectByIProject = new HashMap<>();

	/**
	 * @param project
	 * @return a (possibly empty) read-only set of output folder paths
	 * @throws JavaModelException
	 */
	public Set<IPath> getDefaultBuildOutputFolderPaths(IProject project) throws JavaModelException {
		if (!project.isAccessible()) {
			return Collections.emptySet();
		}
		// "External Plug-In Libraries" are represented as an accessible project,
		// but do not exits on the file system.
		if (project.getRawLocation() == null) {
			return Collections.emptySet();
		}

		Set<IPath> outputFolderPaths = new HashSet<>();

		IJavaProject jProject = getAssociatedCachedJavaProject(project);
		for (IClasspathEntry entry : jProject.getRawClasspath()) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPath outputLocation = entry.getOutputLocation();
				if (outputLocation != null) {
					outputFolderPaths.add(outputLocation);
				}

				// entry.getExclusionPatterns()
				// entry.getInclusionPatterns()
			}
		}

		outputFolderPaths.add(jProject.getOutputLocation());
		// jProject.readOutputLocation()
		return outputFolderPaths;
	}

	private IJavaProject getAssociatedCachedJavaProject(IProject project) {
		IJavaProject jProject;
		if (javaProjectByIProject.containsKey(project)) {
			jProject = javaProjectByIProject.get(project);
		} else {
			jProject = JavaCore.create(project);
		}
		return jProject;
	}
}