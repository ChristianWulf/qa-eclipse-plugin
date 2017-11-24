package pmd.eclipse.plugin.ui;

import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;

public class PmdAnnotationHover extends DefaultAnnotationHover implements IJavaEditorTextHover {

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		// TODO Auto-generated method stub
		return super.getHoverInfo(sourceViewer, lineNumber);
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEditor(IEditorPart arg0) {
		// TODO Auto-generated method stub
		return;
	}
}
