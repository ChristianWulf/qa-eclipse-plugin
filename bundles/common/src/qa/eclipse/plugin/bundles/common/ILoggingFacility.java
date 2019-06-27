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

/**
 *
 * @author Reiner Jung
 *
 * @since 1.1.0
 */
public interface ILoggingFacility {

	/**
	 * Log message to view including the associated exception.
	 *
	 * @param message
	 *            message to display
	 * @param throwable
	 *            exception
	 */
	void logThrowable(final String message, final Throwable throwable);

	/**
	 * Log a warning message to the checkstyle summary view.
	 *
	 * @param message
	 *            the message to display
	 */
	void logWarning(final String message);
}
