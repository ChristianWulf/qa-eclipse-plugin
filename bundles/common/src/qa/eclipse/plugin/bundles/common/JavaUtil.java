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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 *
 * @author Christian Wulf
 *
 */
public class JavaUtil {

	private final Map<IProject, IJavaProject> javaProjectByIProject = new ConcurrentHashMap<>();

	/**
	 * Default constructor.
	 */
	public JavaUtil() {
		// nothing to do here
	}

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

		final IJavaProject jProject = this.getAssociatedCachedJavaProject(project);
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
		if (this.javaProjectByIProject.containsKey(project)) {
			return this.javaProjectByIProject.get(project);
		} else {
			return JavaCore.create(project);
		}
	}
}
