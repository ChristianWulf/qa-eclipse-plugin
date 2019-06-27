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
package qa.eclipse.plugin.bundles.checkstyle.view; // NOPMD (ExcessiveImports) UI programming

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.CheckstyleUIPlugin;
import qa.eclipse.plugin.bundles.checkstyle.icons.FileIconDecorator;
import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleMarkersUtils;
import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleViolationMarker;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;
import qa.eclipse.plugin.bundles.common.ImageRegistryKeyUtils;
import qa.eclipse.plugin.bundles.common.StringUtils;
import qa.eclipse.plugin.bundles.common.markers.AbstractViolationMarker;
import qa.eclipse.plugin.bundles.common.view.AbstractViolationViewPart;
import qa.eclipse.plugin.bundles.common.view.CompareOnSelectListener;
import qa.eclipse.plugin.bundles.common.view.ESortProperty;
import qa.eclipse.plugin.bundles.common.view.ProjectNameViewerFilter;

/**
 *
 * @author Christian Wulf
 *
 */
public class CheckstyleViolationsView extends AbstractViolationViewPart {

	public static final String TOOL_NAME = "Checkstyle";
	public static final String ID = "qa.eclipse.plugin.checkstyle.view";

	static final String PREF_SORT_DIRECTION = CheckstyleViolationsView.ID + ".sortDirection";
	static final String PREF_SORT_COLUMN_INDEX = CheckstyleViolationsView.ID + ".sortColumnIndex";
	static final String PREF_FILTER_PRIORITY = CheckstyleViolationsView.ID + ".filterPriority";
	static final String PREF_COLUMN_ORDER = CheckstyleViolationsView.ID + ".columnOrder";

	private static final String FILTERED_PART_NAME_FORMAT_STRING = CheckstyleViolationsView.TOOL_NAME
			+ " Violations (%d of %d)";
	private static final String NUMBER_OF_CHECKSTYLE_VIOLATIONS_FORMAT_STRING = "Number of "
			+ CheckstyleViolationsView.TOOL_NAME + " Violations: %d of %d";
	private static final int FILTER_INDEX_PRIORITY = 0;
	private static final int FILTER_INDEX_PROJECT = 1;

	private static final String KEY_PREFIX = "vertical.checkstyle.";

	// tutorial used from
	// http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html

	private final Map<Integer, String> keyByPriority = new ConcurrentHashMap<>();

	/**
	 * Create the violation view.
	 */
	public CheckstyleViolationsView() {
		super(FileIconDecorator.ID, CheckstyleUIPlugin.getDefault(), CheckstylePreferences.INSTANCE.getEclipseScopedPreferences());

		final IEclipsePreferences defaultPreferences = CheckstylePreferences.INSTANCE.getDefaultPreferences();
		defaultPreferences.putInt(CheckstyleViolationsView.PREF_SORT_DIRECTION, SWT.DOWN);
		defaultPreferences.putInt(CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX, SWT.DOWN);

		this.viewerFilters[CheckstyleViolationsView.FILTER_INDEX_PRIORITY] = new CheckstylePriorityViewerFilter();
		this.viewerFilters[CheckstyleViolationsView.FILTER_INDEX_PROJECT] = new ProjectNameViewerFilter<CheckstyleViolationMarker>(); // NOPMD

		this.keyByPriority.put(SeverityLevel.ERROR.ordinal(), CheckstyleViolationsView.KEY_PREFIX + "error");
		this.keyByPriority.put(SeverityLevel.WARNING.ordinal(),
				CheckstyleViolationsView.KEY_PREFIX + "warning");
		this.keyByPriority.put(SeverityLevel.INFO.ordinal(), CheckstyleViolationsView.KEY_PREFIX + "info");
		this.keyByPriority.put(SeverityLevel.IGNORE.ordinal(), CheckstyleViolationsView.KEY_PREFIX + "ignore");
	}

