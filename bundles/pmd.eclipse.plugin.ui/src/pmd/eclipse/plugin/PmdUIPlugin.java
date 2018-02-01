package pmd.eclipse.plugin;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import pmd.eclipse.plugin.builder.IncrementalViolationMarkerBuilder;
import pmd.eclipse.plugin.icons.ImageRegistryKey;
import pmd.eclipse.plugin.pmd.PmdTool;
import pmd.eclipse.plugin.ui.visitors.ResourceDeltaFileCollector;

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

		// IDocumentProvider provider = ((ITextEditor)
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getDocumentProvider();
		// AnnotationModel am =
		// (AnnotationModel)provider.getAnnotationModel((PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput()));
		// DocumentProviderRegistry.getDefault().getDocumentProvider(null);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	// represents: analyze on save
	public void resourceChanged(IResourceChangeEvent event) {
		ResourceDeltaFileCollector resourceDeltaFileCollector = new ResourceDeltaFileCollector();

		try {
			event.getDelta().accept(resourceDeltaFileCollector);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

		for (Entry<IProject, List<IFile>> addedFiles : resourceDeltaFileCollector.getAddedFiles().entrySet()) {
			pmdTool.startAsyncAnalysis(addedFiles.getValue());
		}

		for (Entry<IProject, List<IFile>> changedFiles : resourceDeltaFileCollector.getChangedFiles().entrySet()) {
			pmdTool.startAsyncAnalysis(changedFiles.getValue());
		}

		// our view listens to marker changes and thus is indirectly notified about
		// removed resource

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

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);

		ImageRegistryKey.initialize(reg);
	}

	/**
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
