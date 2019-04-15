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
package qa.eclipse.plugin.bundles.checkstyle.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

/**
 *
 * @author Christian Wulf
 *
 */
class CheckstylePreferenceChangeListener implements IPreferenceChangeListener {

	private final IProject project;

	public CheckstylePreferenceChangeListener(final IProject project) {
		this.project = project;
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent event) {
		if (event.getKey() == CheckstylePreferences.PROP_KEY_ENABLED) {
			final String newEnabled = (String) event.getNewValue();
			final Boolean enabled = Boolean.valueOf(newEnabled);
			if (!enabled) { // remove all violation markers
				final String jobName = String.format("Removing Checkstyle violations for project '%s'...",
						this.project.getName());

				CheckstyleRemoveMarkersJob.start(jobName, this.project);
			}
		}
	}

}
