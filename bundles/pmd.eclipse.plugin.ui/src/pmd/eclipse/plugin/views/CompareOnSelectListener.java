package pmd.eclipse.plugin.views;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

class CompareOnSelectListener extends SelectionAdapter {

	private final StructuredViewer structuredViewer;
	private final int selectedSortProperty;

	public CompareOnSelectListener(StructuredViewer structuredViewer, int selectedSortProperty) {
		this.structuredViewer = structuredViewer;
		this.selectedSortProperty = selectedSortProperty;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		TableColumn selectedColumn = (TableColumn) e.getSource();
		Table table = selectedColumn.getParent();

		// toggle sort order
		int sortOrder = (table.getSortDirection() == SWT.UP) ? SWT.DOWN : SWT.TOP;
		table.setSortDirection(sortOrder);
		table.setSortColumn(selectedColumn);

		PmdViolationMarkerComparator comparator = (PmdViolationMarkerComparator) structuredViewer.getComparator();
		comparator.setSelectedSortProperty(selectedSortProperty);
		structuredViewer.refresh();
	}

}
