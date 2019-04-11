package qa.eclipse.plugin.pmd.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

class PmdPreferenceChangeListener implements IPreferenceChangeListener {

	private final IProject project;

	public PmdPreferenceChangeListener(IProject project) {
		this.project = project;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey() == PmdPreferences.PROP_KEY_ENABLED) {
			String newEnabled = (String) event.getNewValue();
			Boolean enabled = Boolean.valueOf(newEnabled);
			if (!enabled) { // remove all violation markers
				String jobName = String.format("Removing PMD violations for project '%s'...", project.getName());

				PmdRemoveMarkersJob.start(jobName, project);
			}
		}
	}

}
