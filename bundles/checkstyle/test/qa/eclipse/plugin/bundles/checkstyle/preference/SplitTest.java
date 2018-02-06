package qa.eclipse.plugin.bundles.checkstyle.preference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;

import org.junit.Test;

public class SplitTest {

	@Test
	public void splitByWhitespaceCommaWhitespace() throws Exception {
		String s = "one,two,  three   ,  four";
		String[] parts = s.split("\\s*,\\s*");

		assertThat(parts, arrayContaining("one", "two", "three", "four"));
	}

	@Test
	public void splitByPureComma() throws Exception {
		String s = "one,two,  three   ,  four";
		String[] parts = s.split(",");

		assertThat(parts, arrayContaining("one", "two", "  three   ", "  four"));
	}
}
