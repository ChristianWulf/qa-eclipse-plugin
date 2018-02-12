package qa.eclipse.plugin.bundles.checkstyle.marker;

import java.io.File;

import org.eclipse.core.resources.IMarker;

public class CheckstyleViolationMarker {

	private final IMarker marker;

	public CheckstyleViolationMarker(IMarker marker) {
		super();
		this.marker = marker;
	}

	public IMarker getMarker() {
		return marker;
	}

	/**
	 * @return the priority (3 highest to 0 lowest) or -1 otherwise.
	 */
	public int getSeverityLevelIndex() {
		return marker.getAttribute(CheckstyleMarkers.ATTR_KEY_PRIORITY, -1);
	}

	/**
	 * @return the line number or 0 otherwise.
	 */
	public int getLineNumer() {
		return marker.getAttribute(IMarker.LINE_NUMBER, 0);
	}

	/**
	 * @return the violation message or the empty string otherwise.
	 */
	public String getMessage() {
		return marker.getAttribute(IMarker.MESSAGE, "");
	}

	public String getCheckName() {
		return marker.getAttribute(CheckstyleMarkers.ATTR_KEY_CHECK_NAME, "");
	}

	public String getCheckPackageName() {
		return marker.getAttribute(CheckstyleMarkers.ATTR_KEY_CHECK_PACKAGE, "");
	}

	public String getProjectName() {
		return marker.getResource().getProject().getName();
	}

	public String getDirectoryPath() {
		File file = marker.getResource().getRawLocation().toFile();
		return file.getParent();
	}

	public String getFileName() {
		File file = marker.getResource().getRawLocation().toFile();
		return file.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((marker == null) ? 0 : marker.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckstyleViolationMarker other = (CheckstyleViolationMarker) obj;
		if (marker == null) {
			if (other.marker != null)
				return false;
		} else if (!marker.equals(other.marker))
			return false;
		return true;
	}

}
