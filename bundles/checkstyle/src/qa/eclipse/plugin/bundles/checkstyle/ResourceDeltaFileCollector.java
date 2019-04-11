/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
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
package qa.eclipse.plugin.bundles.checkstyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;

import qa.eclipse.plugin.bundles.common.JavaUtil;

/**
 * Represents an IResourceDeltaVisitor which collects all IFiles that have been
 * added or changed. Removed files and marker deltas are ignored. Hence, this
 * visitor focus on deltas at the file system, not within Eclipse.
 *
 * @author Christian Wulf
 *
 */
class ResourceDeltaFileCollector implements IResourceDeltaVisitor {

	private final Map<IProject, List<IFile>> addedFiles = new HashMap<>();
	// private final Map<IProject, List<IFile>> removedFiles = new HashMap<>();
	private final Map<IProject, List<IFile>> changedFiles = new HashMap<>();

	private final JavaUtil javaUtil = new JavaUtil();

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		switch (delta.getResource().getType()) {
		case IResource.PROJECT: {
			return this.shouldVisitChildren(delta.getResource());
		}
		case IResource.FILE: {
			this.addFileIfApplicable(delta);
			return false;
		}
		default: {
			return true;
		}
		}
	}

	private boolean shouldVisitChildren(final IResource resource) {
		if (!resource.isAccessible()) {
			return false;
		}
		// filter resources which are not located on the file system
		if (resource.getLocation() == null) {
			return false;
		}
		return true;
	}

	private void addFileIfApplicable(final IResourceDelta delta) throws JavaModelException {
		final IFile file = (IFile) delta.getResource();
		// TODO make configurable
		final boolean isHidden = file.isHidden();
		if (isHidden) {
			return;
		}

		// TODO make configurable
		final boolean isDerived = file.isDerived();
		if (isDerived) {
			return;
		}

		final IProject project = file.getProject();
		if (!project.isAccessible()) {
			return;
		}

		// TODO use exclude patterns instead to be independent of java in this class
		final Set<IPath> outputFolderPaths = this.javaUtil.getDefaultBuildOutputFolderPaths(project);
		for (final IPath outputFolderPath : outputFolderPaths) {
			if (outputFolderPath.isPrefixOf(file.getFullPath())) {
				return;
			}
		}

		switch (delta.getKind()) {
		case IResourceDelta.ADDED: {
			this.addFileToProjectMap(file, project, this.addedFiles);
			break;
		}
		// case IResourceDelta.REMOVED: {
		// addFileToProjectMap(file, project, removedFiles);
		// break;
		// }
		case IResourceDelta.CHANGED: {
			// Skip PMD analysis if only markers have been changed
			if (delta.getFlags() == IResourceDelta.MARKERS) {
				// if ((flags & IResourceDelta.MARKERS) == IResourceDelta.MARKERS) {
				break;
			}

			this.addFileToProjectMap(file, project, this.changedFiles);
			break;
		}
		default: {
			// ignore other kinds
		}
		}
	}

	private void addFileToProjectMap(final IFile file, final IProject project, final Map<IProject, List<IFile>> projectMap) {
		final List<IFile> files;
		if (projectMap.containsKey(project)) {
			files = projectMap.get(project);
		} else {
			files = new ArrayList<>();
			projectMap.put(project, files);
		}
		files.add(file);
	}

	public Map<IProject, List<IFile>> getAddedFiles() {
		return this.addedFiles;
	}

	public Map<IProject, List<IFile>> getChangedFiles() {
		return this.changedFiles;
	}

	// public Map<IProject, List<IFile>> getRemovedFiles() {
	// return removedFiles;
	// }

}
