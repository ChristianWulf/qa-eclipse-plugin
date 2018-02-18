package qa.eclipse.plugin.bundles.common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public final class Logger {

	private static final String PLUGIN_ID = Activator.PLUGIN_ID;

	private static Plugin ACTIVATOR = Activator.getDefault();

	private Logger() {
		// utility class
	}

	public static void logThrowable(String message, Throwable throwable) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
		ACTIVATOR.getLog().log(status);
	}

	public static void logWarning(String message) {
		IStatus status = new Status(IStatus.WARNING, PLUGIN_ID, message);
		ACTIVATOR.getLog().log(status);
	}
}
