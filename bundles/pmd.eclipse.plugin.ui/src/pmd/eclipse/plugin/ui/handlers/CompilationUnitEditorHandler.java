package pmd.eclipse.plugin.ui.handlers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import pmdeclipseplugin.PmdUIPlugin;
import pmdeclipseplugin.pmd.PmdTool;

// used by, for example, 
//	popup:#CompilationUnitEditorContext
public class CompilationUnitEditorHandler extends AbstractHandler {

	private final PmdTool pmdTool;

	public CompilationUnitEditorHandler() {
		pmdTool = PmdUIPlugin.getDefault().getPmdTool();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null)
			return null;
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null)
			return null;
		IEditorInput input = activeEditor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) input;
			IFile file = fileEditorInput.getFile();

			List<IFile> files = Arrays.asList(file);
			pmdTool.startAsyncAnalysis(files);
		}

		return null;
	}
}
