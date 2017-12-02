package pmd.eclipse.plugin.views;

import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_LINENUMBER;
import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_PRIORITY;
import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_PROJECTNAME;
import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_RULENAME;
import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_RULESET;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import net.sourceforge.pmd.RulePriority;
import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.icons.ImageRegistryKey;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.markers.PmdViolationMarker;
import pmd.eclipse.plugin.settings.PmdPreferences;

public class PmdViolationsView extends ViewPart
		implements ISelectionChangedListener, IResourceChangeListener, IDoubleClickListener, MouseListener {

	public static final String ID = "pmd.eclipse.plugin.views.PmdViolationsView";

	private static final String PART_NAME_FORMAT_STRING = "PMD Violations (%d)";
	private static final String NUMBER_OF_PMD_VIOLATIONS = "Number of PMD Violations: ";

	private static final String PREF_SORT_DIRECTION = ID + ".sortDirection";
	private static final String PREF_SORT_COLUMN_INDEX = ID + ".sortColumnIndex";

	private static final int FILTER_INDEX_PRIORITY = 0;
	private static final int FILTER_INDEX_PROJECT = 1;

	// tutorial used from
	// http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html

	private final ViewerComparator comparator = new PmdViolationMarkerComparator();
	private final Preferences viewPreferences;

	private Label label;
	private TableViewer tableViewer;

	// private final Map<String, PmdViewFilter> filterByAttribute = new HashMap<>();
	private final ViewerFilter[] viewerFilters = new ViewerFilter[2];

	public PmdViolationsView() {
		IEclipsePreferences defaultPreferences = PmdPreferences.INSTANCE.getDefaultPreferences();
		defaultPreferences.putInt(PREF_SORT_DIRECTION, SWT.DOWN);
		defaultPreferences.putInt(PREF_SORT_COLUMN_INDEX, SWT.DOWN);

		viewPreferences = PmdPreferences.INSTANCE.getEclipseScopedPreferences();

		// PmdViewFilter underlyingFilter = new PmdPassAllFilter();
		// filter = new PmdPriorityFilter(underlyingFilter,
		// RulePriority.LOW.getPriority();
		// filterByAttribute.put("priority", ));
		viewerFilters[FILTER_INDEX_PRIORITY] = new PmdPriorityViewerFilter();
		viewerFilters[FILTER_INDEX_PROJECT] = new PmdProjectNameViewerFilter();
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = parent;
		// composite = new Composite(parent, SWT.None);
		// composite.setLayout(new GridLayout(1, false));
		composite.setLayout(new GridLayout(1, false));

		label = new Label(composite, SWT.NONE);

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

		// we use the comparator when sorting by column
		tableViewer.setComparator(comparator);

		tableViewer.addSelectionChangedListener(this);
		// arrow down symbol: opens a menu with five priority-based filters
		// ViewerFilter viewerFilter = new ViewerFilter() {
		// @Override
		// public boolean select(Viewer viewer, Object parentElement, Object element) {
		// PmdViolationMarker marker = (PmdViolationMarker) element;
		// return filter.canPass(marker);
		// }
		// };
		tableViewer.setFilters(viewerFilters);
		// interprets the input and transforms it into rows
		tableViewer.setContentProvider(new ArrayContentProvider());
		// on double click: opens the corresponding file in the proper editor, jumps to
		// the line, and selects it
		tableViewer.addDoubleClickListener(this);

		tableViewer.getTable().addMouseListener(this);

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

	private void addToolBarButtons() {
		// IActionBars actionBars = getViewSite().getActionBars();
		// IToolBarManager toolBar = actionBars.getToolBarManager();
		//
		// // IAction action = new
		// toolBar.add(action);
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

	private Integer getSortColumnIndex() {
		return (Integer) tableViewer.getTable().getSortColumn().getData();
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
		TableViewerColumn tableViewerColumn;
		TableColumn column;

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				PmdViolationMarker violationMarker = (PmdViolationMarker) element;
				int pmdPriority = violationMarker.getPriority();
				String imageRegistryKey = ImageRegistryKey.getPriorityColumnKeyByPriority(pmdPriority);
				Image image = PmdUIPlugin.getDefault().getImageRegistry().get(imageRegistryKey);
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
				PmdViolationMarker violationMarker = (PmdViolationMarker) element;
				int pmdPriority = violationMarker.getPriority();
				RulePriority pmdRulePriority = RulePriority.valueOf(pmdPriority);
				return pmdRulePriority.toString();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Priority"); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_PRIORITY));

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getRuleName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Rule name");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_RULENAME));

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getMessage();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Violation message");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(400);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return String.valueOf(marker.getProjectName());
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Project");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(100);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_PROJECTNAME));

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return String.valueOf(marker.getLineNumer());
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Line");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_LINENUMBER));

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getRuleSetName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Rule set");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(100);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_RULESET));

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getDirectoryPath();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("Directory path");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getFileName();
			}
		});
		column = tableViewerColumn.getColumn();
		column.setText("File name");
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(200);
	}

	private IMarker[] getPmdMarkers() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IMarker[] markers;
		try {
			markers = workspaceRoot.findMarkers(PmdMarkers.PMD_VIOLATION_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
		return markers;
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
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(PmdMarkers.PMD_VIOLATION_MARKER, false);
		// do not unnecessarily collect all PMD markers again if no marker of this
		// resource was changed
		if (markerDeltas.length == 0) {
			return;
		}

		updateView();
	}

	private void updateView() {
		final List<PmdViolationMarker> pmdViolationMarkers = new ArrayList<>();

		final IMarker[] updatedMarkers = getPmdMarkers();
		for (IMarker marker : updatedMarkers) {
			PmdViolationMarker pmdViolationMarker = new PmdViolationMarker(marker);
			pmdViolationMarkers.add(pmdViolationMarker);
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				int numViolations = pmdViolationMarkers.size();
				String newPartName = String.format(PART_NAME_FORMAT_STRING, numViolations);

				PmdViolationsView.this.setPartName(newPartName);
				label.setText(NUMBER_OF_PMD_VIOLATIONS + numViolations);
				label.getParent().layout(); // fixed bug: the label was not displayed upon reopening the view
				tableViewer.setInput(pmdViolationMarkers);
			}
		});
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		PmdViolationMarker pmdViolationMarker = (PmdViolationMarker) selection.getFirstElement();
		if (pmdViolationMarker == null) {
			return;
		}

		try {
			IDE.openEditor(getSite().getPage(), pmdViolationMarker.getMarker());
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void mouseDown(MouseEvent e) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		return;
	}

	private void flushSettings() {
		try {
			viewPreferences.putInt(PREF_SORT_DIRECTION, tableViewer.getTable().getSortDirection());
			viewPreferences.putInt(PREF_SORT_COLUMN_INDEX, getSortColumnIndex());

			viewPreferences.flush();
		} catch (Exception e) {
			// we do not want to hinder Eclipse to quit.
			// So, we catch all exceptions here.
		}
	}

	public void filterByPriority(Integer lowestPriority) {
		PmdPriorityViewerFilter priorityFilter = (PmdPriorityViewerFilter) viewerFilters[FILTER_INDEX_PRIORITY];
		if (priorityFilter.getLowestPriority() < lowestPriority) {
			priorityFilter.setLowestPriority(lowestPriority);
			tableViewer.refresh(false);
		}
	}

}
