package pmd.eclipse.plugin.icons;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.pmd.RulePriority;
import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.markers.PmdViolationMarker;

public class FileIconDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ID = "pmd-eclipse-plugin.decorator";

	private final ImageRegistry imageRegistry;

	public FileIconDecorator() {
		imageRegistry = PmdUIPlugin.getDefault().getImageRegistry();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// update if property "xx" of element has been updated
		// update if pmd has run for the file represented by the element
		return true;
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof IResource)) {
			return;
		}

		IResource resource = (IResource) element;
		// do not decorate if the project has been closed
		if (!resource.isAccessible()) {
			return;
		}

		ImageDescriptor imageDescriptor = null;
		try {
			imageDescriptor = getImageDescriptor(resource);
		} catch (CoreException e) {
			PmdUIPlugin.getDefault().logThrowable("Error on decorating element.", e);
		}

		decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
	}

	private ImageDescriptor getImageDescriptor(IResource resource) throws CoreException {
		int depth = IResource.DEPTH_INFINITE;
		// if (resource instanceof IFolder) {
		// depth = IResource.DEPTH_INFINITE;
		// } else if (resource instanceof IFile) {
		// depth = IResource.DEPTH_ZERO;
		// }

		IMarker[] markers = resource.findMarkers(PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER, true, depth);

		// do not display any file decorator if there are no markers
		if (markers.length == 0) {
			return null;
		}

		int highestPriority = RulePriority.LOW.getPriority();
		for (IMarker marker : markers) {
			PmdViolationMarker violationMarker = new PmdViolationMarker(marker);
			int priority = violationMarker.getPriority();
			// 1 is the highest priority, so compare with '<'
			if (priority < highestPriority) {
				highestPriority = priority;
				// fast exit: if highest priority is max priority, then quit at once
				if (highestPriority == RulePriority.HIGH.getPriority()) {
					break;
				}
			}
		}

		// apply filter
		ImageDescriptor imageDescriptor = null;
		int lowestAllowedPriority = 5;
		if (highestPriority <= lowestAllowedPriority) {
			String imageRegistryKey = ImageRegistryKey.getFileDecoratorKeyByPriority(highestPriority);
			imageDescriptor = imageRegistry.getDescriptor(imageRegistryKey);
		}

		return imageDescriptor;
	}

	public static void refresh() {
		IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		IBaseLabelProvider decorator = manager.getBaseLabelProvider(FileIconDecorator.ID);
		if (decorator != null) { // decorator is enabled
			ILabelProviderListener listener = (ILabelProviderListener) manager;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.labelProviderChanged(new LabelProviderChangedEvent(decorator));
				}
			});
		}
	}

}
