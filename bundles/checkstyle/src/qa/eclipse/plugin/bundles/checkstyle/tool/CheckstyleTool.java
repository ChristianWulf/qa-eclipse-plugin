package qa.eclipse.plugin.bundles.checkstyle.tool;
// may not contain anything from the Eclipse API

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import qa.eclipse.plugin.bundles.checkstyle.EclipsePlatform;

public class CheckstyleTool {

	private final Checker checker;

	public CheckstyleTool() {
		this.checker = new Checker();
	}

	// FIXME remove Eclipse API
	public void startAsyncAnalysis(List<IFile> eclipseFiles, CheckstyleListener checkstyleListener) {
		IFile file = eclipseFiles.get(0);
		IProject project = file.getProject();

		checker.setBasedir(null);
		// checker.setCacheFile(fileName);

		try {
			checker.setCharset(project.getDefaultCharset());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
		
		//		 checker.setClassLoader(classLoader);
		
		URL[] urls = null;
		ClassLoader moduleClassLoader=new URLClassLoader(urls, getClass().getClassLoader());
		checker.setModuleClassLoader(moduleClassLoader);

//		ClassLoader checkstyleClassLoader = getClass().getClassLoader();
//		Set<String> packageNames;
//		try {
//			packageNames = PackageNamesLoader.getPackageNames(checkstyleClassLoader);
//		} catch (CheckstyleException e) {
//			throw new IllegalStateException(e);
//		}
//
//		ClassLoader moduleClassLoader = null;
//		PackageObjectFactory moduleFactory = new PackageObjectFactory(packageNames, moduleClassLoader,
//				ModuleLoadOption.TRY_IN_ALL_REGISTERED_PACKAGES);
//		checker.setModuleFactory(moduleFactory);

		Locale platformLocale = EclipsePlatform.getLocale();
		checker.setLocaleLanguage(platformLocale.getLanguage());
		checker.setLocaleCountry(platformLocale.getCountry());

		Configuration configuration = new DefaultConfiguration("Eclipse Checkstyle Config", ThreadModeSettings.SINGLE_THREAD_MODE_INSTANCE);
		try {
			checker.configure(configuration);
		} catch (CheckstyleException e) {
			throw new IllegalStateException(e);
		}

		checker.addListener(checkstyleListener);

		// https://github.com/checkstyle/eclipse-cs/blob/master/net.sf.eclipsecs.core/src/net/sf/eclipsecs/core/builder/CheckerFactory.java

		try {

			for (IFile eclipseFile : eclipseFiles) {
				if (!eclipseFile.isAccessible()) {
					continue;
				}

				final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();

				List<File> files = Arrays.asList(sourceCodeFile);
				int numViolations;

				try {
					numViolations = checker.process(files);
					System.out.println("numViolations: " + numViolations);
				} catch (CheckstyleException e) {
					if (e.getCause() instanceof OperationCanceledException) {
						// user requested cancellation, keep silent
					} else {
						throw new IllegalStateException(e); // log to error view somewhere
					}
				}

			}

		} finally {
			checker.removeListener(checkstyleListener);
		}
	}

	// private void displayViolationMarkers(final Map<String, IFile>
	// eclipseFilesMap, PmdProblemRenderer problemRenderer) {
	// Report report = problemRenderer.getProblemReport();
	// if (report.size() > 0) {
	// for (RuleViolation violation : report.getViolationTree()) {
	// String violationFilename = violation.getFilename();
	// IFile eclipseFile = eclipseFilesMap.get(violationFilename);
	// try {
	// PmdMarkers.appendViolationMarker(eclipseFile, violation);
	// } catch (CoreException e) {
	// // ignore if marker could not be created
	// }
	// }
	//
	// // update explorer view so that the new violation flags are displayed
	// FileIconDecorator.refresh();
	// }
	// }

}
