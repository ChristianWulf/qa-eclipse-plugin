/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
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
package qa.eclipse.plugin.bundles.checkstyle;

import java.util.Locale;

import org.eclipse.core.runtime.Platform;

public final class EclipsePlatform {

	private EclipsePlatform() {
		// utility class
	}

	/**
	 * Helper method to get the current platform locale.
	 *
	 * @return the platform locale
	 */
	public static Locale getLocale() {
		final String nl = Platform.getNL();
		final String[] parts = nl.split("_"); //$NON-NLS-1$

		final String language = parts.length > 0 ? parts[0] : ""; //$NON-NLS-1$
		final String country = parts.length > 1 ? parts[1] : ""; //$NON-NLS-1$
		final String variant = parts.length > 2 ? parts[2] : ""; //$NON-NLS-1$

		return new Locale(language, country, variant);
	}
}
