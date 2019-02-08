package pmd.eclipse.plugin.builder;

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

import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.pmd.PmdJob;
import pmd.eclipse.plugin.ui.visitors.ResourceDeltaFileCollector;

public class IncrementalViolationMarkerBuilder extends IncrementalProjectBuilder {

	// reference impl:
	// https://github.com/eclipse/eclipse.jdt.core/blob/master/org.eclipse.jdt.core/plugin.xml#L94

	public static final String BUILDER_ID = "pmd.eclipse.plugin.builder.IncrementalViolationMarkerBuilder";

	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

	public IncrementalViolationMarkerBuilder() {
		// necessary default public ctor
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		IBuildContext buildContext = getContext();
		IBuildConfiguration[] allReferencedBuildConfigs = buildContext.getAllReferencedBuildConfigs();
		IBuildConfiguration[] allReferencingBuildConfigs = buildContext.getAllReferencingBuildConfigs();
		IBuildConfiguration[] requestedConfigs = buildContext.getRequestedConfigs();

		try {
			buildByKind(kind, monitor);
		} catch (CoreException e) {
			PmdUIPlugin.getDefault().logThrowable("Error on building by kind.", e);
		}

		return EMPTY_PROJECT_ARRAY;
	}

	private void buildByKind(int kind, IProgressMonitor monitor) throws CoreException {
		switch (kind) {
		case IncrementalProjectBuilder.FULL_BUILD: {
			fullBuild(monitor);
			break;
		}
		case IncrementalProjectBuilder.AUTO_BUILD: {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
			break;
		}
		case IncrementalProjectBuilder.INCREMENTAL_BUILD: {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
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
	public ISchedulingRule getRule(int kind, Map<String, String> args) {
		return null; // This builder starts a job. Thus, we do not need to lock resources for the
						// builder itself.
	}

	private void fullBuild(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		ResourceDeltaFileCollector resourceDeltaFileCollector = new ResourceDeltaFileCollector();

		delta.accept(resourceDeltaFileCollector);

		for (Entry<IProject, List<IFile>> addedFiles : resourceDeltaFileCollector.getAddedFiles().entrySet()) {
			PmdJob.startAsyncAnalysis(addedFiles.getValue());
		}

		for (Entry<IProject, List<IFile>> changedFiles : resourceDeltaFileCollector.getChangedFiles().entrySet()) {
			PmdJob.startAsyncAnalysis(changedFiles.getValue());
		}

		// your view listens to marker changes and thus is indirectly notified about
		// removed resource
		return;
	}

}
