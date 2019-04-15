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

import net.sourceforge.pmd.RulePriority;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

/**
 *
 * @author Christian Wulf
 *
 */
class PmdPriorityViewerFilter extends ViewerFilter {

	private int lowestPriority = RulePriority.LOW.getPriority();

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		final PmdViolationMarker marker = (PmdViolationMarker) element;
		return marker.getPriority() <= this.lowestPriority;
	}

	public void setLowestPriority(final int lowestPriority) {
		this.lowestPriority = lowestPriority;
	}

	public int getLowestPriority() {
		return this.lowestPriority;
	}

}
