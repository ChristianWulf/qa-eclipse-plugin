package pmdeclipseplugin.decorators;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import net.sourceforge.pmd.RulePriority;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmdeclipseplugin.PmdUIPlugin;

public class FileIconDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private final ImageRegistry imageRegistry;

	public FileIconDecorator() {
		imageRegistry = PmdUIPlugin.getDefault().getImageRegistry();

		for (int i = 1; i <= 5; i++) {
			String imageRegistryKey = String.valueOf(i);
			String imageFilePath = "/icons/priority" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPluginalways returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(PmdUIPlugin.class, imageFilePath);
			imageRegistry.put(imageRegistryKey, imageDescriptor);
		}
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
		IMarker[] markers;
		try {
			markers = resource.findMarkers(PmdMarkers.PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION, false,
					IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

		int highestPriority = RulePriority.LOW.getPriority();
		for (IMarker marker : markers) {
			int priority = marker.getAttribute(PmdMarkers.ATTR_KEY_PRIORITY, 5);
			// 1 is the highest priority, so compare with '<'
			if (priority < highestPriority) {
				highestPriority = priority;
			}
		}

		ImageDescriptor imageDescriptor = imageRegistry.getDescriptor(String.valueOf(highestPriority));
		decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
	}

}
