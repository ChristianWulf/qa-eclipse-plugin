package pmd.eclipse.plugin.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SelectRulerAction;

public class LeftClickEditorActionDelegate extends SelectRulerAction /* AbstractRulerActionDelegate */ {

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		// new SelectMarkerRulerAction(TextEditorMessages.getBundleForConstructedKeys(),
		// "LeftClickEditorActionDelegate",
		// editor, rulerInfo);
		// new SelectAnnotationRulerAction(bundle, prefix, editor);
		return super.createAction(editor, rulerInfo);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button != 1)
			return;

		super.mouseDown(e);
		// return;
	}
}
