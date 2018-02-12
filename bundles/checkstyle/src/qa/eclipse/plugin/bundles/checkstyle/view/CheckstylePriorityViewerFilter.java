package qa.eclipse.plugin.bundles.checkstyle.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;

class CheckstylePriorityViewerFilter extends ViewerFilter {

	/** default value is 3 from 0..3 */
	private int selectionIndex = SeverityLevel.ERROR.ordinal() - SeverityLevel.IGNORE.ordinal();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
		int severityLevelIndex = marker.getSeverityLevelIndex();

		int transformedSeverityLevelIndex = SeverityLevel.ERROR.ordinal() - severityLevelIndex;

		// example: 3 means "at least IGNORE",
		// so transformed severity must be less or equal selectionIndex
		return transformedSeverityLevelIndex <= selectionIndex;
	}

	public void setSelectionIndex(int selectionIndex) {
		this.selectionIndex = selectionIndex;
	}

	public int getLowestPriority() {
		return selectionIndex;
	}

}
