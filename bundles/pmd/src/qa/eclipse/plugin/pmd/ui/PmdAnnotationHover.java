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
package qa.eclipse.plugin.pmd.ui;

import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;

/**
 *
 * @author Christian Wulf
 *
 */
// triggered whenever the cursor hovers over arbitrary text within the editor.
// that's why the interface name contains the term "text" and not "annotation".
public class PmdAnnotationHover extends DefaultAnnotationHover implements IJavaEditorTextHover {

	@Override
	protected boolean isIncluded(final Annotation annotation) {
		// TODO Auto-generated method stub
		return super.isIncluded(annotation);
	}

	@Override
	public String getHoverInfo(final ISourceViewer sourceViewer, final int lineNumber) {
		// TODO Auto-generated method stub
		return super.getHoverInfo(sourceViewer, lineNumber);
	}

	@Override
	public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEditor(final IEditorPart arg0) {
		// TODO Auto-generated method stub
		return;
	}
}
