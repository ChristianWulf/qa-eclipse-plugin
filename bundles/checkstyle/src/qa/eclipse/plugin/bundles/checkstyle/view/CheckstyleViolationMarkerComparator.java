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
package qa.eclipse.plugin.bundles.checkstyle.view;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;

import qa.eclipse.plugin.bundles.checkstyle.CheckstyleUIPlugin;
import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleViolationMarker;

/**
 *
 * @author Christian Wulf
 *
 */
class CheckstyleViolationMarkerComparator extends ViewerComparator {

	public static final int SORT_PROP_PRIORITY = 0;
	public static final int SORT_PROP_CHECK_NAME = 1;
	public static final int SORT_PROP_LINENUMBER = 2;
	public static final int SORT_PROP_PROJECTNAME = 3;
	public static final int SORT_PROP_CHECK_PACKAGE_NAME = 4;
	public static final int SORT_PROP_VIOLATION_MSG = 5;

	private int selectedSortProperty;

	public CheckstyleViolationMarkerComparator() {
		super();
	}

	@Override
	public int compare(final Viewer viewer, final Object object1, final Object object2) {
		final TableViewer tableViewer = (TableViewer) viewer;
		final Table table = tableViewer.getTable();
		final int sortDirection = table.getSortDirection();
		if (sortDirection == SWT.NONE) {
			return 0;
		}

		final CheckstyleViolationMarker marker1 = (CheckstyleViolationMarker) object1;
		final CheckstyleViolationMarker marker2 = (CheckstyleViolationMarker) object2;

		int compareResult;
		switch (this.selectedSortProperty) {
		case SORT_PROP_PRIORITY: {
			compareResult = this.comparePriority(marker1, marker2);
			break;
		}
		case SORT_PROP_CHECK_NAME: {
			compareResult = this.compareCheckName(marker1, marker2);
			break;
		}
		case SORT_PROP_LINENUMBER: {
			compareResult = this.compareLineNumber(marker1, marker2);
			break;
		}
		case SORT_PROP_PROJECTNAME: {
			compareResult = this.compareProjectName(marker1, marker2);
			break;
		}
		case SORT_PROP_CHECK_PACKAGE_NAME: {
			compareResult = this.compareCheckPackageName(marker1, marker2);
			break;
		}
		case SORT_PROP_VIOLATION_MSG: {
			compareResult = this.compareViolationMessageText(marker1, marker2);
			break;
		}
		default: {
			compareResult = 0;
			final String messageFormatString = "Cannot sort table. Don't know selected sort property '%d'";
			final String message = String.format(messageFormatString, this.selectedSortProperty);
			CheckstyleUIPlugin.getDefault().logWarning(message);
			break;
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
	private int comparePriority(final CheckstyleViolationMarker marker1, final CheckstyleViolationMarker marker2) {
		return -1 * Integer.compare(marker1.getSeverityLevelIndex(), marker2.getSeverityLevelIndex());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareCheckName(final CheckstyleViolationMarker marker1, final CheckstyleViolationMarker marker2) {
		return marker1.getCheckName().compareToIgnoreCase(marker2.getCheckName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareLineNumber(final CheckstyleViolationMarker marker1, final CheckstyleViolationMarker marker2) {
		return Integer.compare(marker1.getLineNumer(), marker2.getLineNumer());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareProjectName(final CheckstyleViolationMarker marker1, final CheckstyleViolationMarker marker2) {
		return marker1.getProjectName().compareToIgnoreCase(marker2.getProjectName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareCheckPackageName(final CheckstyleViolationMarker marker1, final CheckstyleViolationMarker marker2) {
		return marker1.getCheckPackageName().compareToIgnoreCase(marker2.getCheckPackageName());
	}

	/**
	 * Assumed sort order is SWT.UP.
	 */
	private int compareViolationMessageText(final CheckstyleViolationMarker marker1, final CheckstyleViolationMarker marker2) {
		return marker1.getMessage().compareToIgnoreCase(marker2.getMessage());
	}

}
