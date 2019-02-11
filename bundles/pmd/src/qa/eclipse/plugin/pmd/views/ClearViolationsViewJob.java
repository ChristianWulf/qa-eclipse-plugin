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
package qa.eclipse.plugin.pmd.views;

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

import qa.eclipse.plugin.pmd.icons.FileIconDecorator;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

class ClearViolationsViewJob extends WorkspaceJob {

	private final List<PmdViolationMarker> violationMarkers;

	private ClearViolationsViewJob(final List<PmdViolationMarker> violationMarkers) {
		super("Clear violations view");
		this.violationMarkers = violationMarkers;
	}

	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor);

		for (final PmdViolationMarker violationMarker : this.violationMarkers) {
			final IMarker marker = violationMarker.getMarker();

			subMonitor.split(1);
			marker.delete();
		}

		FileIconDecorator.refresh();

		return Status.OK_STATUS;
	}

	public static void startAsyncAnalysis(final List<PmdViolationMarker> violationMarkers) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (final PmdViolationMarker violationMarker : violationMarkers) {
			final IMarker marker = violationMarker.getMarker();
			final IResource resource = marker.getResource();
			final ISchedulingRule fileRule = ruleFactory.markerRule(resource);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		final Job job = new ClearViolationsViewJob(violationMarkers);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}
}
