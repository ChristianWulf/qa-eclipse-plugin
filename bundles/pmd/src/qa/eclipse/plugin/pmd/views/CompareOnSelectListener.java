package qa.eclipse.plugin.pmd.views;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.prefs.Preferences;

class CompareOnSelectListener extends SelectionAdapter {

	private final StructuredViewer structuredViewer;
	private final int selectedSortProperty;
	private final Preferences preferences;

	public CompareOnSelectListener(Preferences preferences, StructuredViewer structuredViewer,
			int selectedSortProperty) {
		this.preferences = preferences;
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

		preferences.putInt(PmdViolationsView.PREF_SORT_DIRECTION, sortOrder);
		preferences.putInt(PmdViolationsView.PREF_SORT_COLUMN_INDEX, (Integer) table.getSortColumn().getData());

		PmdViolationMarkerComparator comparator = (PmdViolationMarkerComparator) structuredViewer.getComparator();
		comparator.setSelectedSortProperty(selectedSortProperty);
		structuredViewer.refresh();
	}

}
