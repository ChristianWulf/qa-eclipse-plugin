/***************************************************************************
 * Copyright (C) 2019
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
package qa.eclipse.plugin.bundles.common.markers;

import java.io.File;

import org.eclipse.core.resources.IMarker;

/**
 * @author Reiner Jung
 *
 */
public abstract class AbstractViolationMarker {

	protected final IMarker marker;

	/**
	 *
	 * @param marker
	 *            marker
	 */
	public AbstractViolationMarker(final IMarker marker) {
		this.marker = marker;
	}

	public IMarker getMarker() {
		return this.marker;
	}

	/**
	 * @return the priority or a default value
	 */
	public abstract int getPriority();

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

	/**
	 * @return the name of the check or rule applied to the code.
	 */
	public abstract String getRuleName();

	/**
	 * @return the rule-set, package, bundle or category where the rule belongs to.
	 */
	public abstract String getRuleSetName();

	/**
	 * @return returns the name of the project
	 */
	public String getProjectName() {
		return this.marker.getResource().getProject().getName();
	}

	/**
	 * Get the string representing the directory of the file which this is a marker from.
	 *
	 * @return directory name
	 */
	public String getDirectoryPath() {
		final File file = this.marker.getResource().getRawLocation().toFile();
		return file.getParent();
	}

	/**
	 * Get the string representing the file which is associated to this marker.
	 *
	 * @return file name
	 */
	public String getFileName() {
		final File file = this.marker.getResource().getRawLocation().toFile();
		return file.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (this.marker == null ? 0 : this.marker.hashCode()); // NOPMD
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
		final AbstractViolationMarker other = (AbstractViolationMarker) obj;
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
