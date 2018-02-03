package qa.eclipse.plugin.bundles.checkstyle.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import qa.eclipse.plugin.bundles.checkstyle.Activator;
import qa.eclipse.plugin.bundles.checkstyle.icons.FileIconDecorator;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleMarkers;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;

public class CheckstyleJob extends WorkspaceJob {

	private final List<IFile> eclipseFiles;

	public CheckstyleJob(String name, List<IFile> eclipseFiles) {
		super(name);
		this.eclipseFiles = eclipseFiles;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		final IResource someEclipseFile = eclipseFiles.get(0);
		final IProject eclipseProject = someEclipseFile.getProject();
		if (!eclipseProject.isAccessible()) { // if project has been closed
			return Status.OK_STATUS;
		}

		IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(eclipseProject);
		boolean pmdEnabled = preferences.getBoolean(CheckstylePreferences.PROP_KEY_ENABLED, false);
		if (!pmdEnabled) { // if PMD is disabled for this project
			return Status.OK_STATUS;
		}

		// collect data sources
		final Map<String, IFile> eclipseFilesMap = new HashMap<>();
		for (IFile eclipseFile : eclipseFiles) {
			try {
				// also remove previous PMD markers on that file
				eclipseFile.deleteMarkers(CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER, true, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				// ignore if resource does not exist anymore or has been closed
			}
		}

		// update explorer view so that the violation flag are not displayed anymore
		FileIconDecorator.refresh();
		
		CheckstyleTool checkstyleTool = Activator.getDefault().getCheckstyleTool();
		checkstyleTool.startAsyncAnalysis(eclipseFiles);
		// TODO 
		
		
		return null;
	}

}
