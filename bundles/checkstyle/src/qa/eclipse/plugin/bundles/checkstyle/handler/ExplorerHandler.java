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
package qa.eclipse.plugin.bundles.checkstyle.handler;

//architectural hint: may use eclipse packages
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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

import qa.eclipse.plugin.bundles.checkstyle.tool.CheckstyleJob;

public class ExplorerHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IWorkbenchPage activePage = window.getActivePage();
		final ISelection selection = activePage.getSelection();

		// if (selection instanceof ITreeSelection) {
		// TreePath[] treePaths = ((ITreeSelection) selection).getPaths();
		// for (TreePath treePath : treePaths) {
		// System.out.println(treePath);
		// Object lastSegment = treePath.getLastSegment();
		// System.out.println(lastSegment);
		// }
		// }

		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			final ResourceCollector resourceCollector = new ResourceCollector();
			// TODO consider to put it into a job, too. Use jobfinishedlistener afterwards.
			final Iterator<?> iter = structuredSelection.iterator();
			while (iter.hasNext()) {
				final Object selectedObject = iter.next();
				this.collectElement(selectedObject, resourceCollector);
			}

			final Map<IProject, List<IFile>> projectResources = resourceCollector.getProjectResources();

			for (final Entry<IProject, List<IFile>> entry : projectResources.entrySet()) {
				CheckstyleJob.startAsyncAnalysis(entry.getValue());
			}
		}

		return null;
	}

	private void collectElement(final Object selectedObject, final ResourceCollector resourceCollector) {
		if (selectedObject instanceof IAdaptable) {
			final IResource resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);

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
			final int depth = (true) ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE;

			try {
				resource.accept(resourceCollector, depth, IResource.NONE);
			} catch (final CoreException e) {
				throw new IllegalStateException(e);
			}
		}
	}

}
