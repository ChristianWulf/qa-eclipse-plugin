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
package qa.eclipse.plugin.pmd.pmd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;
import qa.eclipse.plugin.bundles.common.ClassLoaderUtil;
import qa.eclipse.plugin.bundles.common.FileUtil;
import qa.eclipse.plugin.bundles.common.Logger;
import qa.eclipse.plugin.bundles.common.PreferencesUtil;
import qa.eclipse.plugin.bundles.common.ProjectUtil;
import qa.eclipse.plugin.pmd.icons.FileIconDecorator;
import qa.eclipse.plugin.pmd.markers.PmdMarkers;
import qa.eclipse.plugin.pmd.preference.PmdPreferences;

public final class PmdJob extends WorkspaceJob {

	private static class ConstantRuleSetFactory extends RuleSetFactory {
		private final RuleSets ruleSets;

		// ConstantRuleSetFactory(RuleSet ruleSet) {
		// this.ruleSets = new RuleSets(ruleSet);
		// }

		ConstantRuleSetFactory(final RuleSets ruleSets) {
			this.ruleSets = ruleSets;
		}

		@Override
		public synchronized RuleSets createRuleSets(final String referenceString) throws RuleSetNotFoundException {
			return this.ruleSets;
		}
	}

	private static final int IMARKER_SEVERITY_OTHERS = 3;

	// @Inject
	// private final UISynchronize sync;
	private final List<IFile> eclipseFiles;

	private PmdJob(final String name, final List<IFile> eclipseFiles) {
		super(name);
		this.eclipseFiles = eclipseFiles;
	}

	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
		final IResource someEclipseFile = this.eclipseFiles.get(0);
		final IProject eclipseProject = someEclipseFile.getProject();
		if (!eclipseProject.isAccessible()) { // if project has been closed
			return Status.OK_STATUS;
		}

		final IEclipsePreferences preferences = PmdPreferences.INSTANCE.getProjectScopedPreferences(eclipseProject);
		final boolean pmdEnabled = preferences.getBoolean(PmdPreferences.PROP_KEY_ENABLED, false);
		if (!pmdEnabled) { // if PMD is disabled for this project
			return Status.OK_STATUS;
		}

		// collect data sources
		final Map<String, IFile> eclipseFilesMap = new HashMap<>();
		for (final IFile eclipseFile : this.eclipseFiles) {
			try {
				// also remove previous PMD markers on that file
				PmdMarkers.deleteMarkers(eclipseFile);
			} catch (final CoreException e) {
				// ignore if resource does not exist anymore or has been closed
			}
		}

		// update explorer view so that the violation flag are not displayed anymore
		FileIconDecorator.refresh();

		final String taskName = String.format("Analyzing %d file(s)...", this.eclipseFiles.size());
		final SubMonitor subMonitor = SubMonitor.convert(monitor, taskName, this.eclipseFiles.size());

		final String compilerCompliance = ProjectUtil.getCompilerCompliance(eclipseProject);
		final PMDConfiguration configuration = new CustomPMDConfiguration(compilerCompliance);

		final URL[] urls = this.getCustomJarUrls(eclipseProject);
		// ClassLoader parentClassLoader =
		// Thread.currentThread().getContextClassLoader();
		final ClassLoader parentClassLoader = this.getClass().getClassLoader(); // equinox class loader with jars from the
		// lib
		// folder

		try (URLClassLoader osgiClassLoaderWithCustomRules = ClassLoaderUtil.newClassLoader(urls, parentClassLoader)) {
			ClassLoaderUtil.executeWithContextClassLoader(osgiClassLoaderWithCustomRules, () -> {
				final File eclipseProjectPath = ProjectUtil.getProjectPath(eclipseProject);
				// don't cache rule sets. Reload on each execution.
				final RuleSets ruleSets = PmdPreferences.INSTANCE.loadUpdatedRuleSet(preferences, eclipseProject,
						eclipseProjectPath, osgiClassLoaderWithCustomRules);
				final RuleSetFactory ruleSetFactory = new ConstantRuleSetFactory(ruleSets);

				final Renderer progressRenderer = new PmdProgressRenderer(subMonitor);
				final PmdProblemRenderer problemRenderer = new PmdProblemRenderer();
				final List<Renderer> collectingRenderers = Arrays.asList(progressRenderer, problemRenderer);

				final CancelablePmdProcessor pmdProcessor = new CancelablePmdProcessor(configuration, ruleSetFactory,
						collectingRenderers);

				this.processFiles(monitor, eclipseFilesMap, pmdProcessor);

				this.displayViolationMarkers(eclipseFilesMap, problemRenderer);

				return null;
			});
		} catch (final IOException e) {
			Logger.logThrowable("Exception while analyzing with PMD.", e);
		}

