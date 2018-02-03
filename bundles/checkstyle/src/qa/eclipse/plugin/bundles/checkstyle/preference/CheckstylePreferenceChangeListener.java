package qa.eclipse.plugin.bundles.checkstyle.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

class CheckstylePreferenceChangeListener implements IPreferenceChangeListener {

	private final CheckstylePreferences checkstylePreferences;
	private final IProject project;
	private final IEclipsePreferences preferences;

	public CheckstylePreferenceChangeListener(CheckstylePreferences checkstylePreferences, IProject project,
			IEclipsePreferences preferences) {
		this.checkstylePreferences = checkstylePreferences;
		this.project = project;
		this.preferences = preferences;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
