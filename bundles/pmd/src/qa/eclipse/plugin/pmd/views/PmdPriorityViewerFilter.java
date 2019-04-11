package qa.eclipse.plugin.pmd.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.sourceforge.pmd.RulePriority;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

class PmdPriorityViewerFilter extends ViewerFilter {

	private int lowestPriority = RulePriority.LOW.getPriority();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		PmdViolationMarker marker = (PmdViolationMarker) element;
		return marker.getPriority() <= lowestPriority;
	}

	public void setLowestPriority(int lowestPriority) {
		this.lowestPriority = lowestPriority;
	}

	public int getLowestPriority() {
		return lowestPriority;
	}

}
