package pmdeclipseplugin.eclipse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * 
 * @author Christian Wulf
 *
 * @see <a href=
 *      "https://stackoverflow.com/questions/15426158/loading-of-osgi-bundle-dynamically-from-a-file-system">
 *      https://stackoverflow.com/questions/15426158/loading-of-osgi-bundle-dynamically-from-a-file-system
 *      </a>
 */
public class DynamicBundleLoader {

	private final BundleContext context;

	public DynamicBundleLoader(BundleContext context) {
		this.context = context;
	}

	public Bundle loadCustomRuleSetBundle(String location) throws BundleException {
		Bundle bundle = install(location);
		return bundle;
	}

	public Bundle install(String location) throws BundleException {
		Bundle b = context.installBundle(location);
		b.start();
		return b;
	}

	public void uninstall(String location) throws BundleException {
		Bundle b = context.getBundle(location);
		b.uninstall();
	}
}
