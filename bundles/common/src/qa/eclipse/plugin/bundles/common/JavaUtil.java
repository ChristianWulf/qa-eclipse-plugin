package qa.eclipse.plugin.bundles.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
	public Set<IPath> getDefaultBuildOutputFolderPaths(final IProject project) throws JavaModelException {
		if (!project.isAccessible()) {
			return Collections.emptySet();
		}
		// filter projects which are not located on the file system
		if (project.getLocation() == null) {
			return Collections.emptySet();
		}
		// Return if the passed project is not a Java project,
		// because then getRawClasspath() and getOutputLocation() is not available.
		try {
			if (!project.hasNature(JavaCore.NATURE_ID)) {
				return Collections.emptySet();
			}
		} catch (final CoreException e) {
			return Collections.emptySet();
		}

		final Set<IPath> outputFolderPaths = new HashSet<>();

		final IJavaProject jProject = getAssociatedCachedJavaProject(project);
		for (final IClasspathEntry entry : jProject.getRawClasspath()) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				final IPath outputLocation = entry.getOutputLocation();
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

	private IJavaProject getAssociatedCachedJavaProject(final IProject project) {
		IJavaProject jProject;
		if (javaProjectByIProject.containsKey(project)) {
			jProject = javaProjectByIProject.get(project);
		} else {
			jProject = JavaCore.create(project);
		}
		return jProject;
	}
}
