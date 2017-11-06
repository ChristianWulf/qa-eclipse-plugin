package pmdeclipseplugin.pmd;

//architectural hint: may use eclipse packages
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
import org.osgi.framework.Bundle;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.processor.MonoThreadProcessor;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.ReaderDataSource;
import pmdeclipseplugin.PmdUIPlugin;
import pmdeclipseplugin.eclipse.FileUtil;
import pmdeclipseplugin.eclipse.ProjectUtil;
import pmdeclipseplugin.settings.ClassLoaderCache;
import pmdeclipseplugin.settings.RuleSetCache;
import pmdeclipseplugin.settings.SettingsFileCache;

public class PmdTool {

	private static final String PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION = "pmd.eclipse.plugin.markers.violation";
	private static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	private static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";

	// @Inject
	// private final UISynchronize sync;
	private final Map<File, Object> fileCache = new HashMap<>();

	private final SettingsFileCache settingsFileCache;
	private final ClassLoaderCache classLoaderCache;
	private final RuleSetCache ruleSetCache;

	public PmdTool() {
		Bundle bundle = PmdUIPlugin.getDefault().getBundle();
		settingsFileCache = new SettingsFileCache(bundle);
		classLoaderCache = new ClassLoaderCache();
		ruleSetCache = new RuleSetCache();
	}

