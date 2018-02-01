package pmd.eclipse.plugin.icons;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.markers.PmdViolationMarker;

/**
 * @author Christian Wulf
 *
 */
public class AnnotationImageProvider implements IAnnotationImageProvider {

	private final ImageRegistry imageRegistry;

	/**
	 * Is created automatically once from the Eclipse Plugin Environment.
	 */
	public AnnotationImageProvider() {
		imageRegistry = PmdUIPlugin.getDefault().getImageRegistry();
	}

	@Override
	public Image getManagedImage(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		// MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
		// IMarker marker = markerAnnotation.getMarker();
		// PmdViolationMarker violationMarker = new PmdViolationMarker(marker);

		int priority = getPriorityFromAnnotation((MarkerAnnotation) annotation);

		// apply filter
		String imageRegistryKey = null;
		int lowestAllowedPriority = 5;
		if (priority <= lowestAllowedPriority) {
			imageRegistryKey = ImageRegistryKey.getAnnotationKeyByPriority(priority);
		}

		return imageRegistry.get(imageRegistryKey);
		// return null;
	}

	// letting getManagedImage() and getImageDescriptorId() return null
	// only hides the editor annotation icon,
	// but text hovering and left-click still work

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		int priority = getPriorityFromAnnotation((MarkerAnnotation) annotation);

		// apply filter
		String imageRegistryKey = null;
		int lowestAllowedPriority = 5;
		if (priority <= lowestAllowedPriority) {
			imageRegistryKey = ImageRegistryKey.getAnnotationKeyByPriority(priority);
		}

		return imageRegistryKey;
		// return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
		return imageRegistry.getDescriptor(imageDescriptorId);
	}

	private int getPriorityFromAnnotation(MarkerAnnotation markerAnnotation) {
		IMarker marker = markerAnnotation.getMarker();
		PmdViolationMarker violationMarker = new PmdViolationMarker(marker);
		return violationMarker.getPriority();
	}

}