	@Override
	protected void createPriorityDropDownItems(final TableCombo tableCombo) {
		String imageRegistryKey;
		Image image;
		TableItem tableItem;

		final SeverityLevel[] severityLevels = SeverityLevel.values().clone();

		// highest priority (here: SeverityLevel.ERROR = 3) should be displayed as the
		// top-most item
		Arrays.sort(severityLevels, new Comparator<SeverityLevel>() {
			@Override
			public int compare(final SeverityLevel o1, final SeverityLevel o2) {
				return -1 * o1.compareTo(o2);
			}
		});

		for (final SeverityLevel severityLevel : severityLevels) {
			imageRegistryKey = ImageRegistryKeyUtils.getPriorityColumnKeyByPriority("checkstyle", severityLevel.ordinal());
			image = CheckstyleUIPlugin.getDefault().getImageRegistry().get(imageRegistryKey);
			tableItem = new TableItem(tableCombo.getTable(), SWT.NONE);
			tableItem.setText("At least " + severityLevel.name());
			tableItem.setImage(image);
			tableItem.setData(severityLevel);
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
		final int defaultPriorty = SeverityLevel.IGNORE.ordinal();
		int filterPriority = this.viewPreferences.getInt(CheckstyleViolationsView.PREF_FILTER_PRIORITY, defaultPriorty);
		if (filterPriority < 0 || filterPriority >= tableCombo.getItemCount()) {
			filterPriority = defaultPriorty;
		}
		return filterPriority;
	}

	@Override
	protected int loadSavedSortDirection() {
		return this.viewPreferences.getInt(CheckstyleViolationsView.PREF_SORT_DIRECTION,
				SWT.NONE);
	}

	@Override
	protected TableColumn loadSavedSortColumn() {
		final Integer columnIndex = this.viewPreferences.getInt(CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX, 0);
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
					final int[] columnOrder = CheckstyleViolationsView.this.tableViewer.getTable().getColumnOrder(); // NOPMD cause by API
					final String columnOrderEncoded = StringUtils.join(columnOrder, ',');
					CheckstyleViolationsView.this.viewPreferences.put(CheckstyleViolationsView.PREF_COLUMN_ORDER, // NOPMD cause by API
							columnOrderEncoded);
				}
			}
		};

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				final CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) element;
				final int priority = violationMarker.getPriority();
				final String imageRegistryKey = ImageRegistryKeyUtils.getPriorityColumnKeyByPriority("checkstyle", priority);
				final Image image = CheckstyleUIPlugin.getDefault().getImageRegistry().get(imageRegistryKey);
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
				final CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) element;
				final int severityLevelIndex = violationMarker.getPriority();
				final SeverityLevel severityLevel = SeverityLevel.values()[severityLevelIndex];
				return severityLevel.name();
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
		this.createColumn(tableViewerColumn, "Check name", 200, ESortProperty.SORT_PROP_RULE_NAME, columnMovedListener);

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
		this.createColumn(tableViewerColumn, "Check package", 200, ESortProperty.SORT_PROP_RULESET_NAME, columnMovedListener);

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
				sortByProperty, CheckstyleViolationsView.PREF_SORT_DIRECTION, CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX));
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
		final IMarkerDelta[] markerDeltas = event
				.findMarkerDeltas(CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER, true);
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
			final IMarker[] updatedMarkers = CheckstyleMarkersUtils.findAllMarkers();

			final List<CheckstyleViolationMarker> violationMarkers = new ArrayList<>();

			for (final IMarker marker : updatedMarkers) {
				final CheckstyleViolationMarker violationMarker = new CheckstyleViolationMarker(marker);
				violationMarkers.add(violationMarker);
			}

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					CheckstyleViolationsView.this.tableViewer.setInput(violationMarkers); // NOPMD
					CheckstyleViolationsView.this.updateTitleAndLabel(violationMarkers); // NOPMD
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
		final String newPartName = String.format(CheckstyleViolationsView.FILTERED_PART_NAME_FORMAT_STRING,
				numFilteredViolations, numViolations);
		this.setPartName(newPartName);
	}

	private void updateNumViolationsLabel(final int numFilteredViolations, final int numViolations) {
		final String text = String.format(CheckstyleViolationsView.NUMBER_OF_CHECKSTYLE_VIOLATIONS_FORMAT_STRING,
				numFilteredViolations, numViolations);
		this.numViolationsLabel.setText(text);
		this.numViolationsLabel.getParent().layout(); // update label
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		final CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) selection.getFirstElement();
		if (violationMarker == null) {
			return;
		}

		try {
			IDE.openEditor(this.getSite().getPage(), violationMarker.getMarker());
		} catch (final PartInitException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void filterByPriority(final int selectionIndex) {
		final CheckstylePriorityViewerFilter priorityFilter = (CheckstylePriorityViewerFilter) this.viewerFilters[CheckstyleViolationsView.FILTER_INDEX_PRIORITY];
		priorityFilter.setSelectionIndex(selectionIndex);

		final IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getEclipseEditorPreferences();
		// highest priority (here: 3) should always be displayed
		for (int i = 0; i <= selectionIndex; i++) {
			final int transformedSeverityLevelIndex = SeverityLevel.ERROR.ordinal() - i;
			final String key = this.keyByPriority.get(transformedSeverityLevelIndex);
			preferences.putBoolean(key, true);
		}
		for (int i = selectionIndex + 1; i <= SeverityLevel.ERROR.ordinal(); i++) {
			final int transformedSeverityLevelIndex = SeverityLevel.ERROR.ordinal() - i;
			final String key = this.keyByPriority.get(transformedSeverityLevelIndex);
			preferences.putBoolean(key, false);
		}
	}

	@Override
	protected String getColumnOrder() {
		return CheckstyleViolationsView.PREF_COLUMN_ORDER;
	}

	@Override
	protected String getToolName() {
		return "Checkstyle";
	}

	@Override
	protected SelectionListener getPrioritySelectionListener() {
		return new PriorityFilterSelectionAdapter(CheckstyleViolationsView.PREF_FILTER_PRIORITY, this.tableViewer, this.viewPreferences);
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
			final int lowestPriority = source.getSelectionIndex();
			CheckstyleViolationsView.this.filterByPriority(lowestPriority);
			this.tableViewer.refresh(false);
			this.viewPreferences.putInt(this.preferenceFilterPriority, lowestPriority); // save
			// filter
			// setting

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					CheckstyleViolationsView.this.updateTitleAndLabel((List<?>) PriorityFilterSelectionAdapter.this.tableViewer.getInput());
				}
			});
		}
	}
}
