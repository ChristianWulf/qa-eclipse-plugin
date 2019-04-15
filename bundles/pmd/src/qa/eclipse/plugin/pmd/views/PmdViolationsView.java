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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import net.sourceforge.pmd.RulePriority;
import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.icons.ImageRegistryKey;
import qa.eclipse.plugin.pmd.markers.PmdMarkers;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;
import qa.eclipse.plugin.pmd.preference.PmdPreferences;

/**
 *
 * @author Christian Wulf
 *
 */
public class PmdViolationsView extends ViewPart
		implements ISelectionChangedListener, IResourceChangeListener, IDoubleClickListener {

	public static final String TOOL_NAME = "PMD";
	public static final String ID = "pmd.eclipse.plugin.views.PmdViolationsView";

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

	private final ViewerComparator comparator = new PmdViolationMarkerComparator();
	private final Preferences viewPreferences;

	private Label numViolationsLabel;
	private TableViewer tableViewer;

	private final ViewerFilter[] viewerFilters = new ViewerFilter[2];

	private final Map<Integer, String> keyByPriority = new HashMap<>();

	public PmdViolationsView() {
		final IEclipsePreferences defaultPreferences = PmdPreferences.INSTANCE.getDefaultPreferences();
		defaultPreferences.putInt(PmdViolationsView.PREF_SORT_DIRECTION, SWT.DOWN);
		defaultPreferences.putInt(PmdViolationsView.PREF_SORT_COLUMN_INDEX, SWT.DOWN);

		this.viewPreferences = PmdPreferences.INSTANCE.getEclipseScopedPreferences();

		this.viewerFilters[PmdViolationsView.FILTER_INDEX_PRIORITY] = new PmdPriorityViewerFilter();
		this.viewerFilters[PmdViolationsView.FILTER_INDEX_PROJECT] = new PmdProjectNameViewerFilter();

		this.keyByPriority.put(RulePriority.HIGH.getPriority(), "pmd.high.clvertical");
		this.keyByPriority.put(RulePriority.MEDIUM_HIGH.getPriority(), "pmd.mediumhigh.clvertical");
		this.keyByPriority.put(3, "pmd.medium.clvertical");
		this.keyByPriority.put(4, "pmd.mediumlow.clvertical");
		this.keyByPriority.put(5, "pmd.low.clvertical");
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
				final List<PmdViolationMarker> violationMarkers = (List<PmdViolationMarker>) PmdViolationsView.this.tableViewer
						.getInput();
				// PmdMarkers.deleteMarkers(resource);
				ClearViolationsViewJob.startAsyncAnalysis(violationMarkers);
			}
		});
		// clearButton.setText("Clear");
		clearButton.setToolTipText("Clears all PMD violations");

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
				final int lowestPriority = source.getSelectionIndex() + 1;
				PmdViolationsView.this.filterByPriority(lowestPriority);
				PmdViolationsView.this.tableViewer.refresh(false);
				PmdViolationsView.this.viewPreferences.putInt(PmdViolationsView.PREF_FILTER_PRIORITY, lowestPriority); // save
				// filter
				// setting

				Display.getDefault().asyncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						final Object input = PmdViolationsView.this.tableViewer.getInput();
						final List<PmdViolationMarker> numFilteredViolations = (List<PmdViolationMarker>) input;

						PmdViolationsView.this.updateTitleAndLabel(numFilteredViolations);
					}
				});

				return;
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
		tableCombo.select(loadSavedFilterPriority - 1);
		this.filterByPriority(loadSavedFilterPriority);

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

		for (final RulePriority rulePriority : RulePriority.values()) {
			imageRegistryKey = ImageRegistryKey.getPriorityColumnKeyByPriority(rulePriority.getPriority());
			image = PmdUIPlugin.getDefault().getImageRegistry().get(imageRegistryKey);
			ti = new TableItem(tableCombo.getTable(), SWT.NONE);
			ti.setText("At least " + rulePriority.getName());
			ti.setImage(image);
			ti.setData(rulePriority);
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
		final int defaultPriority = RulePriority.LOW.getPriority();
		int filterPriority = this.viewPreferences.getInt(PmdViolationsView.PREF_FILTER_PRIORITY, defaultPriority);
		if ((filterPriority < 0) || (filterPriority >= tableCombo.getItemCount())) {
			filterPriority = defaultPriority;
		}
		return filterPriority;
	}

	private int loadSavedSortDirection() {
		final int savedSortDirection = this.viewPreferences.getInt(PmdViolationsView.PREF_SORT_DIRECTION, SWT.NONE);
		return savedSortDirection;
	}

	private TableColumn loadSavedSortColumn() {
		final Integer columnIndex = this.viewPreferences.getInt(PmdViolationsView.PREF_SORT_COLUMN_INDEX, 0);
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

		final String columnOrderPreference = this.viewPreferences.get(PmdViolationsView.PREF_COLUMN_ORDER, "");
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
					final int[] columnOrder = PmdViolationsView.this.tableViewer.getTable().getColumnOrder();
					final String columnOrderEncoded = StringUtils.join(columnOrder, ',');
					PmdViolationsView.this.viewPreferences.put(PmdViolationsView.PREF_COLUMN_ORDER,
							columnOrderEncoded);
				}
			}
		};

		TableViewerColumn tableViewerColumn;

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				final PmdViolationMarker violationMarker = (PmdViolationMarker) element;
				final int pmdPriority = violationMarker.getPriority();
				final String imageRegistryKey = ImageRegistryKey.getPriorityColumnKeyByPriority(pmdPriority);
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
		this.createColumn(tableViewerColumn, "Priority", 50, PmdViolationMarkerComparator.SORT_PROP_PRIORITY, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getRuleName();
			}
		});
		this.createColumn(tableViewerColumn, "Rule name", 200, PmdViolationMarkerComparator.SORT_PROP_RULENAME, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getMessage();
			}
		});
		this.createColumn(tableViewerColumn, "Violation message", 400, PmdViolationMarkerComparator.SORT_PROP_VIOLATION_MSG, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return String.valueOf(marker.getProjectName());
			}
		});
		this.createColumn(tableViewerColumn, "Project", 100, PmdViolationMarkerComparator.SORT_PROP_PROJECTNAME, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return String.valueOf(marker.getLineNumer());
			}
		});
		this.createColumn(tableViewerColumn, "Line", 50, PmdViolationMarkerComparator.SORT_PROP_LINENUMBER, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getRuleSetName();
			}
		});
		this.createColumn(tableViewerColumn, "Rule set", 100, PmdViolationMarkerComparator.SORT_PROP_RULESET, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getDirectoryPath();
			}
		});
		this.createColumn(tableViewerColumn, "Directory path", 200, columnMovedListener);

		tableViewerColumn = new TableViewerColumn(this.tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final PmdViolationMarker marker = (PmdViolationMarker) element;
				return marker.getFileName();
			}
		});
		this.createColumn(tableViewerColumn, "File name", 200, columnMovedListener);
	}

	/**
	 * Create a column with a selection listener.
	 *
	 * @param tableViewerColumn
	 * @param label
	 * @param width
	 * @param selectedSortProperty
	 * @param columnMovedListener
	 */
	private void createColumn(final TableViewerColumn tableViewerColumn, final String label, final int width, final int selectedSortProperty,
			final Listener columnMovedListener) {
		final TableColumn column = tableViewerColumn.getColumn();
		column.setText(label); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(width);
		column.addSelectionListener(new CompareOnSelectListener(this.viewPreferences, this.tableViewer,
				selectedSortProperty));
		column.addListener(SWT.Move, columnMovedListener);
	}

	/**
	 * Create a column without a selection listener.
	 *
	 * @param tableViewerColumn
	 * @param label
	 * @param width
	 * @param columnMovedListener
	 */
	private void createColumn(final TableViewerColumn tableViewerColumn, final String label, final int width,
			final Listener columnMovedListener) {
		final TableColumn column = tableViewerColumn.getColumn();
		column.setText(label); // only icon; hover shows explanation (HIGH, LOW, ...)
		column.setResizable(true);
		column.setMoveable(true);
		column.setData(this.tableViewer.getTable().getColumnCount() - 1); // necessary for save/load
		column.setWidth(width);
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
		final IMarkerDelta[] markerDeltas = event.findMarkerDeltas(PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER, true);
		// do not unnecessarily collect all PMD markers again if no marker of this
		// resource was changed
		if (markerDeltas.length == 0) {
			return;
		}

		this.updateView();
	}

	private void updateView() {
		final IMarker[] updatedMarkers = PmdMarkers.findAllInWorkspace();

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

	public TableViewer getTableViewer() {
		// tableViewer.getTable().getItem(index)
		// tableViewer.getTable().showItem(item);
		// tableViewer.reveal(element);
		return this.tableViewer;
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

	private void flushSettings() {
		try {
			this.viewPreferences.flush();
		} catch (final Exception e) { // NOCS,NOPMD
			// we do not want to hinder Eclipse to quit.
			// So, we catch all exceptions here.
		}
	}

	private void filterByPriority(final int lowestPriority) {
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

}
