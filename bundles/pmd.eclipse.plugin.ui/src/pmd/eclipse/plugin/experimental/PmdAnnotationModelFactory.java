package pmd.eclipse.plugin.experimental;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

// extension point: org.eclipse.core.filebuffers.annotationModelCreation
public class PmdAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory {

	@Override
	public IAnnotationModel createAnnotationModel(IPath location) {
		// TODO Auto-generated method stub
		return super.createAnnotationModel(location);
	}
}
