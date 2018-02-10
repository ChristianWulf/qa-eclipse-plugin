package qa.eclipse.plugin.bundles.checkstyle.view;

import static qa.eclipse.plugin.bundles.checkstyle.view.CheckstyleViolationMarkerComparator.SORT_PROP_CHECK_NAME;
import static qa.eclipse.plugin.bundles.checkstyle.view.CheckstyleViolationMarkerComparator.SORT_PROP_CHECK_PACKAGE_NAME;
import static qa.eclipse.plugin.bundles.checkstyle.view.CheckstyleViolationMarkerComparator.SORT_PROP_LINENUMBER;
import static qa.eclipse.plugin.bundles.checkstyle.view.CheckstyleViolationMarkerComparator.SORT_PROP_PRIORITY;
import static qa.eclipse.plugin.bundles.checkstyle.view.CheckstyleViolationMarkerComparator.SORT_PROP_PROJECTNAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.Activator;
import qa.eclipse.plugin.bundles.checkstyle.StringUtils;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleMarkers;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;
import qa.eclipse.plugin.bundles.checkstyle.marker.ImageRegistryKey;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;

public class CheckstyleViolationsView extends ViewPart
		implements ISelectionChangedListener, IResourceChangeListener, IDoubleClickListener {

	public static final String TOOL_NAME = "Checkstyle";
	public static final String ID = "qa.eclipse.plugin.checkstyle.view";

	static final String PREF_SORT_DIRECTION = ID + ".sortDirection";
	static final String PREF_SORT_COLUMN_INDEX = ID + ".sortColumnIndex";
	static final String PREF_FILTER_PRIORITY = ID + ".filterPriority";
	static final String PREF_COLUMN_ORDER = ID + ".columnOrder";

	private static final String PART_NAME_FORMAT_STRING = TOOL_NAME + " Violations (%d)";
	private static final String NUMBER_OF_CHECKSTYLE_VIOLATIONS = "Number of " + TOOL_NAME + " Violations: ";
	private static final int FILTER_INDEX_PRIORITY = 0;
	private static final int FILTER_INDEX_PROJECT = 1;

	private static final String KEY_PREFIX = "vertical.checkstyle.";

	// tutorial used from
	// http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html

	private final ViewerComparator comparator = new CheckstyleViolationMarkerComparator();
	private final Preferences viewPreferences;

	private Label label;
	private TableViewer tableViewer;

	private final ViewerFilter[] viewerFilters = new ViewerFilter[2];

	private final Map<Integer, String> verticalKeyByPriority = new HashMap<>();

	public CheckstyleViolationsView() {
		IEclipsePreferences defaultPreferences = CheckstylePreferences.INSTANCE.getDefaultPreferences();
		defaultPreferences.putInt(PREF_SORT_DIRECTION, SWT.DOWN);
		defaultPreferences.putInt(PREF_SORT_COLUMN_INDEX, SWT.DOWN);

		viewPreferences = CheckstylePreferences.INSTANCE.getEclipseScopedPreferences();

		viewerFilters[FILTER_INDEX_PRIORITY] = new CheckstylePriorityViewerFilter();
		viewerFilters[FILTER_INDEX_PROJECT] = new CheckstyleProjectNameViewerFilter();

		verticalKeyByPriority.put(SeverityLevel.ERROR.ordinal(), KEY_PREFIX + "error");
		verticalKeyByPriority.put(SeverityLevel.WARNING.ordinal(), KEY_PREFIX + "warning");
		verticalKeyByPriority.put(SeverityLevel.INFO.ordinal(), KEY_PREFIX + "info");
		verticalKeyByPriority.put(SeverityLevel.IGNORE.ordinal(), KEY_PREFIX + "ignore");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = parent;
		composite.setLayout(new GridLayout(1, false));

		Composite firstLine = new Composite(composite, SWT.NONE);
		firstLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout firstLineLayout = new GridLayout(3, false);
		firstLineLayout.marginHeight = 0;
		firstLineLayout.marginWidth = 0;
		firstLine.setLayout(firstLineLayout);

		label = new Label(firstLine, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));

		Label filterLabel = new Label(firstLine, SWT.NONE);
		filterLabel.setText("Filters:");
		filterLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));

		TableCombo tableCombo = new TableCombo(firstLine, SWT.READ_ONLY | SWT.BORDER);
		tableCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		tableCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableCombo source = (TableCombo) e.getSource();
				int lowestPriority = source.getSelectionIndex();
				filterByPriority(lowestPriority);
				tableViewer.refresh(false);
				viewPreferences.putInt(PREF_FILTER_PRIORITY, lowestPriority); // save filter setting
				return;
			}
		});
		createTableComboItems(tableCombo);

		tableViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableViewer.setUseHashlookup(true);
		// activate the tooltip support for the viewer
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

		createColumns();

		// tableColumnLayout.setColumnData(column, new ColumnWeightData(20, 200, true));

		// configure table
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setSortDirection(loadSavedSortDirection());
		tableViewer.getTable().setSortColumn(loadSavedSortColumn());
		tableViewer.getTable().setColumnOrder(loadSavedColumnOrder());

		// we use the comparator when sorting by column
		tableViewer.setComparator(comparator);

		tableViewer.addSelectionChangedListener(this);

		tableViewer.setFilters(viewerFilters);
		// load filter settings
		int loadSavedFilterPriority = loadSavedFilterPriority(tableCombo);
		tableCombo.select(loadSavedFilterPriority);
		filterByPriority(loadSavedFilterPriority);

		// interprets the input and transforms it into rows
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		// on double click: opens the corresponding file in the proper editor, jumps to
		// the line, and selects it
		tableViewer.addDoubleClickListener(this);

		addContextMenu();
		addToolBarButtons();

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		tableViewer.getControl().setLayoutData(gridData);

		// TODO unknown what this is necessary for
		getSite().setSelectionProvider(tableViewer);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

		updateView();
	}

	private void createTableComboItems(TableCombo tableCombo) {
		String imageRegistryKey;
		Image image;
		TableItem ti;

		SeverityLevel[] severityLevels = SeverityLevel.values().clone();

		// highest priority (here: SeverityLevel.ERROR = 3) should be displayed as the
		// top-most item
		Arrays.sort(severityLevels, new Comparator<SeverityLevel>() {
			@Override
			public int compare(SeverityLevel o1, SeverityLevel o2) {
				return -1 * o1.compareTo(o2);
			}
		});

		for (SeverityLevel severityLevel : severityLevels) {
			imageRegistryKey = ImageRegistryKey.getPriorityColumnKeyByPriority(severityLevel.ordinal());
			image = Activator.getDefault().getImageRegistry().get(imageRegistryKey);
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

	private int loadSavedFilterPriority(TableCombo tableCombo) {
		final int DEFAULT_PRIORITY = SeverityLevel.IGNORE.ordinal();
		int filterPriority = viewPreferences.getInt(PREF_FILTER_PRIORITY, DEFAULT_PRIORITY);
		if (filterPriority < 0 || filterPriority >= tableCombo.getItemCount()) {
			filterPriority = DEFAULT_PRIORITY;
		}
		return filterPriority;
	}

	private int loadSavedSortDirection() {
		int savedSortDirection = viewPreferences.getInt(PREF_SORT_DIRECTION, SWT.NONE);
		return savedSortDirection;
	}

	private TableColumn loadSavedSortColumn() {
		Integer columnIndex = viewPreferences.getInt(PREF_SORT_COLUMN_INDEX, 0);
		TableColumn savedSortColumn;
		try {
			savedSortColumn = tableViewer.getTable().getColumn(columnIndex);
		} catch (IllegalArgumentException e) {
			savedSortColumn = tableViewer.getTable().getColumn(0);
		}
		return savedSortColumn;
	}

	private int[] loadSavedColumnOrder() {
		final int numColumns = tableViewer.getTable().getColumnCount();
		int[] columnOrderIndices = new int[numColumns];

		String columnOrderPreference = viewPreferences.get(PREF_COLUMN_ORDER, "");
		String[] columnOrdersEncoded = columnOrderPreference.split(",");

		boolean reset = false;

		try {
			for (int i = 0; i < columnOrdersEncoded.length; i++) {
				String columnOrderEncoded = columnOrdersEncoded[i];
				columnOrderEncoded = columnOrderEncoded.trim();
				int columnOrderIndex = Integer.parseInt(columnOrderEncoded);
				columnOrderIndices[i] = columnOrderIndex;
			}
		} catch (NumberFormatException e) {
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

		Menu contextMenu = new Menu(tableViewer.getTable());
		tableViewer.getTable().setMenu(contextMenu);

		for (TableColumn tableColumn : tableViewer.getTable().getColumns()) {
			createMenuItem(contextMenu, tableColumn);
		}
	}

	private void createMenuItem(Menu contextMenu, TableColumn tableColumn) {
		final MenuItem itemName = new MenuItem(contextMenu, SWT.CHECK);
		itemName.setText(tableColumn.getText());
		itemName.setSelection(tableColumn.getResizable());
		itemName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
		flushSettings();

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	private void createColumns() {
		Listener columnMovedListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.type == SWT.Move) {
					int[] columnOrder = tableViewer.getTable().getColumnOrder();
					String columnOrderEncoded = StringUtils.join(columnOrder, ',');
					viewPreferences.put(PREF_COLUMN_ORDER, columnOrderEncoded);
				}
			}
		};

		TableViewerColumn tableViewerColumn;
		TableColumn column;

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) element;
				int priority = violationMarker.getSeverityLevelIndex();
				String imageRegistryKey = ImageRegistryKey.getPriorityColumnKeyByPriority(priority);
				Image image = Activator.getDefault().getImageRegistry().get(imageRegistryKey);
				return image;
			}

			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getToolTipImage(Object object) {
				return getImage(object);
			}

			@Override
			public String getToolTipText(Object element) {
				CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) element;
				int severityLevelIndex = violationMarker.getSeverityLevelIndex();
				SeverityLevel severityLevel = SeverityLevel.values()[severityLevelIndex];
				return severityLevel.name();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Priority"); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(viewPreferences, tableViewer, SORT_PROP_PRIORITY));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getCheckName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Check name");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addSelectionListener(new CompareOnSelectListener(viewPreferences, tableViewer, SORT_PROP_CHECK_NAME));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getMessage();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Violation message");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(400);
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return String.valueOf(marker.getProjectName());
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Project");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(100);
		column.addSelectionListener(new CompareOnSelectListener(viewPreferences, tableViewer, SORT_PROP_PROJECTNAME));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getCheckPackageName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Check package");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addSelectionListener(
				new CompareOnSelectListener(viewPreferences, tableViewer, SORT_PROP_CHECK_PACKAGE_NAME));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return String.valueOf(marker.getLineNumer());
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Line");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(viewPreferences, tableViewer, SORT_PROP_LINENUMBER));
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getDirectoryPath();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Directory path");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addListener(SWT.Move, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
				return marker.getFileName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("File name");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addListener(SWT.Move, columnMovedListener);
	}

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
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
	public void resourceChanged(IResourceChangeEvent event) {
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER,
				true);
		// do not unnecessarily collect all PMD markers again if no marker of this
		// resource was changed
		if (markerDeltas.length == 0) {
			return;
		}

		updateView();
	}

	private void updateView() {
		final IMarker[] updatedMarkers = CheckstyleMarkers.findAllInWorkspace();

		final List<CheckstyleViolationMarker> violationMarkers = new ArrayList<>();

		for (IMarker marker : updatedMarkers) {
			CheckstyleViolationMarker violationMarker = new CheckstyleViolationMarker(marker);
			violationMarkers.add(violationMarker);
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				int numViolations = violationMarkers.size();
				String newPartName = String.format(PART_NAME_FORMAT_STRING, numViolations);

				CheckstyleViolationsView.this.setPartName(newPartName);
				label.setText(NUMBER_OF_CHECKSTYLE_VIOLATIONS + numViolations);
				label.getParent().layout(); // fixed bug: the label was not displayed upon reopening the view
				tableViewer.setInput(violationMarkers);
			}
		});
	}

	public TableViewer getTableViewer() {
		// tableViewer.getTable().getItem(index)
		// tableViewer.getTable().showItem(item);
		// tableViewer.reveal(element);
		return tableViewer;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		CheckstyleViolationMarker violationMarker = (CheckstyleViolationMarker) selection.getFirstElement();
		if (violationMarker == null) {
			return;
		}

		try {
			IDE.openEditor(getSite().getPage(), violationMarker.getMarker());
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}
	}

	private void flushSettings() {
		try {
			viewPreferences.flush();
		} catch (Exception e) {
			// we do not want to hinder Eclipse to quit.
			// So, we catch all exceptions here.
		}
	}

	private void filterByPriority(int lowestPriority) {
		CheckstylePriorityViewerFilter priorityFilter = (CheckstylePriorityViewerFilter) viewerFilters[FILTER_INDEX_PRIORITY];
		priorityFilter.setLowestPriority(lowestPriority);

		IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getEclipseEditorPreferences();
		// highest priority (here: 3) should always be displayed
		for (int i = SeverityLevel.ERROR.ordinal(); i >= lowestPriority; i--) {
			String key = verticalKeyByPriority.get(i);
			preferences.putBoolean(key, true);
		}
		for (int i = lowestPriority - 1; i >= 0; i--) {
			String key = verticalKeyByPriority.get(i);
			preferences.putBoolean(key, false);
		}
	}

}
