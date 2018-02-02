package pmd.eclipse.plugin.ui;

//architectural hint: may use eclipse packages
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.pmd.PmdTool;
import pmd.eclipse.plugin.ui.visitors.ResourceCollector;

public class ExplorerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();

		// if (selection instanceof ITreeSelection) {
		// TreePath[] treePaths = ((ITreeSelection) selection).getPaths();
		// for (TreePath treePath : treePaths) {
		// System.out.println(treePath);
		// Object lastSegment = treePath.getLastSegment();
		// System.out.println(lastSegment);
		// }
		// }

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			ResourceCollector resourceCollector = new ResourceCollector();
			// TODO consider to put it into a job, too. Use jobfinishedlistener afterwards.
			Iterator<?> iter = structuredSelection.iterator();
			while (iter.hasNext()) {
				Object selectedObject = iter.next();
				collectElement(selectedObject, resourceCollector);
			}

			Map<IProject, List<IFile>> projectResources = resourceCollector.getProjectResources();

			PmdTool pmdTool = PmdUIPlugin.getDefault().getPmdTool();
			for (Entry<IProject, List<IFile>> entry : projectResources.entrySet()) {
				pmdTool.startAsyncAnalysis(entry.getValue());
			}
		}

		return null;
	}

	private void collectElement(Object selectedObject, ResourceCollector resourceCollector) {
		if (selectedObject instanceof IAdaptable) {
			IResource resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);

			// IWorkbenchWindow activeWorkbenchWindow =
			// PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			// IWorkbenchPart activePart =
			// activeWorkbenchWindow.getActivePage().getActivePart();
			// if (activePart instanceof IPackagesViewPart) {
			// PackageExplorerPart;
			// ProjectExplorer?
			// "org.eclipse.jdt.ui.PackageExplorer" = org.eclipse.jdt.ui.JavaUI.ID_PACKAGES
			// }

			// TODO determine depth based on the package presentation: flat or hierarchical
			int depth = (true) ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE;

			try {
				resource.accept(resourceCollector, depth, IContainer.NONE);
			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}
		}
	}

}
