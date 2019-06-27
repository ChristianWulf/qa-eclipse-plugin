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
package qa.eclipse.plugin.pmd.view; // NOPMD (ExcessiveImports) UI programming

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.Preferences;

import net.sourceforge.pmd.RulePriority;

import qa.eclipse.plugin.bundles.common.ImageRegistryKeyUtils;
import qa.eclipse.plugin.bundles.common.StringUtils;
import qa.eclipse.plugin.bundles.common.markers.AbstractViolationMarker;
import qa.eclipse.plugin.bundles.common.view.AbstractViolationViewPart;
import qa.eclipse.plugin.bundles.common.view.CompareOnSelectListener;
import qa.eclipse.plugin.bundles.common.view.ESortProperty;
import qa.eclipse.plugin.bundles.common.view.ProjectNameViewerFilter;
import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.icons.FileIconDecorator;
import qa.eclipse.plugin.pmd.markers.PmdMarkersUtils;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;
import qa.eclipse.plugin.pmd.preference.PmdPreferences;

/**
 *
 * @author Christian Wulf
 *
 */
public class PmdViolationsView extends AbstractViolationViewPart {

	public static final String TOOL_NAME = "PMD";
	public static final String ID = "pmd.eclipse.plugin.view.PmdViolationsView";

	static final String PREF_SORT_DIRECTION = PmdViolationsView.ID + ".sortDirection";
	static final String PREF_SORT_COLUMN_INDEX = PmdViolationsView.ID + ".sortColumnIndex";
	static final String PREF_FILTER_PRIORITY = PmdViolationsView.ID + ".filterPriority";
	static final String PREF_COLUMN_ORDER = PmdViolationsView.ID + ".columnOrder";

	private static final String FILTERED_PART_NAME_FORMAT_STRING = PmdViolationsView.TOOL_NAME
			+ " Violations (%d of %d)";
	private static final String NUMBER_OF_PMD_VIOLATIONS_FORMAT_STRING = "Number of " + PmdViolationsView.TOOL_NAME
			+ " Violations: %d of %d";
	private static final int FILTER_INDEX_PRIORITY = 0;
	private static final int FILTER_INDEX_PROJECT = 1;

	// tutorial used from
	// http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html

	private final Map<Integer, String> keyByPriority = new ConcurrentHashMap<>();

	/**
	 * Create the violation view.
	 */
	public PmdViolationsView() {
		super(FileIconDecorator.ID, PmdUIPlugin.getDefault(), PmdPreferences.INSTANCE.getEclipseScopedPreferences());

		final IEclipsePreferences defaultPreferences = PmdPreferences.INSTANCE.getDefaultPreferences();
		defaultPreferences.putInt(PmdViolationsView.PREF_SORT_DIRECTION, SWT.DOWN);
		defaultPreferences.putInt(PmdViolationsView.PREF_SORT_COLUMN_INDEX, SWT.DOWN);

		this.viewerFilters[PmdViolationsView.FILTER_INDEX_PRIORITY] = new PmdPriorityViewerFilter();
		this.viewerFilters[PmdViolationsView.FILTER_INDEX_PROJECT] = new ProjectNameViewerFilter<PmdViolationMarker>(); // NOPMD

		this.keyByPriority.put(RulePriority.HIGH.getPriority(), "pmd.high.clvertical");
		this.keyByPriority.put(RulePriority.MEDIUM_HIGH.getPriority(), "pmd.mediumhigh.clvertical");
		this.keyByPriority.put(3, "pmd.medium.clvertical");
		this.keyByPriority.put(4, "pmd.mediumlow.clvertical");
		this.keyByPriority.put(5, "pmd.low.clvertical");
	}

	@Override
	protected void createPriorityDropDownItems(final TableCombo tableCombo) {
		String imageRegistryKey;
		Image image;
		TableItem tableItem;

		for (final RulePriority rulePriority : RulePriority.values()) {
			imageRegistryKey = ImageRegistryKeyUtils.getPriorityColumnKeyByPriority("pmd", rulePriority.getPriority());
			image = PmdUIPlugin.getDefault().getImageRegistry().get(imageRegistryKey);
			tableItem = new TableItem(tableCombo.getTable(), SWT.NONE);
			tableItem.setText("At least " + rulePriority.getName());
			tableItem.setImage(image);
			tableItem.setData(rulePriority);
		}
	}

	@Override
	protected void addToolBarButtons() {
		// IActionBars actionBars = getViewSite().getActionBars();
		// IToolBarManager toolBar = actionBars.getToolBarManager();
		//
		// // IAction action = new
		// toolBar.add(action);
	}

