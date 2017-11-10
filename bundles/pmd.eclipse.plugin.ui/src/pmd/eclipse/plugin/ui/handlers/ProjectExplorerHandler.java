package pmd.eclipse.plugin.ui.handlers;

//architectural hint: may use eclipse packages
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import pmdeclipseplugin.PmdUIPlugin;
import pmdeclipseplugin.pmd.PmdTool;

public class ProjectExplorerHandler extends AbstractHandler {

	private final PmdTool pmdTool;

	public ProjectExplorerHandler() {
		pmdTool = PmdUIPlugin.getDefault().getPmdTool();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			Iterator<?> iter = structuredSelection.iterator();
			while (iter.hasNext()) {
				Object selectedObject = iter.next();
				processSelectedObject(selectedObject, event);
			}

		}

		return null;
	}

	private void processSelectedObject(Object selectedObject, ExecutionEvent event) {
		// project explorer:
		if (selectedObject instanceof IFile) {
			pmdTool.startAsyncAnalysis((IFile) selectedObject);
		} else if (selectedObject instanceof IFolder) {
			// TODO
		} else if (selectedObject instanceof IProject) {
			// TODO
		} else {
			// TODO
		}
	}

}
