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
package qa.eclipse.plugin.pmd.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

/**
 *
 * @author Christian Wulf
 *
 */
public class PmdProjectNameViewerFilter extends ViewerFilter {

	private String projectName;

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		final PmdViolationMarker marker = (PmdViolationMarker) element;
		if (this.projectName == null) {
			return true;
		}
		return marker.getProjectName().equals(this.projectName);
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

}
