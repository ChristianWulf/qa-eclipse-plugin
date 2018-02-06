package qa.eclipse.plugin.bundles.checkstyle.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;

class CheckstylePriorityViewerFilter extends ViewerFilter {

	private int lowestPriority = SeverityLevel.IGNORE.ordinal();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
		return marker.getSeverityLevelIndex() >= lowestPriority;
	}

	public void setLowestPriority(int lowestPriority) {
		this.lowestPriority = lowestPriority;
	}

	public int getLowestPriority() {
		return lowestPriority;
	}

}
