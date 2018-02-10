package qa.eclipse.plugin.bundles.checkstyle.handler;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class LeftClickEditorActionDelegate extends AbstractRulerActionDelegate {

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		ResourceBundle bundle = ResourceBundle.getBundle(CheckstyleMessages.getBundleName());
		return new LeftClickEditorAction(bundle, "QAEditor.checkstyle.selectMarker.", editor, rulerInfo);
	}

}
