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
package qa.eclipse.plugin.bundles.checkstyle.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.markers.CheckstyleViolationMarker;

/**
 *
 * @author Christian Wulf
 *
 */
class CheckstylePriorityViewerFilter extends ViewerFilter {

	/** default value is 3 from the range 0..3. */
	private int selectionIndex = SeverityLevel.ERROR.ordinal() - SeverityLevel.IGNORE.ordinal();

	public CheckstylePriorityViewerFilter() {
		super();
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		final CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
		final int severityLevelIndex = marker.getSeverityLevelIndex();

		final int transformedSeverityLevelIndex = SeverityLevel.ERROR.ordinal() - severityLevelIndex;

		// example: 3 means "at least IGNORE",
		// so transformed severity must be less or equal selectionIndex
		return transformedSeverityLevelIndex <= this.selectionIndex;
	}

	public void setSelectionIndex(final int selectionIndex) {
		this.selectionIndex = selectionIndex;
	}

	public int getLowestPriority() {
		return this.selectionIndex;
	}

}
