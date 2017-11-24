package pmd.eclipse.plugin.experimental;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.FrameworkUtil;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String PREFERENCE_NODE = FrameworkUtil.getBundle(PreferenceInitializer.class).getSymbolicName();

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(PREFERENCE_NODE);
		// defaultPreferences.put(key, value); // TODO
	}

}
