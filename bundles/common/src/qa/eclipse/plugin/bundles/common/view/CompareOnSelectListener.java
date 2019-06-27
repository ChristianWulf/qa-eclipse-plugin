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
package qa.eclipse.plugin.bundles.common.view;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.prefs.Preferences;

/**
 *
 * @author Christian Wulf
 *
 */
public class CompareOnSelectListener extends SelectionAdapter {

	private final StructuredViewer structuredViewer;
	private final ESortProperty selectedSortProperty;
	private final Preferences preferences;
	private final String sortOrderKey;
	private final String sortColumnIndex;

	/**
	 * Create a listener.
	 *
	 * @param preferences
	 *            preferences
	 * @param structuredViewer
	 *            viewer (table) for the list of markers
	 * @param selectedSortProperty
	 *            the property by which the table is sorted
	 * @param sortOrderKey
	 *            the sort order key
	 * @param sortColumnIndex
	 *            the sort column index
	 */
	public CompareOnSelectListener(final Preferences preferences, final StructuredViewer structuredViewer,
			final ESortProperty selectedSortProperty, final String sortOrderKey, final String sortColumnIndex) {
		super();
		this.preferences = preferences;
		this.structuredViewer = structuredViewer;
		this.selectedSortProperty = selectedSortProperty;
		this.sortOrderKey = sortOrderKey;
		this.sortColumnIndex = sortColumnIndex;
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		final TableColumn selectedColumn = (TableColumn) event.getSource();
		final Table table = selectedColumn.getParent();

		// toggle sort order
		final int sortOrder = (table.getSortDirection() == SWT.UP) ? SWT.DOWN : SWT.TOP;
		table.setSortDirection(sortOrder);
		table.setSortColumn(selectedColumn);

		this.preferences.putInt(this.sortOrderKey, sortOrder);
		this.preferences.putInt(this.sortColumnIndex, (Integer) table.getSortColumn().getData());

		final ViolationMarkerComparator comparator = (ViolationMarkerComparator) this.structuredViewer
				.getComparator();
		comparator.setSelectedSortProperty(this.selectedSortProperty);
		this.structuredViewer.refresh();
	}

}
