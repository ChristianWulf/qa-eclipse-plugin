package pmd.eclipse.plugin.annotations;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import pmd.eclipse.plugin.markers.PmdMarkers;
import pmdeclipseplugin.PmdUIPlugin;

public class AnnotationImageProvider implements IAnnotationImageProvider {

	private final ImageRegistry imageRegistry;

	public AnnotationImageProvider() {
		imageRegistry = PmdUIPlugin.getDefault().getImageRegistry();

		for (int i = 1; i <= 5; i++) {
			String imageRegistryKey = String.valueOf(i) + "-annotation";
			String imageFilePath = "/icons/priority" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPluginalways returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(PmdUIPlugin.class, imageFilePath);
			imageRegistry.put(imageRegistryKey, imageDescriptor);
		}
	}

	@Override
	public Image getManagedImage(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		int priority = getPriorityFromAnnotation((MarkerAnnotation) annotation);

		return imageRegistry.get(String.valueOf(priority) + "-annotation");
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		int priority = getPriorityFromAnnotation((MarkerAnnotation) annotation);
		return String.valueOf(priority) + "-annotation";
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
		return imageRegistry.getDescriptor(imageDescriptorId);
	}

	private int getPriorityFromAnnotation(MarkerAnnotation markerAnnotation) {
		IMarker marker = markerAnnotation.getMarker();
		Integer priority;
		try {
			priority = (Integer) marker.getAttribute(PmdMarkers.ATTR_KEY_PRIORITY);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
		return priority;
	}

}
