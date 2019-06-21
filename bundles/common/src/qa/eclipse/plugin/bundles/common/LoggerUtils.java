/***************************************************************************
 * Copyright (C) 2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package qa.eclipse.plugin.bundles.common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * Logs errors into the Eclipse error log.
 *
 * @author Reiner Jung
 *
 */
public final class LoggerUtils {

	private static final String PLUGIN_ID = Activator.PLUGIN_ID;

	private static final Plugin ACTIVATOR = Activator.getDefault();

	private LoggerUtils() {
		// utility class
	}

	/**
	 * Log errors based on exceptions.
	 *
	 * @param message
	 *            the message for the error
	 * @param throwable
	 *            the exception value
	 */
	public static void logThrowable(final String message, final Throwable throwable) {
		final IStatus status = new Status(IStatus.ERROR, LoggerUtils.PLUGIN_ID, message, throwable);
		LoggerUtils.ACTIVATOR.getLog().log(status);
	}

	/**
	 * Log warnings.
	 *
	 * @param message
	 *            warning message
	 */
	public static void logWarning(final String message) {
		final IStatus status = new Status(IStatus.WARNING, LoggerUtils.PLUGIN_ID, message);
		LoggerUtils.ACTIVATOR.getLog().log(status);
	}
}
