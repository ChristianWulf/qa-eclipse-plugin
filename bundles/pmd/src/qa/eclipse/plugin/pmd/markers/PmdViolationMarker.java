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
package qa.eclipse.plugin.pmd.markers;

import java.io.File;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class PmdViolationMarker {

	private final IMarker marker;

	public PmdViolationMarker(final IMarker marker) {
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
		final File file = marker.getResource().getRawLocation().toFile();
		return file.getParent();
	}

	public String getFileName() {
		final File file = marker.getResource().getRawLocation().toFile();
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
	public Comparable<Object> getAttribute(final String markerAttributeKey) {
		try {
			return (Comparable<Object>) marker.getAttribute(markerAttributeKey);
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((marker == null) ? 0 : marker.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final PmdViolationMarker other = (PmdViolationMarker) obj;
		if (marker == null) {
			if (other.marker != null) {
				return false;
			}
		} else if (!marker.equals(other.marker)) {
			return false;
		}
		return true;
	}

}
