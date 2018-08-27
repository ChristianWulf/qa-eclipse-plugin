package pmd.eclipse.plugin.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

public final class PmdMarkers {

	/** marker to delete violation and error markers */
	public static final String ABSTRACT_PMD_VIOLATION_COMMON = "pmd.eclipse.plugin.markers.common";
	/** marker to identify violation marker for the violations view */
	public static final String ABSTRACT_PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation";
	public static final String HIGH_PMD_VIOLATION_MARKER = ABSTRACT_PMD_VIOLATION_MARKER + ".high";
	public static final String MEDIUMHIGH_PMD_VIOLATION_MARKER = ABSTRACT_PMD_VIOLATION_MARKER + ".mediumhigh";
	public static final String MEDIUM_PMD_VIOLATION_MARKER = ABSTRACT_PMD_VIOLATION_MARKER + ".medium";
	public static final String MEDIUMLOW_PMD_VIOLATION_MARKER = ABSTRACT_PMD_VIOLATION_MARKER + ".mediumlow";
	public static final String LOW_PMD_VIOLATION_MARKER = ABSTRACT_PMD_VIOLATION_MARKER + ".low";
	public static final String PMD_ERROR_MARKER = ABSTRACT_PMD_VIOLATION_MARKER + ".error";

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

		marker.setAttribute(ATTR_KEY_PRIORITY, priority);
		marker.setAttribute(ATTR_KEY_RULENAME, violation.getRule().getName());
		marker.setAttribute(ATTR_KEY_RULESETNAME, violation.getRule().getRuleSetName());

		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

	public static IMarker[] findAllInWorkspace() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IMarker[] markers;
		try {
			markers = workspaceRoot.findMarkers(ABSTRACT_PMD_VIOLATION_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
		return markers;
	}

	public static void deleteMarkers(IResource resource) throws CoreException {
		resource.deleteMarkers(PmdMarkers.ABSTRACT_PMD_VIOLATION_COMMON, true, IResource.DEPTH_INFINITE);
	}
}
