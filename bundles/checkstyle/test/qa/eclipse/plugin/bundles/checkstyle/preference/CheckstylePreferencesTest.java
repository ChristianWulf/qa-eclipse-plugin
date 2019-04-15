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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Before;
import org.junit.Test;

/**
 * Test checkstyle preferences.
 * 
 * @author Christian Wulf
 *
 */
public class CheckstylePreferencesTest { // NOPMD test class no constructor

	private IEclipsePreferences preferences;

	@Before
	public void before() {
		this.preferences = DefaultScope.INSTANCE.getNode("test node");
		this.preferences.put("empty", "");
		this.preferences.put("single", "path/to/jar");
		this.preferences.put("multiple", "path/to/jar  ,  path/to/another/jar");
	}

	@Test(expected = NullPointerException.class)
	public void putNull() throws Exception {
		this.preferences.put("null", null);
	}

	@Test
	public void splitNullValue() throws Exception {
		final String propertyValue = this.preferences.get("null", null);

		assertThat(propertyValue, is(nullValue()));
	}

	@Test
	public void splitDefaultStringValue() throws Exception {
		final String propertyValue = this.preferences.get("null", "");
		final String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}

	@Test
	public void splitEmptyValue() throws Exception {
		final String propertyValue = this.preferences.get("empty", null);
		final String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}

	@Test
	public void splitSingleValue() throws Exception {
		final String propertyValue = this.preferences.get("single", null);
		final String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}

	@Test
	public void splitMultiValue() throws Exception {
		final String propertyValue = this.preferences.get("multiple", null);
		final String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}
}
