package pmd.eclipse.plugin.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;

import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.markers.PmdViolationMarker;

public class PmdViolationMarkerComparator extends ViewerComparator {

	public static final int SORT_PROP_PRIORITY = 0;
	public static final int SORT_PROP_RULENAME = 1;
	public static final int SORT_PROP_LINENUMBER = 2;
	public static final int SORT_PROP_RULESET = 3;

	private int selectedSortProperty;

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		TableViewer tableViewer = (TableViewer) viewer;
		Table table = tableViewer.getTable();
		int sortDirection = table.getSortDirection();
		if (sortDirection == SWT.NONE) {
			return 0;
		}

		PmdViolationMarker marker1 = (PmdViolationMarker) e1;
		PmdViolationMarker marker2 = (PmdViolationMarker) e2;

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
		case SORT_PROP_RULESET: {
			compareResult = compareRuleSet(marker1, marker2);
			break;
		}
		default: {
			compareResult = 0;
			String messageFormatString = "Cannot sort table. Don't know selected sort property '%d'";
			String message = String.format(messageFormatString, selectedSortProperty);
			PmdUIPlugin.getDefault().logWarning(message);
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
	private int comparePriority(PmdViolationMarker marker1, PmdViolationMarker marker2) {
		return -1 * Integer.compare(marker1.getPriority(), marker2.getPriority());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareRuleName(PmdViolationMarker marker1, PmdViolationMarker marker2) {
		return marker1.getRuleName().compareToIgnoreCase(marker2.getRuleName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareLineNumber(PmdViolationMarker marker1, PmdViolationMarker marker2) {
		return Integer.compare(marker1.getLineNumer(), marker2.getLineNumer());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareRuleSet(PmdViolationMarker marker1, PmdViolationMarker marker2) {
		return marker1.getRuleSetName().compareToIgnoreCase(marker2.getRuleSetName());
	}
}
