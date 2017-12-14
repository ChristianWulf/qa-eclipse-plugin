package pmd.eclipse.plugin.ui;

import org.eclipse.osgi.util.NLS;

public class PmdMessages extends NLS {

	private static final String BUNDLE_NAME = PmdMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, PmdMessages.class);
	}

	private PmdMessages() {
		// utility class
	}

	public static String getBundleName() {
		return BUNDLE_NAME;
	}
}
