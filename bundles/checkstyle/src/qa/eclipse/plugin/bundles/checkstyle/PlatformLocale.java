package qa.eclipse.plugin.bundles.checkstyle;

import java.util.Locale;

import org.eclipse.core.runtime.Platform;

public final class PlatformLocale {

	private PlatformLocale() {
		// utility class
	}

	/**
	 * Helper method to get the current plattform locale.
	 *
	 * @return the platform locale
	 */
	public static Locale getPlatformLocale() {
		String nl = Platform.getNL();
		String[] parts = nl.split("_"); //$NON-NLS-1$

		String language = parts.length > 0 ? parts[0] : ""; //$NON-NLS-1$
		String country = parts.length > 1 ? parts[1] : ""; //$NON-NLS-1$
		String variant = parts.length > 2 ? parts[2] : ""; //$NON-NLS-1$

		return new Locale(language, country, variant);
	}
}
