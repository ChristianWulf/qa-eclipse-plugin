package qa.eclipse.plugin.bundles.checkstyle.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import qa.eclipse.plugin.bundles.checkstyle.icons.FileIconDecorator;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleMarkers;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;
import qa.eclipse.plugin.bundles.common.Logger;

public class CheckstyleJob extends WorkspaceJob {

	private final List<IFile> eclipseFiles;

	private CheckstyleJob(String name, List<IFile> eclipseFiles) {
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
		boolean enabled = preferences.getBoolean(CheckstylePreferences.PROP_KEY_ENABLED, false);
		if (!enabled) { // if Checkstyle is disabled for this project
			return Status.OK_STATUS;
		}

		final Map<String, IFile> eclipseFileByFilePath = new HashMap<>();
		// collect data sources
		for (IFile eclipseFile : eclipseFiles) {
			String key = eclipseFile.getLocation().toFile().getAbsolutePath();
			eclipseFileByFilePath.put(key, eclipseFile);

			try {
				// also remove previous markers on that file
				CheckstyleMarkers.deleteMarkers(eclipseFile);
			} catch (CoreException e) {
				// ignore if resource does not exist anymore or has been closed
			}
		}

		// update explorer view so that the violation flags are not displayed anymore
		FileIconDecorator.refresh();

		CheckstyleListener checkstyleListener = new CheckstyleListener(monitor, eclipseFileByFilePath);

		CheckstyleTool checkstyleTool = new CheckstyleTool();
		try {
			checkstyleTool.startAsyncAnalysis(eclipseFiles, checkstyleListener);
		} catch (Exception e) {
			Logger.logThrowable("Exception while analyzing with Checkstyle.", e);
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

	/**
	 * All passed files must belong to the same project.
	 * 
	 * @param eclipseFiles
	 */
	public static void startAsyncAnalysis(List<IFile> eclipseFiles) {
		if (eclipseFiles.isEmpty()) {
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (IFile eclipseFile : eclipseFiles) {
			ISchedulingRule fileRule = ruleFactory.markerRule(eclipseFile);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		Job job = new CheckstyleJob("Analysis by Checkstyle", eclipseFiles);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}

}
