package pmd.eclipse.plugin.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.icons.FileIconDecorator;
import pmd.eclipse.plugin.markers.PmdMarkers;

class PmdRemoveMarkersJob extends Job {

	private final IProject project;

	private PmdRemoveMarkersJob(String name, IProject project) {
		super(name);
		this.project = project;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			PmdMarkers.deleteMarkers(project);
		} catch (CoreException e) {
			String message = String.format("Could not delete all markers for project '%s'", project);
			PmdUIPlugin.getDefault().logThrowable(message, e);
		}

		FileIconDecorator.refresh();

		return Status.OK_STATUS;
	}

	public static void start(String jobName, IProject project) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
		ISchedulingRule projectRule = ruleFactory.markerRule(project);

		Job job = new PmdRemoveMarkersJob(jobName, project);
		job.setRule(projectRule);
		job.setUser(true);
		job.schedule();
	}

}