		return Status.OK_STATUS;
	}

	private void processFiles(final IProgressMonitor monitor, final Map<String, IFile> eclipseFilesMap,
			final CancelablePmdProcessor pmdProcessor) {
		final RuleContext context = new RuleContext();

		pmdProcessor.onStarted();
		for (final IFile eclipseFile : this.eclipseFiles) {
			if (monitor.isCanceled()) {
				// only stop the loop, not the whole method to finish reporting
				break;
			}

			if (!eclipseFile.isAccessible()) {
				continue;
			}

			final File sourceCodeFile = eclipseFile.getLocation().toFile().getAbsoluteFile();
			final DataSource dataSource = new FileDataSource(sourceCodeFile);

			// map file name to eclipse file: necessary for adding markers at the end
			final String niceFileName = dataSource.getNiceFileName(false, "");
			eclipseFilesMap.put(niceFileName, eclipseFile);

			pmdProcessor.processFile(dataSource, context);
		}
		pmdProcessor.onFinished();
	}

	private URL[] getCustomJarUrls(final IProject project) {
		final IEclipsePreferences preferences = PmdPreferences.INSTANCE.getProjectScopedPreferences(project);
		final String[] customRulesJarPaths = PreferencesUtil.loadCustomJarPaths(preferences,
				PmdPreferences.PROP_KEY_CUSTOM_RULES_JARS);

		final File eclipseProjectPath = ProjectUtil.getProjectPath(project);
		FileUtil.checkFilesExist("Jar file with custom rules", eclipseProjectPath, customRulesJarPaths);
		final URL[] urls = FileUtil.filePathsToUrls(eclipseProjectPath, customRulesJarPaths);
		return urls;
	}

	private void displayViolationMarkers(final Map<String, IFile> eclipseFilesMap,
			final PmdProblemRenderer problemRenderer) {
		final Report report = problemRenderer.getProblemReport();
		if (report.size() > 0) {
			for (final RuleViolation violation : report.getViolationTree()) {
				final String violationFilename = violation.getFilename();
				final IFile eclipseFile = eclipseFilesMap.get(violationFilename);
				try {
					PmdMarkers.appendViolationMarker(eclipseFile, violation);
				} catch (final CoreException e) {
					// ignore if marker could not be created
				}
			}

			// violations suppressed by NOCS etc.
			// for (SuppressedViolation violation : report.getSuppressedRuleViolations()) {
			// System.out.println("user: " + violation.getUserMessage());
			// }

			// update explorer view so that the new violation flags are displayed
			FileIconDecorator.refresh();
		}

		report.errors().forEachRemaining(error -> {
			final String errorFilename = error.getFile();
			final IFile eclipseFile = eclipseFilesMap.get(errorFilename);
			try {
				this.appendProcessingErrorMarker(eclipseFile, error);
			} catch (final CoreException e) {
				// ignore if marker could not be created
			}
			// PmdUIPlugin.getDefault().logWarning(error.getMsg());
		});
	}

	private void appendProcessingErrorMarker(final IFile eclipseFile, final ProcessingError error)
			throws CoreException {
		final IMarker marker = eclipseFile.createMarker(PmdMarkers.PMD_ERROR_MARKER);
		// whether it is displayed as error, warning, info or other in the Problems View
		marker.setAttribute(IMarker.SEVERITY, PmdJob.IMARKER_SEVERITY_OTHERS);
		marker.setAttribute(IMarker.MESSAGE, error.getMsg());
		// marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		marker.setAttribute(IMarker.LOCATION, error.getFile());
	}

	/**
	 * All passed files must belong to the same project.
	 *
	 * @param eclipseFiles
	 */
	public static void startAsyncAnalysis(final List<IFile> eclipseFiles) {
		if (eclipseFiles.isEmpty()) {
			return;
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (final IFile eclipseFile : eclipseFiles) {
			final ISchedulingRule fileRule = ruleFactory.markerRule(eclipseFile);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		final Job job = new PmdJob("Analysis by PMD", eclipseFiles);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}

}