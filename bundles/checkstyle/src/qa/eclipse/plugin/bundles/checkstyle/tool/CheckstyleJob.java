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
public final class CheckstyleJob extends WorkspaceJob {

	private final List<IFile> eclipseFiles;

	private CheckstyleJob(final String name, final List<IFile> eclipseFiles) {
		super(name);
		this.eclipseFiles = eclipseFiles;
	}

	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
		final IResource someEclipseFile = this.eclipseFiles.get(0);
		final IProject eclipseProject = someEclipseFile.getProject();
		if (!eclipseProject.isAccessible()) { // if project has been closed
			return Status.OK_STATUS;
		}

		final IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(eclipseProject);
		final boolean enabled = preferences.getBoolean(CheckstylePreferences.PROP_KEY_ENABLED, false);
		if (!enabled) { // if Checkstyle is disabled for this project
			return Status.OK_STATUS;
		}

		final Map<String, IFile> eclipseFileByFilePath = new HashMap<>();
		// collect data sources
		for (final IFile eclipseFile : this.eclipseFiles) {
			final String key = eclipseFile.getLocation().toFile().getAbsolutePath();
			eclipseFileByFilePath.put(key, eclipseFile);

			try {
				// also remove previous markers on that file
				CheckstyleMarkers.deleteMarkers(eclipseFile);
			} catch (final CoreException e) {
				// ignore if resource does not exist anymore or has been closed
			}
		}

		// update explorer view so that the violation flags are not displayed anymore
		FileIconDecorator.refresh();

		final CheckstyleListener checkstyleListener = new CheckstyleListener(monitor, eclipseFileByFilePath);

		final CheckstyleTool checkstyleTool = new CheckstyleTool();
		try {
			checkstyleTool.startAsyncAnalysis(this.eclipseFiles, checkstyleListener);
		} catch (final Exception e) {
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
	public static void startAsyncAnalysis(final List<IFile> eclipseFiles) {
		if (eclipseFiles.isEmpty()) {
			return;
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (final IFile eclipseFile : eclipseFiles) {
			final ISchedulingRule fileRule = ruleFactory.markerRule(eclipseFile);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		final Job job = new CheckstyleJob("Analysis by Checkstyle", eclipseFiles);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}

}
