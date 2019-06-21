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
package qa.eclipse.plugin.bundles.checkstyle.handler;

import org.eclipse.osgi.util.NLS;

/**
 *
 * @author Christian Wulf
 *
 */
public final class CheckstyleMessages extends NLS {

	private static final String BUNDLE_NAME = CheckstyleMessages.class.getName();

	static {
		NLS.initializeMessages(CheckstyleMessages.BUNDLE_NAME, CheckstyleMessages.class);
	}

	private CheckstyleMessages() { // NOPMD (CallSuperInConstructor) utility class
		// utility class
	}

	public static String getBundleName() {
		return CheckstyleMessages.BUNDLE_NAME;
	}
}
