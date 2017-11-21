package pmd.eclipse.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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
		// IMarkerDelta[] markerDeltas =
		// event.findMarkerDeltas(PmdMarkers.PMD_VIOLATION_MARKER, false);
		// for (IMarkerDelta markerDelta : markerDeltas) {
		// markerDelta.
		// }

		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if (delta.getResource() instanceof IFile) {
						IResource file = delta.getResource();
						return false;
					}
					return true;
				}
			});
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

		return;
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