	@Override
	protected int loadSavedFilterPriority(final TableCombo tableCombo) {
		final int defaultPriority = RulePriority.LOW.getPriority();
		int filterPriority = this.viewPreferences.getInt(PmdViolationsView.PREF_FILTER_PRIORITY, defaultPriority);
		if ((filterPriority < 0) || (filterPriority >= tableCombo.getItemCount())) {
			filterPriority = defaultPriority;
		}
		return filterPriority;
	}

	@Override
	protected int loadSavedSortDirection() {
		return this.viewPreferences.getInt(PmdViolationsView.PREF_SORT_DIRECTION,
				SWT.NONE);
	}

	@Override
	protected TableColumn loadSavedSortColumn() {
		final Integer columnIndex = this.viewPreferences.getInt(PmdViolationsView.PREF_SORT_COLUMN_INDEX, 0);
		TableColumn savedSortColumn;
		try {
			savedSortColumn = this.tableViewer.getTable().getColumn(columnIndex);
		} catch (final IllegalArgumentException e) {
			savedSortColumn = this.tableViewer.getTable().getColumn(0);
		}
		return savedSortColumn;
	}

	@Override
	protected void createColumns() {
		final Listener columnMovedListener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.type == SWT.Move) {
					final int[] columnOrder = PmdViolationsView.this.tableViewer.getTable().getColumnOrder();
					final String columnOrderEncoded = StringUtils.join(columnOrder, ',');
					PmdViolationsView.this.viewPreferences.put(PmdViolationsView.PREF_COLUMN_ORDER,
							columnOrderEncoded);
				}
			}
		};

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				final PmdViolationMarker violationMarker = (PmdViolationMarker) element;
				final int pmdPriority = violationMarker.getPriority();
				final String imageRegistryKey = ImageRegistryKeyUtils.getPriorityColumnKeyByPriority("pmd", pmdPriority);
				final Image image = PmdUIPlugin.getDefault().getImageRegistry().get(imageRegistryKey);
				return image;
			}

			@Override
			public String getText(final Object element) {
				return "";
			}

			@Override
			public Image getToolTipImage(final Object object) {
				return this.getImage(object);
			}

			@Override
			public String getToolTipText(final Object element) {
				final PmdViolationMarker violationMarker = (PmdViolationMarker) element;
				final int pmdPriority = violationMarker.getPriority();
				final RulePriority pmdRulePriority = RulePriority.valueOf(pmdPriority);
				return pmdRulePriority.toString();
			}
		});
		this.createColumn(tableViewerColumn, "Priority", 50, ESortProperty.SORT_PROP_PRIORITY, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return marker.getRuleName();
			}
		});
		this.createColumn(tableViewerColumn, "Rule name", 200, ESortProperty.SORT_PROP_RULE_NAME, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return marker.getMessage();
			}
		});
		this.createColumn(tableViewerColumn, "Violation message", 400, ESortProperty.SORT_PROP_MESSAGE, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return String.valueOf(marker.getProjectName());
			}
		});
		this.createColumn(tableViewerColumn, "Project", 100, ESortProperty.SORT_PROP_PROJECTNAME, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return String.valueOf(marker.getLineNumer());
			}
		});
		this.createColumn(tableViewerColumn, "Line", 50, ESortProperty.SORT_PROP_LINENUMBER, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return marker.getRuleSetName();
			}
		});
		this.createColumn(tableViewerColumn, "Rule Set", 200, ESortProperty.SORT_PROP_LINENUMBER, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return marker.getDirectoryPath();
			}
		});
		this.createColumn(tableViewerColumn, "Directory path", 200, ESortProperty.SORT_PROP_PATH, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final AbstractViolationMarker marker = (AbstractViolationMarker) element;
				return marker.getFileName();
			}
		});
		this.createColumn(tableViewerColumn, "File name", 200, ESortProperty.SORT_PROP_FILENAME, columnMovedListener);
	}

	/**
	 * Create a column with a selection listener.
	 *
	 * @param tableViewerColumn
	 *            the table column viewer
	 * @param label
	 *            the label of the column
	 * @param width
	 *            the initial width
	 * @param sortByProperty
	 *            the sort by property for this column
	 * @param columnMovedListener
	 *            column listener
	 */
	private void createColumn(final TableViewerColumn tableViewerColumn, final String label, final int width, final ESortProperty sortByProperty,
			final Listener columnMovedListener) {
		final TableColumn column = tableViewerColumn.getColumn();
		column.setText(label); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(width);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				sortByProperty, PmdViolationsView.PREF_SORT_DIRECTION, PmdViolationsView.PREF_SORT_COLUMN_INDEX));
		column.addListener(SWT.Move, columnMovedListener);
	}

	/**
	 * @see <a href=
	 *      "https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2FresAdv_events.htm"
	 *      >Link to type-based switch</a>
	 *
	 * @see <a href=
	 *      "https://stackoverflow.com/questions/10501966/add-change-listener-on-marker">Link
	 *      to findMarkerDeltas</a>
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		final IMarkerDelta[] markerDeltas = event.findMarkerDeltas(PmdMarkersUtils.ABSTRACT_PMD_VIOLATION_MARKER, true);
		// do not unnecessarily collect all PMD markers again if no marker of this
		// resource was changed
		if (markerDeltas.length == 0) {
			return;
		}

		this.updateView();
	}

	@Override
	protected void updateView() {
		try {
			final IMarker[] updatedMarkers = PmdMarkersUtils.findAllMarkers();

			final List<PmdViolationMarker> pmdViolationMarkers = new ArrayList<>();

			for (final IMarker marker : updatedMarkers) {
				final PmdViolationMarker pmdViolationMarker = new PmdViolationMarker(marker);
				pmdViolationMarkers.add(pmdViolationMarker);
			}

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// setInput must be set first so that numFilteredViolations is correct
					PmdViolationsView.this.tableViewer.setInput(pmdViolationMarkers);
					PmdViolationsView.this.updateTitleAndLabel(pmdViolationMarkers);
				}
			});
		} catch (final CoreException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Internal Error", e.getLocalizedMessage());
		}

	}

	private void updateTitleAndLabel(final List<?> violationMarkers) {
		final int numFilteredViolations = this.tableViewer.getTable().getItemCount();
		final int numViolations = violationMarkers.size();
		this.updateTabTitle(numFilteredViolations, numViolations);
		this.updateNumViolationsLabel(numFilteredViolations, numViolations);
	}

	private void updateTabTitle(final int numFilteredViolations, final int numViolations) {
		final String newPartName = String.format(PmdViolationsView.FILTERED_PART_NAME_FORMAT_STRING,
				numFilteredViolations, numViolations);
		this.setPartName(newPartName);
	}

	private void updateNumViolationsLabel(final int numFilteredViolations, final int numViolations) {
		final String text = String.format(PmdViolationsView.NUMBER_OF_PMD_VIOLATIONS_FORMAT_STRING,
				numFilteredViolations, numViolations);
		this.numViolationsLabel.setText(text);
		this.numViolationsLabel.getParent().layout(); // update label
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		final PmdViolationMarker pmdViolationMarker = (PmdViolationMarker) selection.getFirstElement();
		if (pmdViolationMarker == null) {
			return;
		}

		try {
			IDE.openEditor(this.getSite().getPage(), pmdViolationMarker.getMarker());
		} catch (final PartInitException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void filterByPriority(final int lowestPriority) {
		final PmdPriorityViewerFilter priorityFilter = (PmdPriorityViewerFilter) this.viewerFilters[PmdViolationsView.FILTER_INDEX_PRIORITY];
		priorityFilter.setLowestPriority(lowestPriority);

		final IEclipsePreferences preferences = PmdPreferences.INSTANCE.getEclipseEditorPreferences();
		// highest priority (here: 1) should always be displayed
		for (int i = 1; i < (lowestPriority + 1); i++) {
			final String key = this.keyByPriority.get(i);
			preferences.putBoolean(key, true);
		}
		for (int i = lowestPriority + 1; i < (5 + 1); i++) {
			final String key = this.keyByPriority.get(i);
			preferences.putBoolean(key, false);
		}
	}

	@Override
	protected SelectionListener getPrioritySelectionListener() {
		return new PriorityFilterSelectionAdapter(PmdViolationsView.PREF_FILTER_PRIORITY, this.tableViewer, this.viewPreferences);
	}

	@Override
	protected String getToolName() {
		return "PMD";
	}

	@Override
	protected String getColumnOrder() {
		return PmdViolationsView.PREF_COLUMN_ORDER;
	}

	private class PriorityFilterSelectionAdapter extends SelectionAdapter {

		private final String preferenceFilterPriority;
		private final TableViewer tableViewer;
		private final Preferences viewPreferences;

		public PriorityFilterSelectionAdapter(final String preferenceFilterPriority, final TableViewer tableViewer, final Preferences viewPreferences) {
			super();
			this.preferenceFilterPriority = preferenceFilterPriority;
			this.tableViewer = tableViewer;
			this.viewPreferences = viewPreferences;
		}

		@Override
		public void widgetSelected(final SelectionEvent event) {
			final TableCombo source = (TableCombo) event.getSource();
			final int lowestPriority = source.getSelectionIndex() + 1;
			PmdViolationsView.this.filterByPriority(lowestPriority);
			this.tableViewer.refresh(false);
			this.viewPreferences.putInt(this.preferenceFilterPriority, lowestPriority); // save
			// filter
			// setting

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					PmdViolationsView.this.updateTitleAndLabel((List<?>) PriorityFilterSelectionAdapter.this.tableViewer.getInput());
				}
			});
		}
	}
}
