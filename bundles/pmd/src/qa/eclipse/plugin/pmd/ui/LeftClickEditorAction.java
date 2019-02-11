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

import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

import qa.eclipse.plugin.pmd.markers.PmdMarkers;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;
import qa.eclipse.plugin.pmd.views.PmdViolationsView;

public class LeftClickEditorAction extends SelectMarkerRulerAction {

	private final IVerticalRulerInfo ruler;

	public LeftClickEditorAction(final ResourceBundle bundle, final String prefix, final ITextEditor editor,
			final IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
		this.ruler = ruler;
	}

	@Override
	public void run() {
		super.run();
		this.runAction();
	}

	private void runAction() {
		final IDocument document = this.getDocument();
		if (document == null) {
			return;
		}

		final int activeLine = this.ruler.getLineOfLastMouseButtonActivity();

		final IRegion lineRegion;
		try {
			lineRegion = document.getLineInformation(activeLine);
		} catch (final BadLocationException e) {
			return;
		}
		final AbstractMarkerAnnotationModel annotationModel = this.getAnnotationModel();
		final Iterator<Annotation> annotations = annotationModel.getAnnotationIterator(lineRegion.getOffset(),
				lineRegion.getLength() + 1, true, true);

		while (annotations.hasNext()) {
			final Annotation annotation = annotations.next();
			if (annotation.isMarkedDeleted()) {
				break;
			}

			if (annotation instanceof MarkerAnnotation) {
				final MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				final IMarker marker = markerAnnotation.getMarker();

				final String markerType;
				try {
					markerType = marker.getType();
				} catch (final CoreException e) {
					break;
				}

				if (markerType.startsWith(PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER)) {
					this.openViolationView(marker);
				}
			}

		}
	}

	private void openViolationView(final IMarker marker) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

				try {
					// VIEW_ACTIVATE: focus view; VIEW_VISIBLE: do not focus view
					final IViewPart viewPart = activePage.showView(PmdViolationsView.ID, null,
							IWorkbenchPage.VIEW_VISIBLE);
					final PmdViolationsView violationView = (PmdViolationsView) viewPart;

					final Object input = new PmdViolationMarker(marker);
					final ISelection selection = new StructuredSelection(input);
					violationView.getTableViewer().setSelection(selection, true);
				} catch (final PartInitException ex) {
					throw new IllegalStateException(ex);
				}
			}
		});
	}

}
