package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

class PmdResourceMarkerAnnotationModel extends ResourceMarkerAnnotationModel {

	private int maxPriority = 1;

	public PmdResourceMarkerAnnotationModel(IResource resource) {
		super(resource);
	}

	@Override
	protected boolean isAcceptable(IMarker marker) {
		boolean isAcceptable = super.isAcceptable(marker);
		if (!isAcceptable) {
			return false;
		}

		PmdViolationMarker pmdViolationMarker = new PmdViolationMarker(marker);
		int priority = pmdViolationMarker.getPriority();
		return (maxPriority >= priority);
	}

}
