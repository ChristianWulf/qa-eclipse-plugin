package qa.eclipse.plugin.bundles.checkstyle.view;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;

import qa.eclipse.plugin.bundles.checkstyle.Activator;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;

class CheckstyleViolationMarkerComparator extends ViewerComparator {

	public static final int SORT_PROP_PRIORITY = 0;
	public static final int SORT_PROP_RULENAME = 1;
	public static final int SORT_PROP_LINENUMBER = 2;
//	public static final int SORT_PROP_RULESET = 3;
	public static final int SORT_PROP_PROJECTNAME = 4;

	private int selectedSortProperty;

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		TableViewer tableViewer = (TableViewer) viewer;
		Table table = tableViewer.getTable();
		int sortDirection = table.getSortDirection();
		if (sortDirection == SWT.NONE) {
			return 0;
		}

		CheckstyleViolationMarker marker1 = (CheckstyleViolationMarker) e1;
		CheckstyleViolationMarker marker2 = (CheckstyleViolationMarker) e2;

		int compareResult;
		switch (selectedSortProperty) {
		case SORT_PROP_PRIORITY: {
			compareResult = comparePriority(marker1, marker2);
			break;
		}
		case SORT_PROP_RULENAME: {
			compareResult = compareRuleName(marker1, marker2);
			break;
		}
		case SORT_PROP_LINENUMBER: {
			compareResult = compareLineNumber(marker1, marker2);
			break;
		}
//		case SORT_PROP_RULESET: {
//			compareResult = compareRuleSet(marker1, marker2);
//			break;
//		}
		case SORT_PROP_PROJECTNAME: {
			compareResult = compareProjectName(marker1, marker2);
			break;
		}
		default: {
			compareResult = 0;
			String messageFormatString = "Cannot sort table. Don't know selected sort property '%d'";
			String message = String.format(messageFormatString, selectedSortProperty);
			Activator.getDefault().logWarning(message);
		}
		}

		// We assume a sort order of SWT.UP for the compare-Methods.
		// Otherwise, switch the sign to represent SWT.DOWN.
		if (sortDirection == SWT.DOWN) {
			compareResult *= -1;
		}

		return compareResult;
	}

	public void setSelectedSortProperty(int selectedSortProperty) {
		this.selectedSortProperty = selectedSortProperty;
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int comparePriority(CheckstyleViolationMarker marker1, CheckstyleViolationMarker marker2) {
		return -1 * Integer.compare(marker1.getSeverityLevelIndex(), marker2.getSeverityLevelIndex());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareRuleName(CheckstyleViolationMarker marker1, CheckstyleViolationMarker marker2) {
		return marker1.getModuleName().compareToIgnoreCase(marker2.getModuleName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareLineNumber(CheckstyleViolationMarker marker1, CheckstyleViolationMarker marker2) {
		return Integer.compare(marker1.getLineNumer(), marker2.getLineNumer());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareProjectName(CheckstyleViolationMarker marker1, CheckstyleViolationMarker marker2) {
		return marker1.getProjectName().compareToIgnoreCase(marker2.getProjectName());
	}
}
