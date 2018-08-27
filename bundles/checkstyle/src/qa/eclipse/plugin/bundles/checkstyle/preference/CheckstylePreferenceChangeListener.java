package qa.eclipse.plugin.bundles.checkstyle.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

class CheckstylePreferenceChangeListener implements IPreferenceChangeListener {

	private final IProject project;

	public CheckstylePreferenceChangeListener(IProject project) {
		this.project = project;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey() == CheckstylePreferences.PROP_KEY_ENABLED) {
			String newEnabled = (String) event.getNewValue();
			Boolean enabled = Boolean.valueOf(newEnabled);
			if (!enabled) { // remove all violation markers
				String jobName = String.format("Removing Checkstyle violations for project '%s'...", project.getName());

				CheckstyleRemoveMarkersJob.start(jobName, project);
			}
		}
	}

}
