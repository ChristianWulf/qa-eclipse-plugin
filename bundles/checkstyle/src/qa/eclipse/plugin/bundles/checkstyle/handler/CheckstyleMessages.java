package qa.eclipse.plugin.bundles.checkstyle.handler;

import org.eclipse.osgi.util.NLS;

public class CheckstyleMessages extends NLS {

	private static final String BUNDLE_NAME = CheckstyleMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, CheckstyleMessages.class);
	}

	private CheckstyleMessages() {
		// utility class
	}

	public static String getBundleName() {
		return BUNDLE_NAME;
	}
}
