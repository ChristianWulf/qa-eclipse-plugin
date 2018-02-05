package qa.eclipse.plugin.bundles.checkstyle.tool;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.Activator;

class CheckstyleListener implements AuditListener {

	private final Map<String, IFile> eclipseFileByFilePath;
	private final SubMonitor monitor;

	public CheckstyleListener(IProgressMonitor monitor, Map<String, IFile> eclipseFileByFilePath) {
		this.eclipseFileByFilePath = eclipseFileByFilePath;
		int numFiles = eclipseFileByFilePath.size();
		String taskName = "Analyzing " + numFiles + " file(s)...";
		this.monitor = SubMonitor.convert(monitor, taskName, numFiles);
	}

	@Override
	public void auditStarted(AuditEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void fileStarted(AuditEvent event) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

//		String filePath = event.getFileName();
//		IFile eclipseFile = eclipseFileByFilePath.get(filePath);
//		
//		if (!eclipseFile.isAccessible()) {
//			return;
//		}

		monitor.split(1);
	}

	@Override
	public void fileFinished(AuditEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void auditFinished(AuditEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addError(AuditEvent violation) {
		SeverityLevel severityLevel = violation.getSeverityLevel();

		System.out.println("violation: " + violation);

		// TODO Auto-generated method stub

	}

	@Override
	public void addException(AuditEvent event, Throwable throwable) {
		Activator.getDefault().logThrowable(event.getMessage(), throwable);
	}
}
