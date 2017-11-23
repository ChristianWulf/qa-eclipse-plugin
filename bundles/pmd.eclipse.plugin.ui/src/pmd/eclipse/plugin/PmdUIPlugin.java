package pmd.eclipse.plugin;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import pmd.eclipse.plugin.builder.IncrementalViolationMarkerBuilder;
import pmd.eclipse.plugin.pmd.PmdTool;

/**
 * The activator class controls the plug-in life cycle
 */
public class PmdUIPlugin extends AbstractUIPlugin implements IResourceChangeListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "pmd-eclipse-plugin"; //$NON-NLS-1$

	// The shared instance
	private static PmdUIPlugin plugin;

	private BundleContext context;

	private PmdTool pmdTool;

	/**
	 * The constructor
	 */
	public PmdUIPlugin() {
		// default constructor is required by Eclipse
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
		this.pmdTool = new PmdTool();

		registerResourceChangeListener();
	}

	private void registerResourceChangeListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

		// ISaveParticipant saveParticipant = new PmdSaveParticipant();
		// try {
		// ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID,
		// saveParticipant);
		// } catch (CoreException e) {
		// throw new IllegalStateException(e);
		// }
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// ResourceDeltaFileCollector resourceDeltaFileCollector = new
		// ResourceDeltaFileCollector();
		//
		// Object source = event.getSource();
		// System.out.println("source: " + source);
		//
		// int buildKind = event.getBuildKind();
		// System.out.println("buildKind: " + buildKind);
		//
		// try {
		// event.getDelta().accept(resourceDeltaFileCollector);
		// } catch (CoreException e) {
		// throw new IllegalStateException(e);
		// }

		// for (Entry<IProject, List<IFile>> addedFiles :
		// resourceDeltaFileCollector.getAddedFiles().entrySet()) {
		// pmdTool.startAsyncAnalysis(addedFiles.getValue());
		// }
		//
		// for (Entry<IProject, List<IFile>> changedFiles :
		// resourceDeltaFileCollector.getChangedFiles().entrySet()) {
		// pmdTool.startAsyncAnalysis(changedFiles.getValue());
		// }

		// your view listens to marker changes and thus is indirectly notified about
		// removed resource
		// for (Entry<IProject, List<IFile>> removedFiles :
		// resourceDeltaFileCollector.getRemovedFiles().entrySet()) {}

		return;
	}

	public void registerBuilder(IProject project) {
		IProjectDescription desc;
		try {
			desc = project.getDescription();
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
		ICommand[] commands = desc.getBuildSpec();
		boolean found = false;

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(IncrementalViolationMarkerBuilder.BUILDER_ID)) {
				found = true;
				break;
			}
		}

		if (!found) {
			// add builder to project
			ICommand command = desc.newCommand();
			command.setBuilderName(IncrementalViolationMarkerBuilder.BUILDER_ID);
			ICommand[] newCommands = new ICommand[commands.length + 1];

			// Add it before other builders.
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			desc.setBuildSpec(newCommands);
			try {
				project.setDescription(desc, null);
			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PmdUIPlugin getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
	}

	public PmdTool getPmdTool() {
		return pmdTool;
	}

	public void logException(String message, Exception exception) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
		getLog().log(status);
	}

	public void logWarning(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		getLog().log(status);
	}

}
