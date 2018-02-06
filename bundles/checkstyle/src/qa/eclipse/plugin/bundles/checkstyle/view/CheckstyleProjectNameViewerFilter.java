package qa.eclipse.plugin.bundles.checkstyle.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;

class CheckstyleProjectNameViewerFilter extends ViewerFilter {

	private String projectName;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
		if (projectName == null) {
			return true;
		}
		return marker.getProjectName().equals(projectName);
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
