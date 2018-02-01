package pmd.eclipse.plugin.experimental;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

// extension point: org.eclipse.core.filebuffers.annotationModelCreation
public class PmdAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory {

	@Override
	public IAnnotationModel createAnnotationModel(IPath location) {
		IFile file = FileBuffers.getWorkspaceFileAtLocation(location);
		if (file != null) {
			return new PmdResourceMarkerAnnotationModel(file);
		}
		return new AnnotationModel();
	}
}
