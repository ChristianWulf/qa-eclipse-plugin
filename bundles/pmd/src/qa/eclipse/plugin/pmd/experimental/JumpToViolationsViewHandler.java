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
package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 *
 * @author Christian Wulf
 *
 */
public class JumpToViolationsViewHandler extends AbstractHandler {

	/**
	 * Jump to a view event handler.
	 */
	public JumpToViolationsViewHandler() {
		super();
		// nothing to do here
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			return null;
		}
		final IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return null;
		}
		final IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			return null;
		}

		final IVerticalRulerInfo verticalRulerInfo = activeEditor.getAdapter(IVerticalRulerInfo.class);
		if (verticalRulerInfo != null) {
			// verticalRulerInfo.getControl().get
		}

		// TODO Auto-generated method stub
		return null;
	}

}
