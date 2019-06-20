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
package qa.eclipse.plugin.bundles.checkstyle.markers;

import java.io.File;

import org.eclipse.core.resources.IMarker;

/**
 *
 * @author Christian Wulf
 *
 */
public class CheckstyleViolationMarker {

	private final IMarker marker;

	/**
	 *
	 * @param marker
	 *            marker
	 */
	public CheckstyleViolationMarker(final IMarker marker) {
		super();
		this.marker = marker;
	}

	public IMarker getMarker() {
		return this.marker;
	}

	/**
	 * @return the priority (3 highest to 0 lowest) or -1 otherwise.
	 */
	public int getSeverityLevelIndex() {
		return this.marker.getAttribute(CheckstyleMarkersUtils.ATTR_KEY_PRIORITY, -1);
	}

	/**
	 * @return the line number or 0 otherwise.
	 */
	public int getLineNumer() {
		return this.marker.getAttribute(IMarker.LINE_NUMBER, 0);
	}

	/**
	 * @return the violation message or the empty string otherwise.
	 */
	public String getMessage() {
		return this.marker.getAttribute(IMarker.MESSAGE, "");
	}

	public String getCheckName() {
		return this.marker.getAttribute(CheckstyleMarkersUtils.ATTR_KEY_CHECK_NAME, "");
	}

	public String getCheckPackageName() {
		return this.marker.getAttribute(CheckstyleMarkersUtils.ATTR_KEY_CHECK_PACKAGE, "");
	}

	public String getProjectName() {
		return this.marker.getResource().getProject().getName();
	}

	public String getDirectoryPath() {
		final File file = this.marker.getResource().getRawLocation().toFile();
		return file.getParent();
	}

	public String getFileName() {
		final File file = this.marker.getResource().getRawLocation().toFile();
		return file.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.marker == null) ? 0 : this.marker.hashCode());
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
		final CheckstyleViolationMarker other = (CheckstyleViolationMarker) obj;
		if (this.marker == null) {
			if (other.marker != null) {
				return false;
			}
		} else if (!this.marker.equals(other.marker)) {
			return false;
		}
		return true;
	}

}
