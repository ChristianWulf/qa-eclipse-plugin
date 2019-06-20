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
package qa.eclipse.plugin.pmd.ui;

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

import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.tool.PmdTool;

// used by, for example,
//	popup:#CompilationUnitEditorContext
/**
 * Handler to run the pmd job.
 *
 * @author Christian Wulf
 *
 */
public class CompilationUnitEditorHandler extends AbstractHandler {

	private final PmdTool pmdTool;

	/**
	 * Create handler.
	 */
	public CompilationUnitEditorHandler() {
		super();
		this.pmdTool = PmdUIPlugin.getDefault().getPmdTool();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return null;
		}
		final IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			return null;
		}
		final IEditorInput input = activeEditor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			final IFileEditorInput fileEditorInput = (IFileEditorInput) input;
			final IFile file = fileEditorInput.getFile();

			final List<IFile> files = Arrays.asList(file);
			this.pmdTool.startAsyncAnalysis(files);
		}

		return null;
	}
}
