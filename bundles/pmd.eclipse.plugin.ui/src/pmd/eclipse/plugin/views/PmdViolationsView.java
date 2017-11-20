package pmd.eclipse.plugin.views;

import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_LINENUMBER;
import static pmd.eclipse.plugin.views.PmdViolationMarkerComparator.SORT_PROP_PRIORITY;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import net.sourceforge.pmd.RulePriority;
import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.icons.AnnotationImageProvider;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.markers.PmdViolationMarker;

public class PmdViolationsView extends ViewPart implements ISelectionChangedListener, IResourceChangeListener {

	private static final String PART_NAME_FORMAT_STRING = "PMD Violations (%d)";
	private static final String NUMBER_OF_PMD_VIOLATIONS = "Number of PMD Violations: ";

	// tutorial used from
	// http://www.vogella.com/tutorials/EclipseJFaceTableAdvanced/article.html

	private Label label;
	private TableViewer tableViewer;

	private final ViewerComparator comparator = new PmdViolationMarkerComparator();

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		label = new Label(parent, SWT.NONE);

		tableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableViewer.setUseHashlookup(true);
		// activate the tooltip support for the viewer
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);

		createColumns();

		// tableColumnLayout.setColumnData(column, new ColumnWeightData(20, 200, true));

		// configure table
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);

		tableViewer.setComparator(comparator);
		// on selection: opens the corresponding file in the proper editor, jumps to the
		// line, and selects it
		tableViewer.addSelectionChangedListener(this);
		// arrow down symbol: opens a menu with five priority-based filters
		ViewerFilter viewerFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				PmdViolationMarker marker = (PmdViolationMarker) element;
				// TODO Auto-generated method stub
				return true;
			}
		};
		tableViewer.addFilter(viewerFilter);
		// interprets the input and transforms it into rows
		tableViewer.setContentProvider(new ArrayContentProvider());

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

	private void createColumns() {
		TableViewerColumn tableViewerColumn;
		TableColumn column;

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				PmdViolationMarker violationMarker = (PmdViolationMarker) element;
				int pmdPriority = violationMarker.getPriority();
				String imageRegistryKey = AnnotationImageProvider.getImageRegistryKeyByPriority(pmdPriority);
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
		column.setWidth(200);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_RULENAME));

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
		column.setWidth(200);

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
		column.setWidth(50);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_LINENUMBER));

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
		column.setWidth(400);

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
		column.setWidth(100);
		column.addSelectionListener(new CompareOnSelectListener(tableViewer, SORT_PROP_RULESET));
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
				tableViewer.setInput(pmdViolationMarkers);
			}
		});
	}

}
