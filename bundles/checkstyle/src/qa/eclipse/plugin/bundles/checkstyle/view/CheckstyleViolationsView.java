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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.CheckstyleUIPlugin;
import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleMarkersUtils;
import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleViolationMarker;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;
import qa.eclipse.plugin.bundles.common.ImageRegistryKeyUtils;
import qa.eclipse.plugin.bundles.common.StringUtils;

/**
 *
 * @author Christian Wulf
 *
 */
public class CheckstyleViolationsView extends ViewPart
		implements ISelectionChangedListener, IResourceChangeListener, IDoubleClickListener {

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

	private final ViewerComparator comparator = new CheckstyleViolationMarkerComparator();
	private final Preferences viewPreferences;

	private Label numViolationsLabel;
	private TableViewer tableViewer;

	private final ViewerFilter[] viewerFilters = new ViewerFilter[2];

	private final Map<Integer, String> verticalKeyByPriority = new ConcurrentHashMap<>();

	/**
	 * Create the violation view.
	 */
	public CheckstyleViolationsView() {
		super();
		final IEclipsePreferences defaultPreferences = CheckstylePreferences.INSTANCE.getDefaultPreferences();
		defaultPreferences.putInt(CheckstyleViolationsView.PREF_SORT_DIRECTION, SWT.DOWN);
		defaultPreferences.putInt(CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX, SWT.DOWN);

		this.viewPreferences = CheckstylePreferences.INSTANCE.getEclipseScopedPreferences();

		this.viewerFilters[CheckstyleViolationsView.FILTER_INDEX_PRIORITY] = new CheckstylePriorityViewerFilter();
		this.viewerFilters[CheckstyleViolationsView.FILTER_INDEX_PROJECT] = new CheckstyleProjectNameViewerFilter();

		this.verticalKeyByPriority.put(SeverityLevel.ERROR.ordinal(), CheckstyleViolationsView.KEY_PREFIX + "error");
		this.verticalKeyByPriority.put(SeverityLevel.WARNING.ordinal(),
				CheckstyleViolationsView.KEY_PREFIX + "warning");
		this.verticalKeyByPriority.put(SeverityLevel.INFO.ordinal(), CheckstyleViolationsView.KEY_PREFIX + "info");
		this.verticalKeyByPriority.put(SeverityLevel.IGNORE.ordinal(), CheckstyleViolationsView.KEY_PREFIX + "ignore");

		// violationPriorityBySeverityLevel.put(SeverityLevel.ERROR, 0);
		// int violationPriority =
		// violationPriorityBySeverityLevel.get(SeverityLevel.ERROR);
	}

	@Override
	public void createPartControl(final Composite parent) {
		final Composite composite = parent;
		composite.setLayout(new GridLayout(1, false));

		final Composite firstLine = new Composite(composite, SWT.NONE);
		firstLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout firstLineLayout = new GridLayout(5, false);
		firstLineLayout.marginHeight = 0;
		firstLineLayout.marginWidth = 0;
		firstLine.setLayout(firstLineLayout);

		this.numViolationsLabel = new Label(firstLine, SWT.NONE);
		this.numViolationsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));

		final Button clearButton = new Button(firstLine, SWT.PUSH);
		clearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		final String symbolicName = "platform:/plugin/org.eclipse.ui.views.log/icons/elcl16/clear.png";
		// PlatformUI.getWorkbench().getSharedImages().getImage(symbolicName)
		// AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
		// "/icons/settings.png").createImage();
		final ImageDescriptor clearButtonImageDescriptor;
		try {
			clearButtonImageDescriptor = ImageDescriptor.createFromURL(new URL(symbolicName));
		} catch (final MalformedURLException e) {
			throw new IllegalStateException(e);
		}
		final Image clearButtonImage = clearButtonImageDescriptor.createImage();
		clearButton.setImage(clearButtonImage);
		clearButton.setBackground(firstLine.getBackground());
		clearButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				@SuppressWarnings("unchecked")
				final List<CheckstyleViolationMarker> violationMarkers = (List<CheckstyleViolationMarker>) CheckstyleViolationsView.this.tableViewer
						.getInput();
				// CheckstyleMarkers.deleteMarkers(eclipseFile);
				ClearViolationsViewJob.startAsyncAnalysis(violationMarkers);
			}
		});
		// clearButton.setText("Clear");
		clearButton.setToolTipText("Clears all Checkstyle violations");

		final Label separatorLabel = new Label(firstLine, SWT.BORDER);
		separatorLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Label filterLabel = new Label(firstLine, SWT.NONE);
		filterLabel.setText("Filters:");
		filterLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));

		final TableCombo tableCombo = new TableCombo(firstLine, SWT.READ_ONLY | SWT.BORDER);
		tableCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		tableCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final TableCombo source = (TableCombo) e.getSource();
				final int selectionIndex = source.getSelectionIndex();
				CheckstyleViolationsView.this.filterBySelectionIndex(selectionIndex);
				CheckstyleViolationsView.this.tableViewer.refresh(false);
				CheckstyleViolationsView.this.viewPreferences.putInt(CheckstyleViolationsView.PREF_FILTER_PRIORITY,
						selectionIndex); // save filter setting

				Display.getDefault().asyncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						final Object input = CheckstyleViolationsView.this.tableViewer.getInput();
						final List<CheckstyleViolationMarker> violationMarkers = (List<CheckstyleViolationMarker>) input;

						CheckstyleViolationsView.this.updateTitleAndLabel(violationMarkers);
					}
				});
			}
		});
		this.createTableComboItems(tableCombo);

		this.tableViewer = new TableViewer(composite,
				SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		this.tableViewer.setUseHashlookup(true);
		// activate the tooltip support for the viewer
		ColumnViewerToolTipSupport.enableFor(this.tableViewer, ToolTip.NO_RECREATE);

		this.createColumns();

		// tableColumnLayout.setColumnData(column, new ColumnWeightData(20, 200, true));

		// configure table
		this.tableViewer.getTable().setHeaderVisible(true);
		this.tableViewer.getTable().setLinesVisible(true);
		this.tableViewer.getTable().setSortDirection(this.loadSavedSortDirection());
		this.tableViewer.getTable().setSortColumn(this.loadSavedSortColumn());
		this.tableViewer.getTable().setColumnOrder(this.loadSavedColumnOrder());

		// we use the comparator when sorting by column
		this.tableViewer.setComparator(this.comparator);

		this.tableViewer.addSelectionChangedListener(this);

		this.tableViewer.setFilters(this.viewerFilters);
		// load filter settings
		final int loadSavedFilterPriority = this.loadSavedFilterPriority(tableCombo);
		tableCombo.select(loadSavedFilterPriority);
		this.filterBySelectionIndex(loadSavedFilterPriority);

		// interprets the input and transforms it into rows
		this.tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		// on double click: opens the corresponding file in the proper editor, jumps to
		// the line, and selects it
		this.tableViewer.addDoubleClickListener(this);

		this.addContextMenu();
		this.addToolBarButtons();

		// Layout the viewer
		final GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		this.tableViewer.getControl().setLayoutData(gridData);

		// TODO unknown what this is necessary for
		this.getSite().setSelectionProvider(this.tableViewer);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

		this.updateView();
	}

	private void createTableComboItems(final TableCombo tableCombo) {
		String imageRegistryKey;
		Image image;
		TableItem ti;

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
			ti = new TableItem(tableCombo.getTable(), SWT.NONE);
			ti.setText("At least " + severityLevel.name());
			ti.setImage(image);
			ti.setData(severityLevel);
		}
	}

	private void addToolBarButtons() {
		// IActionBars actionBars = getViewSite().getActionBars();
		// IToolBarManager toolBar = actionBars.getToolBarManager();
		//
		// // IAction action = new
		// toolBar.add(action);
	}

	private int loadSavedFilterPriority(final TableCombo tableCombo) {
		final int defaultPriorty = SeverityLevel.IGNORE.ordinal();
		int filterPriority = this.viewPreferences.getInt(CheckstyleViolationsView.PREF_FILTER_PRIORITY, defaultPriorty);
		if (filterPriority < 0 || filterPriority >= tableCombo.getItemCount()) {
			filterPriority = defaultPriorty;
		}
		return filterPriority;
	}

	private int loadSavedSortDirection() {
		return this.viewPreferences.getInt(CheckstyleViolationsView.PREF_SORT_DIRECTION,
				SWT.NONE);
	}

	private TableColumn loadSavedSortColumn() {
		final Integer columnIndex = this.viewPreferences.getInt(CheckstyleViolationsView.PREF_SORT_COLUMN_INDEX, 0);
		TableColumn savedSortColumn;
		try {
			savedSortColumn = this.tableViewer.getTable().getColumn(columnIndex);
		} catch (final IllegalArgumentException e) {
			savedSortColumn = this.tableViewer.getTable().getColumn(0);
		}
		return savedSortColumn;
	}

	private int[] loadSavedColumnOrder() {
		final int numColumns = this.tableViewer.getTable().getColumnCount();
		final int[] columnOrderIndices = new int[numColumns];

		final String columnOrderPreference = this.viewPreferences.get(CheckstyleViolationsView.PREF_COLUMN_ORDER, "");
		final String[] columnOrdersEncoded = columnOrderPreference.split(",");

		boolean reset = false;

		try {
			for (int i = 0; i < columnOrdersEncoded.length; i++) {
				String columnOrderEncoded = columnOrdersEncoded[i];
				columnOrderEncoded = columnOrderEncoded.trim();
				final int columnOrderIndex = Integer.parseInt(columnOrderEncoded);
				columnOrderIndices[i] = columnOrderIndex;
			}
		} catch (final NumberFormatException e) {
			// if one of the encoded indices is an invalid number,
			// use the default order 0,1,2,...
			reset = true;
		}

		if (columnOrderIndices.length != columnOrdersEncoded.length) {
			// if the viewPreferences are out-of-date due to an plugin version update,
			// use the default order 0,1,2,...
			reset = true;
		}

		if (reset) {
			for (int i = 0; i < numColumns; i++) {
				columnOrderIndices[i] = i;
			}
		}

		return columnOrderIndices;
	}

	private void addContextMenu() {
		// MenuManager menuManager = new MenuManager();
		// Menu contextMenu = menuManager.createContextMenu(tableViewer.getTable());
		// tableViewer.getTable().setMenu(contextMenu);
		// getSite().registerContextMenu(menuManager, tableViewer);
		// getEditorSite().registerContextMenu(menuManager, tableViewer, false);

		final Menu contextMenu = new Menu(this.tableViewer.getTable());
		this.tableViewer.getTable().setMenu(contextMenu);

		for (final TableColumn tableColumn : this.tableViewer.getTable().getColumns()) {
			this.createMenuItem(contextMenu, tableColumn);
		}
	}

	private void createMenuItem(final Menu contextMenu, final TableColumn tableColumn) {
		final MenuItem itemName = new MenuItem(contextMenu, SWT.CHECK);
		itemName.setText(tableColumn.getText());
		itemName.setSelection(tableColumn.getResizable());
		itemName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (itemName.getSelection()) {
					tableColumn.setWidth(150);
					tableColumn.setResizable(true);
				} else {
					tableColumn.setWidth(0);
					tableColumn.setResizable(false);
				}
			}
		});
	}

	@Override
	public void dispose() { // is called on closing the view and on closing Eclipse itself
		this.flushSettings();

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	private void createColumns() {
		final Listener columnMovedListener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.type == SWT.Move) {
					final int[] columnOrder = CheckstyleViolationsView.this.tableViewer.getTable().getColumnOrder();
					final String columnOrderEncoded = StringUtils.join(columnOrder, ',');
					CheckstyleViolationsView.this.viewPreferences.put(CheckstyleViolationsView.PREF_COLUMN_ORDER,
							columnOrderEncoded);
				}
			}
		};

		TableViewerColumn tableViewerColumn;
		TableColumn column;

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				final CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) element;
				final int priority = violationMarker.getSeverityLevelIndex();
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
				final int severityLevelIndex = violationMarker.getSeverityLevelIndex();
				final SeverityLevel severityLevel = SeverityLevel.values()[severityLevelIndex];
				return severityLevel.name();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Priority"); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				CheckstyleViolationMarkerComparator.SORT_PROP_PRIORITY));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getCheckName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Check name");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				CheckstyleViolationMarkerComparator.SORT_PROP_CHECK_NAME));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getMessage();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Violation message");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(400);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				CheckstyleViolationMarkerComparator.SORT_PROP_VIOLATION_MSG));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return String.valueOf(marker.getProjectName());
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Project");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(100);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				CheckstyleViolationMarkerComparator.SORT_PROP_PROJECTNAME));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getCheckPackageName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Check package");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				CheckstyleViolationMarkerComparator.SORT_PROP_CHECK_PACKAGE_NAME));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return String.valueOf(marker.getLineNumer());
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Line");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				CheckstyleViolationMarkerComparator.SORT_PROP_LINENUMBER));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getDirectoryPath();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Directory path");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getFileName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("File name");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addListener(SWT.Move, columnMovedListener);
	}

	@Override
	public void setFocus() {
		this.tableViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		// do nothing when selecting (the event is not aware of left-click or
		// right-click)
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

	private void updateView() {
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

	public TableViewer getTableViewer() {
		// tableViewer.getTable().getItem(index)
		// tableViewer.getTable().showItem(item);
		// tableViewer.reveal(element);
		return this.tableViewer;
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

	private void flushSettings() {
		try {
			this.viewPreferences.flush();
		} catch (final Exception e) { // NOCS, NOPMD
			// we do not want to hinder Eclipse to quit.
			// So, we catch all exceptions here.
		}
	}

	private void filterBySelectionIndex(final int selectionIndex) {
		final CheckstylePriorityViewerFilter priorityFilter = (CheckstylePriorityViewerFilter) this.viewerFilters[CheckstyleViolationsView.FILTER_INDEX_PRIORITY];
		priorityFilter.setSelectionIndex(selectionIndex);

		final IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getEclipseEditorPreferences();
		// highest priority (here: 3) should always be displayed
		for (int i = 0; i <= selectionIndex; i++) {
			final int transformedSeverityLevelIndex = SeverityLevel.ERROR.ordinal() - i;
			final String key = this.verticalKeyByPriority.get(transformedSeverityLevelIndex);
			preferences.putBoolean(key, true);
		}
		for (int i = selectionIndex + 1; i <= SeverityLevel.ERROR.ordinal(); i++) {
			final int transformedSeverityLevelIndex = SeverityLevel.ERROR.ordinal() - i;
			final String key = this.verticalKeyByPriority.get(transformedSeverityLevelIndex);
			preferences.putBoolean(key, false);
		}
	}

}
