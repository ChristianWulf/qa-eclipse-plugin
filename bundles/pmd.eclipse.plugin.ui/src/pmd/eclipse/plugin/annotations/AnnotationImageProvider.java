package pmd.eclipse.plugin.annotations;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import pmd.eclipse.plugin.markers.PmdMarkers;
import pmdeclipseplugin.decorators.PmdImageDescriptors;

public class AnnotationImageProvider implements IAnnotationImageProvider {

	private final PmdImageDescriptors pmdImageDescriptors = new PmdImageDescriptors();

	@Override
	public Image getManagedImage(Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
		IMarker marker = markerAnnotation.getMarker();
		Integer priority;
		try {
			priority = (Integer) marker.getAttribute(PmdMarkers.ATTR_KEY_PRIORITY);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
		ImageDescriptor imageDescriptor = pmdImageDescriptors.getForPriority(priority);
		return imageDescriptor.createImage();
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		throw new UnsupportedOperationException(annotation.toString());
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		throw new UnsupportedOperationException(imageDescritporId);
	}

}
