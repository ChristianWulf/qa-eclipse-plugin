package pmd.eclipse.plugin.ui;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class LeftClickEditorActionDelegate extends AbstractRulerActionDelegate {

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		return new LeftClickEditorAction(ResourceBundle.getBundle(PmdMessages.getBundleName()),
				"QAEditor.selectMarker.", editor, rulerInfo);
	}

}
