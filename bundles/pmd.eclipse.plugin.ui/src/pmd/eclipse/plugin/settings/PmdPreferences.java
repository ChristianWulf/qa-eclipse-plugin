package pmd.eclipse.plugin.settings;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import pmd.eclipse.plugin.PmdUIPlugin;

public class PmdPreferences {

	public static final PmdPreferences INSTANCE = new PmdPreferences(PmdUIPlugin.PLUGIN_ID);

	private static final IScopeContext INSTANCE_SCOPE = InstanceScope.INSTANCE;

	private final Map<IProject, IScopeContext> projectScopeByProject = new HashMap<>();

	private final String node;

	private PmdPreferences(String node) {
		// private singleton constructor
		this.node = node;
	}

	public IEclipsePreferences getDefaultPreferences() {
		IEclipsePreferences preferences = DefaultScope.INSTANCE.getNode(node);

		return preferences;
	}

	public IEclipsePreferences getEclipseScopedPreferences() {
		IEclipsePreferences preferences = INSTANCE_SCOPE.getNode(node);
		return preferences;
	}

	public IEclipsePreferences getProjectScopedPreferences(IProject project) {
		IScopeContext projectPref;
		if (projectScopeByProject.containsKey(project)) {
			projectPref = projectScopeByProject.get(project);
		} else {
			projectPref = new ProjectScope(project);
			projectScopeByProject.put(project, projectPref);
		}

		IEclipsePreferences preferences = projectPref.getNode(node);
		return preferences;
	}

}
