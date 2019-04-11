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
package qa.eclipse.plugin.pmd.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;

import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

class PmdViolationMarkerComparator extends ViewerComparator {

	public static final int SORT_PROP_PRIORITY = 0;
	public static final int SORT_PROP_RULENAME = 1;
	public static final int SORT_PROP_LINENUMBER = 2;
	public static final int SORT_PROP_RULESET = 3;
	public static final int SORT_PROP_PROJECTNAME = 4;
	public static final int SORT_PROP_VIOLATION_MSG = 5;

	private int selectedSortProperty;

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		final TableViewer tableViewer = (TableViewer) viewer;
		final Table table = tableViewer.getTable();
		final int sortDirection = table.getSortDirection();
		if (sortDirection == SWT.NONE) {
			return 0;
		}

		final PmdViolationMarker marker1 = (PmdViolationMarker) e1;
		final PmdViolationMarker marker2 = (PmdViolationMarker) e2;

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
		case SORT_PROP_PROJECTNAME: {
			compareResult = compareProjectName(marker1, marker2);
			break;
		}
		case SORT_PROP_VIOLATION_MSG: {
			compareResult = compareViolationMessage(marker1, marker2);
			break;
		}
		default: {
			compareResult = 0;
			final String messageFormatString = "Cannot sort table. Don't know selected sort property '%d'";
			final String message = String.format(messageFormatString, selectedSortProperty);
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

	public void setSelectedSortProperty(final int selectedSortProperty) {
		this.selectedSortProperty = selectedSortProperty;
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int comparePriority(final PmdViolationMarker marker1, final PmdViolationMarker marker2) {
		return -1 * Integer.compare(marker1.getPriority(), marker2.getPriority());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareRuleName(final PmdViolationMarker marker1, final PmdViolationMarker marker2) {
		return marker1.getRuleName().compareToIgnoreCase(marker2.getRuleName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareLineNumber(final PmdViolationMarker marker1, final PmdViolationMarker marker2) {
		return Integer.compare(marker1.getLineNumer(), marker2.getLineNumer());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareRuleSet(final PmdViolationMarker marker1, final PmdViolationMarker marker2) {
		return marker1.getRuleSetName().compareToIgnoreCase(marker2.getRuleSetName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareProjectName(final PmdViolationMarker marker1, final PmdViolationMarker marker2) {
		return marker1.getProjectName().compareToIgnoreCase(marker2.getProjectName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareViolationMessage(final PmdViolationMarker marker1, final PmdViolationMarker marker2) {
		return marker1.getMessage().compareToIgnoreCase(marker2.getMessage());
	}
}
