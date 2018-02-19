package qa.eclipse.plugin.bundles.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.dialogs.PropertyPage;

public final class ProjectUtil {

	private ProjectUtil() {
		// utility class
	}

	/**
	 * 
	 * @param project
	 * @return e.g., <code>JavaSE-1.8</code>
	 */
	public static String getCompilerCompliance(IProject project) {
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject javaProject = JavaCore.create(project);
				String compilerCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
				return compilerCompliance;
			}
			throw new IllegalStateException("The project is not a Java project.");
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
	}

	public static File getProjectPath(IProject project) {
		IPath location = project.getLocation(); // getRawLocation returns null
		return location.makeAbsolute().toFile();
	}

	public static Path getAbsoluteProjectPath(PropertyPage propertyPage) {
		IResource resource = propertyPage.getElement().getAdapter(IResource.class);
		IProject project = resource.getProject();
		File projectFile = ProjectUtil.getProjectPath(project);
		Path absoluteProjectPath = Paths.get(projectFile.getAbsolutePath());
		return absoluteProjectPath;
	}

}
