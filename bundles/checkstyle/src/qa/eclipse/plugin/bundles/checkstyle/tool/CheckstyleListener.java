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
package qa.eclipse.plugin.bundles.checkstyle.tool;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.BeforeExecutionFileFilter;

import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleMarkersUtils;

/**
 *
 * @author Christian Wulf
 *
 */
class CheckstyleListener implements AuditListener, BeforeExecutionFileFilter {

	private final Map<String, IFile> eclipseFileByFilePath;
	private final SubMonitor monitor;

	public CheckstyleListener(final IProgressMonitor monitor, final Map<String, IFile> eclipseFileByFilePath) {
		this.eclipseFileByFilePath = eclipseFileByFilePath;
		final int numFiles = eclipseFileByFilePath.size();
		final String taskName = String.format("Analyzing %d file(s)...", numFiles);
		this.monitor = SubMonitor.convert(monitor, taskName, numFiles);
	}

	@Override
	public void auditStarted(final AuditEvent arg0) {
		// do nothing
	}

	@Override
	public boolean accept(final String absoluteFilePath) {
		this.monitor.split(1);

		final IFile eclipseFile = this.eclipseFileByFilePath.get(absoluteFilePath);
		return eclipseFile.isAccessible();
	}

	@Override
	public void fileStarted(final AuditEvent event) {
		// do nothing
	}

	@Override
	public void fileFinished(final AuditEvent arg0) {
		// do nothing
	}

	@Override
	public void auditFinished(final AuditEvent arg0) {
		// do nothing
	}

	@Override
	public void addError(final AuditEvent violation) {
		final String violationFilename = violation.getFileName();
		final IFile eclipseFile = this.eclipseFileByFilePath.get(violationFilename);
		try {
			CheckstyleMarkersUtils.appendViolationMarker(eclipseFile, violation);
		} catch (final CoreException e) { // NOPMD ignore empty block
			// ignore if marker could not be created
		}
	}

	@Override
	public void addException(final AuditEvent event, final Throwable throwable) {
		// Activator.getDefault().logThrowable(event.getMessage(), throwable);

		final String violationFilename = event.getFileName();
		final IFile eclipseFile = this.eclipseFileByFilePath.get(violationFilename);
		try {
			CheckstyleMarkersUtils.appendProcessingErrorMarker(eclipseFile, throwable);
		} catch (final CoreException e) { // NOPMD ignore empty block
			// ignore if marker could not be created
		}
	}

}
