package qa.eclipse.plugin.bundles.checkstyle.preference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Before;
import org.junit.Test;

public class CheckstylePreferencesTest {

	private IEclipsePreferences preferences;

	@Before
	public void before() {
		preferences = DefaultScope.INSTANCE.getNode("test node");
		preferences.put("empty", "");
		preferences.put("single", "path/to/jar");
		preferences.put("multiple", "path/to/jar  ,  path/to/another/jar");
	}

	@Test(expected = NullPointerException.class)
	public void putNull() throws Exception {
		preferences.put("null", null);
	}

	@Test
	public void splitNullValue() throws Exception {
		String propertyValue = preferences.get("null", null);

		assertThat(propertyValue, is(nullValue()));
	}

	@Test
	public void splitDefaultStringValue() throws Exception {
		String propertyValue = preferences.get("null", "");
		String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}

	@Test
	public void splitEmptyValue() throws Exception {
		String propertyValue = preferences.get("empty", null);
		String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}

	@Test
	public void splitSingleValue() throws Exception {
		String propertyValue = preferences.get("single", null);
		String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}

	@Test
	public void splitMultiValue() throws Exception {
		String propertyValue = preferences.get("multiple", null);
		String[] propertyValueParts = propertyValue.split(",");

		assertThat(propertyValueParts, is(instanceOf(String[].class)));
	}
}
