package pmd.eclipse.plugin.settings;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.icons.FileIconDecorator;
import pmd.eclipse.plugin.markers.PmdMarkers;

class PmdRemoveMarkersJob extends Job {

	private final IProject project;

	public PmdRemoveMarkersJob(String name, IProject project) {
		super(name);
		this.project = project;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			project.deleteMarkers(PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			String message = String.format("Could not delete all markers for project '%s'", project);
			PmdUIPlugin.getDefault().logException(message, e);
		}

		FileIconDecorator.refresh();

		return Status.OK_STATUS;
	}

}
