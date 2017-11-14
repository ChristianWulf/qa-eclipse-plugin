package pmd.eclipse.plugin.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.RuleViolation;

public final class PmdMarkers {

	private PmdMarkers() {
		// utility class
	}

	public static final String PMD_VIOLATION_MARKER = "pmd.eclipse.plugin.markers.violation";

	/**
	 * @see {@link net.sourceforge.pmd.RulePriority}
	 */
	public static final String ATTR_KEY_PRIORITY = "pmd.priority";

	public static void appendViolationMarker(IFile eclipseFile, RuleViolation violation) throws CoreException {
		IMarker marker = eclipseFile.createMarker(PmdMarkers.PMD_VIOLATION_MARKER);
		marker.setAttribute(IMarker.MESSAGE, violation.getDescription());
		marker.setAttribute(PmdMarkers.ATTR_KEY_PRIORITY, violation.getRule().getPriority().getPriority());
		marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

}
