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

		int priority = getPriorityFromAnnotation((MarkerAnnotation) annotation);

		String imageRegistryKey = ImageRegistryKey.getAnnotationKeyByPriority(priority);
		return imageRegistry.get(imageRegistryKey);
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		int priority = getPriorityFromAnnotation((MarkerAnnotation) annotation);

		return ImageRegistryKey.getAnnotationKeyByPriority(priority);
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
