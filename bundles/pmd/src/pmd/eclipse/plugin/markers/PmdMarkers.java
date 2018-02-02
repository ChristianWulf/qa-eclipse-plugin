package pmd.eclipse.plugin.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

public final class PmdMarkers {

	public static final String ABSTRACT_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation";
	public static final String HIGH_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation.high";
	public static final String MEDIUMHIGH_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation.mediumhigh";
	public static final String MEDIUM_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation.medium";
	public static final String MEDIUMLOW_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation.mediumlow";
	public static final String LOW_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation.low";

	private static final Map<Integer, String> MARKER_TYPE_BY_PRIORITY = new HashMap<Integer, String>();

	static {
		MARKER_TYPE_BY_PRIORITY.put(RulePriority.HIGH.getPriority(), HIGH_PMD_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(2, MEDIUMHIGH_PMD_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(3, MEDIUM_PMD_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(4, MEDIUMLOW_PMD_VIOLATION_MARKER);
		MARKER_TYPE_BY_PRIORITY.put(5, LOW_PMD_VIOLATION_MARKER);
	}

	private PmdMarkers() {
		// utility class
	}

	/**
	 * @see {@link net.sourceforge.pmd.RulePriority}
	 */
	public static final String ATTR_KEY_PRIORITY = "pmd.priority";
	public static final String ATTR_KEY_RULENAME = "pmd.rulename";
	public static final String ATTR_KEY_RULESETNAME = "pmd.rulesetname";

	public static void appendViolationMarker(IFile eclipseFile, RuleViolation violation) throws CoreException {
		int priority = violation.getRule().getPriority().getPriority();
		String markerType = MARKER_TYPE_BY_PRIORITY.get(priority);

		IMarker marker = eclipseFile.createMarker(markerType);
		marker.setAttribute(IMarker.MESSAGE, violation.getDescription());
		marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

		marker.setAttribute(PmdMarkers.ATTR_KEY_PRIORITY, priority);
		marker.setAttribute(PmdMarkers.ATTR_KEY_RULENAME, violation.getRule().getName());
		marker.setAttribute(PmdMarkers.ATTR_KEY_RULESETNAME, violation.getRule().getRuleSetName());

		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

}
