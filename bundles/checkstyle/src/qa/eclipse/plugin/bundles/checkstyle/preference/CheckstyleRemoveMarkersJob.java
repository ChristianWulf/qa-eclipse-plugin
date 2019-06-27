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

import qa.eclipse.plugin.bundles.checkstyle.CheckstyleUIPlugin;
import qa.eclipse.plugin.bundles.checkstyle.icons.FileIconDecorator;
import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleMarkersUtils;
import qa.eclipse.plugin.bundles.common.icons.FileIconDecoratorUtils;

/**
 *
 * @author Christian Wulf
 *
 */
final class CheckstyleRemoveMarkersJob extends Job {

	private final IProject project;

	private CheckstyleRemoveMarkersJob(final String name, final IProject project) {
		super(name);
		this.project = project;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		try {
			CheckstyleMarkersUtils.deleteMarkers(this.project);
		} catch (final CoreException e) {
			final String message = String.format("Could not delete all markers for project '%s'", this.project);
			CheckstyleUIPlugin.getDefault().logThrowable(message, e);
		}

		FileIconDecoratorUtils.refresh(FileIconDecorator.ID);

		return Status.OK_STATUS;
	}

	public static void start(final String jobName, final IProject project) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
		final ISchedulingRule projectRule = ruleFactory.markerRule(project);

		final Job job = new CheckstyleRemoveMarkersJob(jobName, project);
		job.setRule(projectRule);
		job.setUser(true);
		job.schedule();
	}

}
