package pmd.eclipse.plugin.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class JumpToViolationsView extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return null;
		}
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			return null;
		}

		final IVerticalRulerInfo verticalRulerInfo = activeEditor.getAdapter(IVerticalRulerInfo.class);
		if (verticalRulerInfo != null) {

		}

		// TODO Auto-generated method stub
		return null;
	}

}
