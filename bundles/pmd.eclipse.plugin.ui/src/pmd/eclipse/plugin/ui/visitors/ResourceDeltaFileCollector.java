package pmd.eclipse.plugin.ui.visitors;

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

import pmd.eclipse.plugin.JavaUtil;

/**
 * Represents an IResourceDeltaVisitor which collects all IFiles that have been
 * added or changed. Removed files and marker deltas are ignored. Hence, this
 * visitor focus on deltas at the file system, not within Eclipse.
 * 
 * @author Christian Wulf
 *
 */
public class ResourceDeltaFileCollector implements IResourceDeltaVisitor {

	private final Map<IProject, List<IFile>> addedFiles = new HashMap<>();
	// private final Map<IProject, List<IFile>> removedFiles = new HashMap<>();
	private final Map<IProject, List<IFile>> changedFiles = new HashMap<>();

	private final JavaUtil javaUtil = new JavaUtil();

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		switch (delta.getResource().getType()) {
		case IResource.PROJECT: {
			return shouldVisitChildren(delta.getResource());
		}
		case IResource.FILE: {
			addFileIfApplicable(delta);
			return false;
		}
		default: {
			return true;
		}
		}
	}

	private boolean shouldVisitChildren(IResource resource) {
		if (!resource.isAccessible()) {
			return false;
		}
		// filter resources which are not located on the file system
		if (resource.getLocation() == null) {
			return false;
		}
		return true;
	}

	private void addFileIfApplicable(IResourceDelta delta) throws JavaModelException {
		IFile file = (IFile) delta.getResource();
		// TODO make configurable
		boolean isHidden = file.isHidden();
		if (isHidden) {
			return;
		}

		// TODO make configurable
		boolean isDerived = file.isDerived();
		if (isDerived) {
			return;
		}

		IProject project = file.getProject();
		if (!project.isAccessible()) {
			return;
		}

		// TODO use exclude patterns instead to be independent of java in this class
		Set<IPath> outputFolderPaths = javaUtil.getDefaultBuildOutputFolderPaths(project);
		for (IPath outputFolderPath : outputFolderPaths) {
			if (outputFolderPath.isPrefixOf(file.getFullPath())) {
				return;
			}
		}

		switch (delta.getKind()) {
		case IResourceDelta.ADDED: {
			addFileToProjectMap(file, project, addedFiles);
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

			addFileToProjectMap(file, project, changedFiles);
			break;
		}
		default: {
			// ignore other kinds
		}
		}
	}

	private void addFileToProjectMap(IFile file, IProject project, Map<IProject, List<IFile>> projectMap) {
		List<IFile> files;
		if (projectMap.containsKey(project)) {
			files = projectMap.get(project);
		} else {
			files = new ArrayList<>();
			projectMap.put(project, files);
		}
		files.add(file);
	}

	public Map<IProject, List<IFile>> getAddedFiles() {
		return addedFiles;
	}

	public Map<IProject, List<IFile>> getChangedFiles() {
		return changedFiles;
	}

	// public Map<IProject, List<IFile>> getRemovedFiles() {
	// return removedFiles;
	// }

}
