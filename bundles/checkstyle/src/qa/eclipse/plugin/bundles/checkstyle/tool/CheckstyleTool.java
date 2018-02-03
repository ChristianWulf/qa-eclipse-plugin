package qa.eclipse.plugin.bundles.checkstyle.tool;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class CheckstyleTool implements AuditListener {

	private Checker checker;

	public CheckstyleTool() {
		checker = new Checker();
	}

	public void startAsyncAnalysis(List<IFile> eclipseFiles) {
		// checker.setBasedir(basedir);
		// checker.setCacheFile(fileName);
		// checker.setCharset(arg0);
		// checker.setClassLoader(classLoader);
		checker.addListener(this);

		for (IFile eclipseFile : eclipseFiles) {
			// if (monitor.isCanceled()) {
			// // only stop the loop, not the whole method to finish reporting
			// break;
			// }

			if (!eclipseFile.isAccessible()) {
				continue;
			}

			final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();

			List<File> files = Arrays.asList(sourceCodeFile);
			int numViolations;

			try {
				numViolations = checker.process(files);
			} catch (CheckstyleException e) {
				throw new IllegalStateException(e); // log to error view somewhere
			}

			System.out.println("numViolations: " + numViolations);
		}
	}

	@Override
	public void addError(AuditEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addException(AuditEvent arg0, Throwable arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void auditFinished(AuditEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void auditStarted(AuditEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileFinished(AuditEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileStarted(AuditEvent arg0) {
		// TODO Auto-generated method stub

	}

}
