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
package qa.eclipse.plugin.pmd.builder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IBuildContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.tool.PmdTool;
import qa.eclipse.plugin.pmd.ui.visitors.ResourceDeltaFileCollector;

/**
 *
 * @author Christian Wulf
 *
 */
public class IncrementalViolationMarkerBuilder extends IncrementalProjectBuilder {

	// reference impl:
	// https://github.com/eclipse/eclipse.jdt.core/blob/master/org.eclipse.jdt.core/plugin.xml#L94

	public static final String BUILDER_ID = "pmd.eclipse.plugin.builder.IncrementalViolationMarkerBuilder";

	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

	private final PmdTool pmdTool;

	public IncrementalViolationMarkerBuilder() {
		// necessary default public ctor
		this.pmdTool = PmdUIPlugin.getDefault().getPmdTool();
	}

	@Override
	protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) {
		final IBuildContext buildContext = this.getContext();
		final IBuildConfiguration[] allReferencedBuildConfigs = buildContext.getAllReferencedBuildConfigs();
		final IBuildConfiguration[] allReferencingBuildConfigs = buildContext.getAllReferencingBuildConfigs();
		final IBuildConfiguration[] requestedConfigs = buildContext.getRequestedConfigs();

		try {
			this.buildByKind(kind, monitor);
		} catch (final CoreException e) {
			PmdUIPlugin.getDefault().logThrowable("Error on building by kind.", e);
		}

		return IncrementalViolationMarkerBuilder.EMPTY_PROJECT_ARRAY;
	}

	private void buildByKind(final int kind, final IProgressMonitor monitor) throws CoreException {
		switch (kind) {
		case IncrementalProjectBuilder.FULL_BUILD: {
			this.fullBuild(monitor);
			break;
		}
		case IncrementalProjectBuilder.AUTO_BUILD: {
			final IResourceDelta delta = this.getDelta(this.getProject());
			if (delta == null) {
				this.fullBuild(monitor);
			} else {
				this.incrementalBuild(delta, monitor);
			}
			break;
		}
		case IncrementalProjectBuilder.INCREMENTAL_BUILD: {
			final IResourceDelta delta = this.getDelta(this.getProject());
			if (delta == null) {
				this.fullBuild(monitor);
			} else {
				this.incrementalBuild(delta, monitor);
			}
			break;
		}
		case IncrementalProjectBuilder.CLEAN_BUILD: {
			break;
		}
		default:
			break;
		}
	}

	@Override
	public ISchedulingRule getRule(final int kind, final Map<String, String> args) {
		return null; // This builder starts a job. Thus, we do not need to lock resources for the
		// builder itself.
	}

	private void fullBuild(final IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	private void incrementalBuild(final IResourceDelta delta, final IProgressMonitor monitor) throws CoreException {
		final ResourceDeltaFileCollector resourceDeltaFileCollector = new ResourceDeltaFileCollector();

		delta.accept(resourceDeltaFileCollector);

		for (final Entry<IProject, List<IFile>> addedFiles : resourceDeltaFileCollector.getAddedFiles().entrySet()) {
			this.pmdTool.startAsyncAnalysis(addedFiles.getValue());
		}

		for (final Entry<IProject, List<IFile>> changedFiles : resourceDeltaFileCollector.getChangedFiles()
				.entrySet()) {
			this.pmdTool.startAsyncAnalysis(changedFiles.getValue());
		}

		// your view listens to marker changes and thus is indirectly notified about
		// removed resource
		return;
	}

}
