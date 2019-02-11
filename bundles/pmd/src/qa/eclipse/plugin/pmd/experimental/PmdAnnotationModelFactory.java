/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

// extension point: org.eclipse.core.filebuffers.annotationModelCreation
public class PmdAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory {

	@Override
	public IAnnotationModel createAnnotationModel(final IPath location) {
		final IFile file = FileBuffers.getWorkspaceFileAtLocation(location);
		if (file != null) {
			return new PmdResourceMarkerAnnotationModel(file);
		}
		return new AnnotationModel();
	}
}
