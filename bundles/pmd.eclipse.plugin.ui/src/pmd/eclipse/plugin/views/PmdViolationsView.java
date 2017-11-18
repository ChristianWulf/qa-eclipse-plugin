package pmd.eclipse.plugin.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

public class PmdViolationsView extends ViewPart implements ISelectionChangedListener {

	// tutorial used from
	// http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html

	private TableViewer tableViewer;

	@Override
	public void createPartControl(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER);
		tableViewer.setUseHashlookup(true);

		TableViewerColumn tableViewerColumn;
		TableColumn column;

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("Priority"); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		column.setMoveable(true);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("Rule name");
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		column.setMoveable(true);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("Directory path");
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		column.setMoveable(true);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("File name");
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		column.setMoveable(true);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("Line");
		column.setResizable(true);
		column.setAlignment(SWT.RIGHT);
		column.setMoveable(true);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("Violation message");
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		column.setMoveable(true);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		// column.setLabelProvider(labelProvider);
		column = tableViewerColumn.getColumn();
		column.setText("Rule set");
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		column.setMoveable(true);

		// configure table
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		// on selection: opens the corresponding file in the proper editor, jumps to the
		// line, and selects it
		tableViewer.addSelectionChangedListener(this);
		// arrow down symbol: opens a menu with five priority-based filters
		ViewerFilter viewerFilter = null;// FIXME create filter
		tableViewer.addFilter(viewerFilter);
		// interprets the input and transforms it into rows
		tableViewer.setContentProvider(new ArrayContentProvider());
		// input is an array
		IMarker[] markers = null; // FIXME pass marker to this variable
		tableViewer.setInput(markers);
		// the comparator depends on the selected column (ascending or descending order)
		ViewerComparator comparator = null; // FIXME create comparator
		tableViewer.setComparator(comparator);

		// TODO Auto-generated method stub
	}

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// TODO Auto-generated method stub

	}

}
