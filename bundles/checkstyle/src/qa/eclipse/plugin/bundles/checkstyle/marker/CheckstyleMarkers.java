package qa.eclipse.plugin.bundles.checkstyle.marker;

import java.util.HashMap;
import java.util.Map;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

public final class CheckstyleMarkers {

	public static final String ABSTRACT_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.markers.violation";
	public static final String ERROR_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.markers.violation.error";
	public static final String WARNING_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.markers.violation.warning";
	public static final String INFO_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.markers.violation.info";
	public static final String IGNORE_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.markers.violation.ignore";

	private static final Map<Integer, String> MARKER_TYPE_BY_PRIORITY = new HashMap<Integer, String>();

	static {
		MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.ERROR.ordinal(), ERROR_CHECKSTYLE_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.WARNING.ordinal(), WARNING_CHECKSTYLE_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.INFO.ordinal(), INFO_CHECKSTYLE_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.IGNORE.ordinal(), IGNORE_CHECKSTYLE_VIOLATION_MARKER);
	}

	private CheckstyleMarkers() {
		// utility class
	}
}
