package qa.eclipse.plugin.bundles.checkstyle.view;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;

import qa.eclipse.plugin.bundles.checkstyle.icons.FileIconDecorator;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;

class ClearViolationsViewJob extends WorkspaceJob {

	private final List<CheckstyleViolationMarker> violationMarkers;

	private ClearViolationsViewJob(List<CheckstyleViolationMarker> violationMarkers) {
		super("Clear violations view");
		this.violationMarkers = violationMarkers;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		for (CheckstyleViolationMarker checkstyleViolationMarker : violationMarkers) {
			IMarker marker = checkstyleViolationMarker.getMarker();

			subMonitor.split(1);
			marker.delete();
		}
		
		FileIconDecorator.refresh();

		return Status.OK_STATUS;
	}

	public static void startAsyncAnalysis(List<CheckstyleViolationMarker> violationMarkers) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (CheckstyleViolationMarker violationMakrer : violationMarkers) {
			IMarker marker = violationMakrer.getMarker();
			IResource resource = marker.getResource();
			ISchedulingRule fileRule = ruleFactory.markerRule(resource);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		Job job = new ClearViolationsViewJob(violationMarkers);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}
}
