package qa.eclipse.plugin.pmd.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

public class PmdProjectNameViewerFilter extends ViewerFilter {

	private String projectName;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		PmdViolationMarker marker = (PmdViolationMarker) element;
		if (projectName == null) {
			return true;
		}
		return marker.getProjectName().equals(projectName);
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
