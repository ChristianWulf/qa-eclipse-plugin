package pmd.eclipse.plugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class JavaUtil {

	private final Map<IProject, IJavaProject> javaProjectByIProject = new HashMap<>();

	public IPath getDefaultBuildOutputFolderPath(IProject project) throws JavaModelException {
		IJavaProject jProject = getAssociatedCachedJavaProject(project);
		// jProject.readOutputLocation()
		return jProject.getOutputLocation();
		// for (IClasspathEntry entry : jProject.getRawClasspath()) {
		// if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
		//
		// entry.getExclusionPatterns()
		// entry.getInclusionPatterns()
		// }
		// }
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
