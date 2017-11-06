package pmd.eclipse.plugin.ui.handlers;

//architectural hint: may use eclipse packages
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import pmdeclipseplugin.pmd.PmdTool;

public class PackageExplorerHandler extends AbstractHandler {

	private final PmdTool pmdTool;

	public PackageExplorerHandler() {
		pmdTool = new PmdTool();
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
		// package explorer:
		if (selectedObject instanceof ICompilationUnit) {
			processCompilationUnit((ICompilationUnit) selectedObject, event);
		} else if (selectedObject instanceof IPackageFragment) {
			// TODO
		} else if (selectedObject instanceof IJavaProject) {
			// TODO
		}
	}

	private void processCompilationUnit(ICompilationUnit compilationUnit, ExecutionEvent event) {
		IResource resource = compilationUnit.getResource();

		if (resource instanceof IFile) {
			IFile eclipseFile = (IFile) resource;

			pmdTool.startAsyncAnalysis(eclipseFile, event);
		}
	}
}
