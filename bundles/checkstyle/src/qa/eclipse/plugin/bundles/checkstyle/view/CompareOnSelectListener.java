package qa.eclipse.plugin.bundles.checkstyle.view;

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
	private final String sortOrderKey;
	private final String sortColumnIndex;

	public CompareOnSelectListener(Preferences preferences, StructuredViewer structuredViewer,
			int selectedSortProperty) {
		this.preferences = preferences;
		this.structuredViewer = structuredViewer;
		this.selectedSortProperty = selectedSortProperty;
		this.sortOrderKey = CheckstyleViolationsView.PREF_SORT_DIRECTION;
		this.sortColumnIndex = CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		TableColumn selectedColumn = (TableColumn) e.getSource();
		Table table = selectedColumn.getParent();

		// toggle sort order
		int sortOrder = (table.getSortDirection() == SWT.UP) ? SWT.DOWN : SWT.TOP;
		table.setSortDirection(sortOrder);
		table.setSortColumn(selectedColumn);

		preferences.putInt(sortOrderKey, sortOrder);
		preferences.putInt(sortColumnIndex, (Integer) table.getSortColumn().getData());

		CheckstyleViolationMarkerComparator comparator = (CheckstyleViolationMarkerComparator) structuredViewer
				.getComparator();
		comparator.setSelectedSortProperty(selectedSortProperty);
		structuredViewer.refresh();
	}

}
