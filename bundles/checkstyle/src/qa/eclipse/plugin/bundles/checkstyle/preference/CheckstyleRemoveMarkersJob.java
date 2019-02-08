package qa.eclipse.plugin.bundles.checkstyle.preference;

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

import qa.eclipse.plugin.bundles.checkstyle.Activator;
import qa.eclipse.plugin.bundles.checkstyle.icons.FileIconDecorator;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleMarkers;

class CheckstyleRemoveMarkersJob extends Job {

	private final IProject project;

	private CheckstyleRemoveMarkersJob(String name, IProject project) {
		super(name);
		this.project = project;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			CheckstyleMarkers.deleteMarkers(project);
		} catch (CoreException e) {
			String message = String.format("Could not delete all markers for project '%s'", project);
			Activator.getDefault().logThrowable(message, e);
		}

		FileIconDecorator.refresh();

		return Status.OK_STATUS;
	}

	public static void start(String jobName, IProject project) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
		ISchedulingRule projectRule = ruleFactory.markerRule(project);

		Job job = new CheckstyleRemoveMarkersJob(jobName, project);
		job.setRule(projectRule);
		job.setUser(true);
		job.schedule();
	}

}
