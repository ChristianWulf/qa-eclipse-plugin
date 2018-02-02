package pmd.eclipse.plugin.pmd;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import pmd.eclipse.plugin.eclipse.ProjectUtil;
import pmd.eclipse.plugin.icons.FileIconDecorator;
import pmd.eclipse.plugin.markers.PmdMarkers;
import pmd.eclipse.plugin.settings.PmdPreferences;

class PmdWorkspaceJob extends WorkspaceJob {

	private static class ConstantRuleSetFactory extends RuleSetFactory {
		private final RuleSets ruleSets;

		// ConstantRuleSetFactory(RuleSet ruleSet) {
		// this.ruleSets = new RuleSets(ruleSet);
		// }

		ConstantRuleSetFactory(RuleSets ruleSets) {
			this.ruleSets = ruleSets;
		}

		@Override
		public synchronized RuleSets createRuleSets(String referenceString) throws RuleSetNotFoundException {
			return ruleSets;
		}
	}

	// @Inject
	// private final UISynchronize sync;
	private final List<IFile> eclipseFiles;

	public PmdWorkspaceJob(String name, List<IFile> eclipseFiles) {
		super(name);
		this.eclipseFiles = eclipseFiles;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		final IResource someEclipseFile = eclipseFiles.get(0);
		final IProject eclipseProject = someEclipseFile.getProject();
		if (!eclipseProject.isAccessible()) { // if project has been closed
			return Status.OK_STATUS;
		}

		IEclipsePreferences preferences = PmdPreferences.INSTANCE.getProjectScopedPreferences(eclipseProject);
		boolean pmdEnabled = preferences.getBoolean(PmdPreferences.PROP_KEY_ENABLED, false);
		if (!pmdEnabled) { // if PMD is disabled for this project
			return Status.OK_STATUS;
		}

		// collect data sources
		final Map<String, IFile> eclipseFilesMap = new HashMap<>();
		for (IFile eclipseFile : eclipseFiles) {
			try {
				// also remove previous PMD markers on that file
				eclipseFile.deleteMarkers(PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER, true, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				// ignore if resource does not exist anymore or has been closed
			}
		}

		// update explorer view so that the violation flag are not displayed anymore
		FileIconDecorator.refresh();

		String taskName = "Analyzing " + eclipseFiles.size() + " file(s)...";
		final SubMonitor subMonitor = SubMonitor.convert(monitor, taskName, eclipseFiles.size());

		String compilerCompliance = ProjectUtil.getCompilerCompliance(eclipseProject);
		final PMDConfiguration configuration = new CustomPMDConfiguration(compilerCompliance);

		RuleSets ruleSets = PmdPreferences.INSTANCE.getRuleSets(eclipseProject);
		final RuleSetFactory ruleSetFactory = new ConstantRuleSetFactory(ruleSets);

		Renderer progressRenderer = new PmdProgressRenderer(subMonitor);
		PmdProblemRenderer problemRenderer = new PmdProblemRenderer();
		final List<Renderer> collectingRenderers = Arrays.asList(progressRenderer, problemRenderer);

		CancelablePmdProcessor pmdProcessor = new CancelablePmdProcessor(configuration, ruleSetFactory,
				collectingRenderers);

		final RuleContext context = new RuleContext();

		pmdProcessor.onStarted();
		for (IFile eclipseFile : eclipseFiles) {
			if (monitor.isCanceled()) {
				// only stop the loop, not the whole method to finish reporting
				break;
			}

			if (!eclipseFile.isAccessible()) {
				continue;
			}

			final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();
			final DataSource dataSource = new FileDataSource(sourceCodeFile);

			// map file name to eclipse file: necessary for adding markers at the end
			String niceFileName = dataSource.getNiceFileName(false, "");
			eclipseFilesMap.put(niceFileName, eclipseFile);

			pmdProcessor.processFile(dataSource, context);
		}
		pmdProcessor.onFinished();

		displayViolationMarkers(eclipseFilesMap, problemRenderer);

		return Status.OK_STATUS;
	}

	private void displayViolationMarkers(final Map<String, IFile> eclipseFilesMap, PmdProblemRenderer problemRenderer) {
		Report report = problemRenderer.getProblemReport();
		if (report.size() > 0) {
			for (RuleViolation violation : report.getViolationTree()) {
				String violationFilename = violation.getFilename();
				IFile eclipseFile = eclipseFilesMap.get(violationFilename);
				try {
					PmdMarkers.appendViolationMarker(eclipseFile, violation);
				} catch (CoreException e) {
					// ignore if marker could not be created
				}
			}

			// update explorer view so that the new violation flags are displayed
			FileIconDecorator.refresh();
		}
	}

}