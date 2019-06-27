/***************************************************************************
 * Copyright (C) 2019
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import qa.eclipse.plugin.bundles.common.ILoggingFacility;
import qa.eclipse.plugin.bundles.common.markers.AbstractViolationMarker;

/**
 * @author Reiner Jung
 *
 */
public abstract class AbstractViolationViewPart extends ViewPart implements ISelectionChangedListener, IDoubleClickListener, IResourceChangeListener {

	protected Label numViolationsLabel;
	protected TableViewer tableViewer;
	protected final ViewerComparator comparator;

	protected final ViewerFilter[] viewerFilters = new ViewerFilter[2];

	protected final Preferences viewPreferences;

	private final String fileIconDecoratorId;

	private Composite firstLine;

	/**
	 * Create a violation view part.
	 *
	 * @param fileIconDecoratorId
	 *            id of the file icon decorator
	 * @param logger
	 *            plugin logging facillity
	 * @param viewPreferences
	 *            view preferences
	 */
	public AbstractViolationViewPart(final String fileIconDecoratorId, final ILoggingFacility logger, final IEclipsePreferences viewPreferences) {
		super();
		this.fileIconDecoratorId = fileIconDecoratorId;
		this.comparator = new ViolationMarkerComparator(logger);
		this.viewPreferences = viewPreferences;

	}

	@Override
	public void createPartControl(final Composite parent) {
		this.createHeaderOfControlPart(parent);

		this.createTableViewer(parent);

		this.configureHeaderofControlPart();

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

		this.updateView();
	}

	private void createHeaderOfControlPart(final Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		this.firstLine = new Composite(parent, SWT.NONE);
		this.firstLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout firstLineLayout = new GridLayout(5, false);
		firstLineLayout.marginHeight = 0;
		firstLineLayout.marginWidth = 0;
		this.firstLine.setLayout(firstLineLayout);
	}

	private void configureHeaderofControlPart() {
		this.numViolationsLabel = new Label(this.firstLine, SWT.NONE);
		this.numViolationsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 1, 1));

		final Button clearButton = new Button(this.firstLine, SWT.PUSH);
		clearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		final String symbolicName = "platform:/plugin/org.eclipse.ui.views.log/icons/elcl16/clear.png";
		final ImageDescriptor clearButtonImageDescriptor;
		try {
			clearButtonImageDescriptor = ImageDescriptor.createFromURL(new URL(symbolicName));
		} catch (final MalformedURLException e) {
			throw new IllegalStateException(e);
		}
		final Image clearButtonImage = clearButtonImageDescriptor.createImage();
		clearButton.setImage(clearButtonImage);
		clearButton.setBackground(this.firstLine.getBackground());
		clearButton.addListener(SWT.Selection, new ClearButtonListener(this.tableViewer, this.fileIconDecoratorId));
		clearButton.setToolTipText(String.format("Clears all %s violations", this.getToolName()));

		final Label separatorLabel = new Label(this.firstLine, SWT.BORDER);
		separatorLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Label filterLabel = new Label(this.firstLine, SWT.NONE);
		filterLabel.setText("Filters:");
		filterLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		final TableCombo priorityDropDown = new TableCombo(this.firstLine, SWT.READ_ONLY | SWT.BORDER);
		priorityDropDown.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		this.createPriorityDropDownItems(priorityDropDown);

		priorityDropDown.addSelectionListener(this.getPrioritySelectionListener());

		// load filter settings
		final int loadSavedFilterPriority = this.loadSavedFilterPriority(priorityDropDown);
		priorityDropDown.select(loadSavedFilterPriority);
		this.filterByPriority(loadSavedFilterPriority);
	}

	private void createTableViewer(final Composite parent) {
		this.tableViewer = new TableViewer(parent,
				SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		this.tableViewer.setUseHashlookup(true);
		// activate the tooltip support for the viewer
		ColumnViewerToolTipSupport.enableFor(this.tableViewer, ToolTip.NO_RECREATE);

		this.createColumns();

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

	}

	/**
	 * @return returns the priority selection listener seen in the upper part of the view
	 */
	protected abstract SelectionListener getPrioritySelectionListener();

	/**
	 * @return returns the name of the tool, e.g., checkstyle
	 */
	protected abstract String getToolName();

	/**
	 * @return returns the property name for the column order
	 */
	protected abstract String getColumnOrder();

	/**
	 * Retrieve the saved filter priority setting.
	 *
	 * @param prioritySelectionDropDown
	 *            combo box for priority selection
	 * @return returns the selected priority
	 */
	protected abstract int loadSavedFilterPriority(TableCombo prioritySelectionDropDown);

	/**
	 * Filter all markers by priority.
	 *
	 * @param filterPriority
	 *            lowest allowed priority level
	 */
	protected abstract void filterByPriority(int filterPriority);

	/**
	 * Update the view.
	 */
	protected abstract void updateView();

	/**
	 * Load saved sort direction.
	 *
	 * @return returns the sort direction
	 */
	protected abstract int loadSavedSortDirection();

	/**
	 * @return returns the stored value identifying the column to sort by
	 */
	protected abstract TableColumn loadSavedSortColumn();

	/**
	 * Hookup for toolbar buttons.
	 */
	protected abstract void addToolBarButtons();

	/**
	 * Create table columns.
	 */
	protected abstract void createColumns();

	/**
	 * Create items for the priority list.
	 *
	 * @param tableCombo
	 *            priority list combo box
	 */
	protected abstract void createPriorityDropDownItems(final TableCombo tableCombo);

	private int[] loadSavedColumnOrder() {
		final int numColumns = this.tableViewer.getTable().getColumnCount();
		final int[] columnOrderIndices = new int[numColumns];

		final String columnOrderPreference = this.viewPreferences.get(this.getColumnOrder(), "");
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

	private void flushSettings() {
		try {
			this.viewPreferences.flush();
		} catch (final Exception e) { // NOCS, NOPMD
			// we do not want to hinder Eclipse to quit.
			// So, we catch all exceptions here.
		}
	}

	private void createMenuItem(final Menu contextMenu, final TableColumn tableColumn) {
		final MenuItem itemName = new MenuItem(contextMenu, SWT.CHECK);
		itemName.setText(tableColumn.getText());
		itemName.setSelection(tableColumn.getResizable());
		itemName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
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

	@Override
	public void setFocus() {
		this.tableViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) { // NOPMD just need to ignore event
		// do nothing when selecting (the event is not aware of left-click or
		// right-click)
	}

	/**
	 * @return get table viewer
	 */
	public TableViewer getTableViewer() {
		// tableViewer.getTable().getItem(index)
		// tableViewer.getTable().showItem(item);
		// tableViewer.reveal(element);
		return this.tableViewer;
	}

	/**
	 * Listener for the clear button.
	 *
	 * @author Reiner Jung
	 *
	 */
	private class ClearButtonListener implements Listener {

		private final TableViewer tableViewer;
		private final String decoratorId;

		/* default */ ClearButtonListener(final TableViewer tableViewer, final String decoratorId) {
			this.tableViewer = tableViewer;
			this.decoratorId = decoratorId;
		}

		@Override
		public void handleEvent(final Event event) {
			@SuppressWarnings("unchecked")
			final List<? extends AbstractViolationMarker> violationMarkers = (List<? extends AbstractViolationMarker>) this.tableViewer
					.getInput();
			ClearViolationsViewJob.startAsyncAnalysis(violationMarkers, this.decoratorId);
		}
	}
}
