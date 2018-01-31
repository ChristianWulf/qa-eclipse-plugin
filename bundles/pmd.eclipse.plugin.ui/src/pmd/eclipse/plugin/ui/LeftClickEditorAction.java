package pmd.eclipse.plugin.ui;

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

import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.markers.PmdViolationMarker;
import pmd.eclipse.plugin.views.PmdViolationsView;

public class LeftClickEditorAction extends SelectMarkerRulerAction {

	private final IVerticalRulerInfo ruler;

	public LeftClickEditorAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
		this.ruler = ruler;
	}

	@Override
	public void run() {
		super.run();
		runAction();
	}

	// @Override
	// public void runWithEvent(Event event) {
	// super.run();
	// runAction();
	// }

	private void runAction() {
		IDocument document = getDocument();
		if (document == null) {
			return;
		}

		int activeLine = ruler.getLineOfLastMouseButtonActivity();

		IRegion lineRegion;
		try {
			lineRegion = document.getLineInformation(activeLine);
		} catch (BadLocationException e) {
			return;
		}
		AbstractMarkerAnnotationModel annotationModel = getAnnotationModel();
		Iterator<Annotation> annotations = annotationModel.getAnnotationIterator(lineRegion.getOffset(),
				lineRegion.getLength() + 1, true, true);

		while (annotations.hasNext()) {
			Annotation annotation = annotations.next();
			if (annotation.isMarkedDeleted()) {
				break;
			}

			if (annotation instanceof MarkerAnnotation) {
				MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				IMarker marker = markerAnnotation.getMarker();

				String markerType;
				try {
					markerType = marker.getType();
				} catch (CoreException e) {
					break;
				}

				if (markerType.equals(PmdMarkers.PMD_VIOLATION_MARKER)) {
					// TODO select marker line in violation view
					openViolationView(marker);
				}
			}

		}
	}

	private void openViolationView(IMarker marker) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

				try {
					// VIEW_ACTIVATE: focus view; VIEW_VISIBLE: do not focus view
					IViewPart viewPart = activePage.showView(PmdViolationsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
					PmdViolationsView violationView = (PmdViolationsView) viewPart;

					Object input = new PmdViolationMarker(marker);
					ISelection selection = new StructuredSelection(input);
					violationView.getTableViewer().setSelection(selection, true);
				} catch (PartInitException ex) {
					throw new IllegalStateException(ex);
				}
			}
		});
	}

}
