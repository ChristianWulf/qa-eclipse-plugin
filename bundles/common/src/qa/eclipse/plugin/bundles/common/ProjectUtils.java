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

/**
 *
 * @author Christian Wulf
 *
 */
public final class ProjectUtils {

	private ProjectUtils() {
		// utility class
	}

	/**
	 * Get compiler code compliance for a project.
	 *
	 * @param project
	 *            project
	 * @return e.g., <code>JavaSE-1.8</code>
	 */
	public static String getCompilerCompliance(final IProject project) {
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				final IJavaProject javaProject = JavaCore.create(project);
				return javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			}
			throw new IllegalStateException("The project is not a Java project.");
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Get java io file for a given project.
	 *
	 * @param project
	 *            the project
	 * @return the file
	 */
	public static File getProjectPath(final IProject project) {
		final IPath location = project.getLocation(); // getRawLocation returns null
		return location.makeAbsolute().toFile();
	}

	/**
	 * Get absolute project path.
	 *
	 * @param propertyPage
	 *            property page which is related to the project
	 * @return the project path (java)
	 */
	public static Path getAbsoluteProjectPath(final PropertyPage propertyPage) {
		final IResource resource = propertyPage.getElement().getAdapter(IResource.class);
		final IProject project = resource.getProject();
		final File projectFile = ProjectUtils.getProjectPath(project);

		return Paths.get(projectFile.getAbsolutePath());
	}

}
