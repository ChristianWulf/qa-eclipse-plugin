package pmd.eclipse.plugin.markers;

import java.io.File;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class PmdViolationMarker {

	private final IMarker marker;

	public PmdViolationMarker(IMarker marker) {
		this.marker = marker;
	}

	/**
	 * @return the line number or 0 otherwise.
	 */
	public int getLineNumer() {
		return marker.getAttribute(IMarker.LINE_NUMBER, 0);
	}

	/**
	 * @return the priority (1 highest to 5 lowest) or 0 otherwise.
	 */
	public int getPriority() {
		return marker.getAttribute(PmdMarkers.ATTR_KEY_PRIORITY, 0);
	}

	/**
	 * @return the violation message or the empty string otherwise.
	 */
	public String getMessage() {
		return marker.getAttribute(IMarker.MESSAGE, "");
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

	public String getRuleName() {
		return marker.getAttribute(PmdMarkers.ATTR_KEY_RULENAME, "unknown");
	}

	public String getRuleSetName() {
		return marker.getAttribute(PmdMarkers.ATTR_KEY_RULESETNAME, "unknown");
	}

	public IMarker getMarker() {
		return marker;
	}

	@SuppressWarnings("unchecked")
	public Comparable<Object> getAttribute(String markerAttributeKey) {
		try {
			return (Comparable<Object>) marker.getAttribute(markerAttributeKey);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
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
		PmdViolationMarker other = (PmdViolationMarker) obj;
		if (marker == null) {
			if (other.marker != null)
				return false;
		} else if (!marker.equals(other.marker))
			return false;
		return true;
	}

	// public Comparable<?> getAttributeValueByIndex(int creationColumnIndex) {
	// switch (creationColumnIndex) {
	// case 0: {
	// return getPriority();
	// }
	// case 1: {
	// return getRuleName();
	// }
	// case 2: {
	// return getDirectoryPath();
	// }
	// case 3: {
	// return getFileName();
	// }
	// case 4: {
	// return getLineNumer();
	// }
	// case 5: {
	// return getMessage();
	// }
	// case 6: {
	// return getRuleSetName();
	// }
	// case 7: {
	// return getProjectName();
	// }
	// default: {
	// return 0;
	// }
	// }
	// }

}
