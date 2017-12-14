package pmd.eclipse.plugin.icons;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import net.sourceforge.pmd.RulePriority;
import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.markers.PmdViolationMarker;

public class FileIconDecorator extends LabelProvider implements ILightweightLabelDecorator {

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

		int depth = IResource.DEPTH_INFINITE;
		// if (resource instanceof IFolder) {
		// depth = IResource.DEPTH_INFINITE;
		// } else if (resource instanceof IFile) {
		// depth = IResource.DEPTH_ZERO;
		// }

		IMarker[] markers;
		try {
			markers = resource.findMarkers(PmdMarkers.PMD_VIOLATION_MARKER, false, depth);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

		// do not display any file decorator if there are no markers
		if (markers.length == 0) {
			return;
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

		decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
	}

}
