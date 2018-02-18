package pmd.eclipse.plugin.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

class PmdPreferenceChangeListener implements IPreferenceChangeListener {

	private final PmdPreferences pmdPreferences;
	private final IProject project;
	private final IEclipsePreferences preferences;

	public PmdPreferenceChangeListener(PmdPreferences pmdPreferences, IProject project,
			IEclipsePreferences preferences) {
		this.pmdPreferences = pmdPreferences;
		this.project = project;
		this.preferences = preferences;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey() == PmdPreferences.PROP_KEY_ENABLED) {
			String newEnabled = (String) event.getNewValue();
			Boolean enabled = Boolean.valueOf(newEnabled);
			if (!enabled) { // remove all violation markers
				String jobName = String.format("Removing PMD violations for project '%s'...", project.getName());

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
				ISchedulingRule projectRule = ruleFactory.markerRule(project);

				Job job = new PmdRemoveMarkersJob(jobName, project);
				job.setRule(projectRule);
				job.setUser(true);
				job.schedule();
			}
		}

		pmdPreferences.updateRulsetCache(project, preferences);
	}

}
