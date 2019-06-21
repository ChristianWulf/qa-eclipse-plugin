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
package qa.eclipse.plugin.bundles.checkstyle.markers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.common.SplitUtils;

/**
 * Utility class to handle checkstyle markers.
 *
 * @author Christian Wulf
 *
 */
public final class CheckstyleMarkersUtils {

	/** marker to delete violation and error markers. */
	public static final String ABSTRACT_CHECKSTYLE_COMMON_MARKER = "qa.eclipse.plugin.checkstyle.markers.common";
	/** marker to identify violation marker for the violations view. */
	public static final String ABSTRACT_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.checkstyle.markers.violation";
	public static final String ERROR_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".error";
	public static final String WARNING_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".warning";
	public static final String INFO_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".info";
	public static final String IGNORE_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".ignore";
	public static final String EXCEPTION_CHECKSTYLE_MARKER = CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".exception";

	/**
	 * @see {@link com.puppycrawl.tools.checkstyle.api.SeverityLevel}
	 */
	public static final String ATTR_KEY_PRIORITY = "checkstyle.priority";
	public static final String ATTR_KEY_CHECK_PACKAGE = "checkstyle.check_package";
	public static final String ATTR_KEY_CHECK_NAME = "checkstyle.check_name";

	private static final int IMARKER_SEVERITY_OTHERS = 3;

	private static final Map<Integer, String> MARKER_TYPE_BY_PRIORITY = new ConcurrentHashMap<Integer, String>();

	static {
		CheckstyleMarkersUtils.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.ERROR.ordinal(), CheckstyleMarkersUtils.ERROR_CHECKSTYLE_VIOLATION_MARKER);
		CheckstyleMarkersUtils.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.WARNING.ordinal(), CheckstyleMarkersUtils.WARNING_CHECKSTYLE_VIOLATION_MARKER);
		CheckstyleMarkersUtils.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.INFO.ordinal(), CheckstyleMarkersUtils.INFO_CHECKSTYLE_VIOLATION_MARKER);
		CheckstyleMarkersUtils.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.IGNORE.ordinal(), CheckstyleMarkersUtils.IGNORE_CHECKSTYLE_VIOLATION_MARKER);
	}

	private CheckstyleMarkersUtils() {
		// utility class
	}

	/**
	 * Add a violation marker for a given file and a specific violation level.
	 *
	 * @param eclipseFile
	 *            the file
	 * @param violation
	 *            the audit level
	 * @throws CoreException
	 *             on various errors
	 */
	public static void appendViolationMarker(final IFile eclipseFile, final AuditEvent violation) throws CoreException {
		final int priority = violation.getSeverityLevel().ordinal();
		final String markerType = CheckstyleMarkersUtils.MARKER_TYPE_BY_PRIORITY.get(priority);

		final IMarker marker = eclipseFile.createMarker(markerType);
		marker.setAttribute(IMarker.MESSAGE, violation.getMessage());
		marker.setAttribute(IMarker.LINE_NUMBER, violation.getLine());
		// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

		marker.setAttribute(CheckstyleMarkersUtils.ATTR_KEY_PRIORITY, priority);
		// getModuleId() always returns null
		final String checkClassName = violation.getSourceName();
		final List<String> checkClassNameParts = SplitUtils.split(checkClassName).once().at('.').fromTheEnd();
		marker.setAttribute(CheckstyleMarkersUtils.ATTR_KEY_CHECK_PACKAGE, checkClassNameParts.get(0));
		marker.setAttribute(CheckstyleMarkersUtils.ATTR_KEY_CHECK_NAME, checkClassNameParts.get(1));

		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

	/**
	 * Find all markers in the workspace.
	 *
	 * @return return an array of markers
	 * @throws CoreException
	 *             in case an error occurs while finding markers
	 *
	 */
	public static IMarker[] findAllMarkers() throws CoreException {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return workspaceRoot.findMarkers(CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER, true, IResource.DEPTH_INFINITE);
	}

	/**
	 * Delete all markers for the given resource.
	 *
	 * @param resource
	 *            resource
	 * @throws CoreException
	 *             on errors while deleting
	 */
	public static void deleteMarkers(final IResource resource) throws CoreException {
		resource.deleteMarkers(CheckstyleMarkersUtils.ABSTRACT_CHECKSTYLE_COMMON_MARKER, true, IResource.DEPTH_INFINITE);
	}

	/**
	 * Append error markers during processing.
	 *
	 * @param eclipseFile
	 *            the file checked
	 * @param throwable
	 *            the error which occured during checking
	 * @throws CoreException
	 *             on errors while appending
	 */
	public static void appendProcessingErrorMarker(final IFile eclipseFile, final Throwable throwable) throws CoreException {
		final IMarker marker = eclipseFile.createMarker(CheckstyleMarkersUtils.EXCEPTION_CHECKSTYLE_MARKER);
		// whether it is displayed as error, warning, info or other in the Problems View
		marker.setAttribute(IMarker.SEVERITY, CheckstyleMarkersUtils.IMARKER_SEVERITY_OTHERS);
		marker.setAttribute(IMarker.MESSAGE, throwable.toString());
		// marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		marker.setAttribute(IMarker.LOCATION, eclipseFile.getFullPath().toString());
	}
}
