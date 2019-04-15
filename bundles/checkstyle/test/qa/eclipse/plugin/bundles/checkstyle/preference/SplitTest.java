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
package qa.eclipse.plugin.bundles.checkstyle.preference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;

import org.junit.Test;

/**
 *
 * @author Christian Wulf
 *
 */
public class SplitTest { // NOPMD test class no constructor

	@Test
	public void splitByWhitespaceCommaWhitespace() throws Exception {
		final String s = "one,two,  three   ,  four";
		final String[] parts = s.split("\\s*,\\s*");

		assertThat(parts, arrayContaining("one", "two", "three", "four"));
	}

	@Test
	public void splitByPureComma() throws Exception {
		final String s = "one,two,  three   ,  four";
		final String[] parts = s.split(",");

		assertThat(parts, arrayContaining("one", "two", "  three   ", "  four"));
	}
}