	public void startAsyncAnalysis(IFile eclipseFile, ExecutionEvent event) {
		final File sourceCodeFile = eclipseFile.getRawLocation().makeAbsolute().toFile();

		final IProject eclipseProject = eclipseFile.getProject();

		// load custom rules from settings file
		// but only if the settings file has changed since last loading
		if (!settingsFileCache.isUpToDate(eclipseProject)) {
			loadUpdatedRuleSet(eclipseProject);
		}

		final RuleSet ruleSet = ruleSetCache.getCachedRuleSet(eclipseProject);
		if (!ruleSet.applies(sourceCodeFile)) {
			return;
		}

		Job job = Job.create("Analysis by PMD", new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 1);

				// remove previous PMD markers
				eclipseFile.deleteMarkers(PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION, false, IResource.DEPTH_ZERO);

				String compilerCompliance = ProjectUtil.getCompilerCompliance(eclipseProject);
				PMDConfiguration configuration = new CustomPMDConfiguration(compilerCompliance);

				RuleSetFactory ruleSetFactory = new RuleSetFactory() {
					@Override
					public synchronized RuleSets createRuleSets(String referenceString)
							throws RuleSetNotFoundException {
						return new RuleSets(ruleSet);
					}
				};

				RuleContext context = PMD.newRuleContext(sourceCodeFile.getName(), sourceCodeFile);
				// context.setLanguageVersion(configuration.getla);

				PmdProblemRenderer problemRenderer = new PmdProblemRenderer();
				List<Renderer> collectingRenderers = Arrays.asList(problemRenderer);

				try (InputStreamReader input = new InputStreamReader(eclipseFile.getContents(),
						eclipseFile.getCharset())) {
					DataSource dataSource = new ReaderDataSource(input, eclipseFile.getName());
					List<DataSource> dataSources = Arrays.asList(dataSource);

					MonoThreadProcessor pmdProcessor = new MonoThreadProcessor(configuration);

					subMonitor.split(1);
					pmdProcessor.processFiles(ruleSetFactory, dataSources, context, collectingRenderers);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}

				Report report = problemRenderer.getProblemReport();
				if (report.size() > 0) {
					for (RuleViolation violation : report.getViolationTree()) {
						// violation.getFilename();
						System.out.println("violation: " + violation);
						appendViolationMarker(eclipseFile, violation);
					}
				}

				// from:
				// http://www.vogella.com/tutorials/EclipseJobs/article.html#using-syncexec-and-asyncexec
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						// addAnnotations(eclipseFile, event);
					}
				});

				// fileCache.put(file, result);
			}
		});
		job.schedule();
	}

	protected void appendViolationMarker(IFile eclipseFile, RuleViolation violation) {
		IMarker marker;
		try {
			marker = eclipseFile.createMarker(PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION);
			marker.setAttribute(IMarker.MESSAGE, violation.getDescription());
			marker.setAttribute("pmd.priority", violation.getRule().getPriority().getPriority());
			// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
			// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
			// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());
			
			// whether it is displayed as error, warning, info or other in the Problems View
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
	}

	private void addAnnotations(IFile eclipseFile, ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null)
			return;
		IEditorPart editor = activePage.getActiveEditor();
		if (editor == null)
			return;
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			IDocumentProvider documentProvider = textEditor.getDocumentProvider();

			IDocument document = documentProvider.getDocument(editor.getEditorInput());
			IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());

			IMarker[] markers;
			try {
				markers = eclipseFile.findMarkers(PMD_ECLIPSE_PLUGIN_MARKERS_VIOLATION, true, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}

			annotationModel.connect(document);
			try {
				for (IMarker marker : markers) {
					RulePriority pmdPriority;
					try {
						pmdPriority = (RulePriority) marker.getAttribute("pmd.priority");
					} catch (CoreException e) {
						throw new IllegalStateException(e);
					}

					String annotationTypeName;
					switch (pmdPriority) {
					default:
						annotationTypeName = "pmd.eclipse.plugin.ui.specification.priority.high";
						break;
					}

					SimpleMarkerAnnotation markerAnnotation = new SimpleMarkerAnnotation(annotationTypeName, marker);
					annotationModel.addAnnotation(markerAnnotation,
							new Position(marker.getAttribute(IMarker.LINE_NUMBER, 0)));
				}
			} finally {
				annotationModel.disconnect(document);
			}
		}
	}

	private void loadUpdatedRuleSet(final IProject eclipseProject) {
		final Properties properties = settingsFileCache.load(eclipseProject);
		final String customRulesJarsValue = properties.getProperty(PROP_KEY_CUSTOM_RULES_JARS);
		final File eclipseProjectFile = eclipseProject.getRawLocation().makeAbsolute().toFile();

		// load custom rules into a new class loader
		String[] customRulesJars = customRulesJarsValue.split(",");
		URL[] urls = filePathsToUrls(eclipseProjectFile, customRulesJars);
		URLClassLoader osgiClassLoaderWithCustomRules = new URLClassLoader(urls, getClass().getClassLoader());

		// assign the new class loader with the project
		classLoaderCache.putClassLoader(eclipseProject, osgiClassLoaderWithCustomRules);

		String ruleSetFilePathValue = properties.getProperty(PROP_KEY_RULE_SET_FILE_PATH);
		File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectFile);
		String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		try {
			ruleSetCache.load(eclipseProject, ruleSetFilePath, osgiClassLoaderWithCustomRules);
		} catch (RuleSetNotFoundException e) {
			// RuleSetNotFoundException at this place means: fiel not found.
			// Since PMD does not work without any ruleset file, we stop the loop here.
			// TODO log error to problems view
			String message = String.format("Ruleset file not found on file path '%s'", ruleSetFilePathValue);
			throw new IllegalStateException(message, e);
		}
	}

	private URL[] filePathsToUrls(final File parentFile, String[] customRulesJars) {
		URL[] urls = new URL[customRulesJars.length];
		for (int i = 0; i < customRulesJars.length; i++) {
			File customRulesJarFile = FileUtil.makeAbsoluteFile(customRulesJars[i], parentFile);
			try {
				urls[i] = customRulesJarFile.toURI().toURL();
			} catch (MalformedURLException e) {
				// customRulesJars is filled by the user, so continue loop upon exception
				// TODO log unknown jar path
			}
		}
		return urls;
	}

	public void getHighestPriorityForFile(File file) {
		Object object = fileCache.get(file);

		// TODO return PmdPriority instance
	}
}
