package pmd.eclipse.plugin.ui.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

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
		if (delta.getResource() instanceof IFile) {
			IFile file = (IFile) delta.getResource();

			// ResourceWorkingSetFilter filter = new ResourceWorkingSetFilter();
			// filter.setWorkingSet(workingSet);
			// fileInWorkingSet = filter.select(null, null, file);

			IProject project = file.getProject();
			int flags = delta.getFlags();

			String fileMessageFormat = "file: %s, rel path = %s, flags = %d";
			String fileMessage = String.format(fileMessageFormat, file, file.getProjectRelativePath(), flags);
			System.out.println(fileMessage);

			for (IMarkerDelta markerDelta : delta.getMarkerDeltas()) {
				String message = String.format("%s: %s, kind=%s", markerDelta, markerDelta.getMarker(),
						markerDelta.getKind());
				System.out.println(message);
			}

			// TODO make configurable
			boolean isHidden = file.isHidden();
			if (isHidden) {
				return false;
			}

			// TODO make configurable
			boolean isDerived = file.isDerived();
			if (isDerived) {
				return false;
			}

			// TODO use exclude patterns instead to be independent of java in this class
			IPath outputFolderPath = javaUtil.getDefaultBuildOutputFolderPath(project);
			if (outputFolderPath.isPrefixOf(file.getFullPath())) {
				return false;
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
				if (flags == IResourceDelta.MARKERS) {
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

			return false;
		}
		return true;
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
