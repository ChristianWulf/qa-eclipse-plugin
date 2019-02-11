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

	public CompareOnSelectListener(final Preferences preferences, final StructuredViewer structuredViewer,
			final int selectedSortProperty) {
		this.preferences = preferences;
		this.structuredViewer = structuredViewer;
		this.selectedSortProperty = selectedSortProperty;
		this.sortOrderKey = CheckstyleViolationsView.PREF_SORT_DIRECTION;
		this.sortColumnIndex = CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX;
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		final TableColumn selectedColumn = (TableColumn) e.getSource();
		final Table table = selectedColumn.getParent();

		// toggle sort order
		final int sortOrder = (table.getSortDirection() == SWT.UP) ? SWT.DOWN : SWT.TOP;
		table.setSortDirection(sortOrder);
		table.setSortColumn(selectedColumn);

		this.preferences.putInt(this.sortOrderKey, sortOrder);
		this.preferences.putInt(this.sortColumnIndex, (Integer) table.getSortColumn().getData());

		final CheckstyleViolationMarkerComparator comparator = (CheckstyleViolationMarkerComparator) this.structuredViewer
				.getComparator();
		comparator.setSelectedSortProperty(this.selectedSortProperty);
		this.structuredViewer.refresh();
	}

}
