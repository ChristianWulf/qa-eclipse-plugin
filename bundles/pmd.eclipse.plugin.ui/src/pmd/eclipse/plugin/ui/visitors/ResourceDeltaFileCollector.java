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

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getResource() instanceof IFile) {
			IFile file = (IFile) delta.getResource();
			IProject project = file.getProject();

			System.out.println("file: " + file);

			int flags = delta.getFlags();

			for (IMarkerDelta markerDelta : delta.getMarkerDeltas()) {
				String message = String.format("%s: %s, kind=%s", markerDelta, markerDelta.getMarker(),
						markerDelta.getKind());
				System.out.println(message);
			}

			boolean isHidden = file.isHidden();
			boolean isDerived = file.isDerived();

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
