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
package qa.eclipse.plugin.bundles.common.view;

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

import qa.eclipse.plugin.bundles.common.icons.FileIconDecoratorUtils;
import qa.eclipse.plugin.bundles.common.markers.AbstractViolationMarker;

/**
 *
 * @param <T>
 *            kind of marker
 *
 * @author Christian Wulf
 * @author Reiner Jung -- generalization
 */
public final class ClearViolationsViewJob<T extends AbstractViolationMarker> extends WorkspaceJob {

	private final List<T> violationMarkers;
	private final String decratorId;

	private ClearViolationsViewJob(final List<T> violationMarkers, final String decoratorId) {
		super("Clear violations view");
		this.violationMarkers = violationMarkers;
		this.decratorId = decoratorId;
	}

	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMonitor = SubMonitor.convert(monitor);

		for (final T violationMarker : this.violationMarkers) {
			final IMarker marker = violationMarker.getMarker();

			subMonitor.split(1);
			marker.delete();
		}

		FileIconDecoratorUtils.refresh(this.decratorId);

		return Status.OK_STATUS;
	}

	/**
	 * Create and start an analysis job.
	 *
	 * @param violationMarkers
	 *            collection of violation markers
	 * @param decoratorId
	 *            id of the file decorator
	 *
	 * @param <T>
	 *            type of markers
	 */
	public static <T extends AbstractViolationMarker> void startAsyncAnalysis(final List<T> violationMarkers, final String decoratorId) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (final T violationMarker : violationMarkers) {
			final IMarker marker = violationMarker.getMarker();
			final IResource resource = marker.getResource();
			final ISchedulingRule fileRule = ruleFactory.markerRule(resource);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		final Job job = new ClearViolationsViewJob<>(violationMarkers, decoratorId);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}
}
