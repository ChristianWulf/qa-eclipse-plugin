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
package qa.eclipse.plugin.pmd;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import qa.eclipse.plugin.bundles.common.ILoggingFacility;
import qa.eclipse.plugin.bundles.common.ImageRegistryKeyUtils;
import qa.eclipse.plugin.pmd.builder.IncrementalViolationMarkerBuilder;
import qa.eclipse.plugin.pmd.tool.PmdTool;
import qa.eclipse.plugin.pmd.ui.visitors.ResourceDeltaFileCollector;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Christian wulf
 */
public class PmdUIPlugin extends AbstractUIPlugin implements ILoggingFacility, IResourceChangeListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "qa.eclipse.plugin.bundles.pmd"; //$NON-NLS-1$

	public static final int PMD_MIN_PRIORITY = 1;

	public static final int PMD_MAX_PRIORITY = 5;

	public static final String PMD_PREFIX = "pmd";

	// The shared instance
	private static PmdUIPlugin plugin;

	private BundleContext context;

	private PmdTool pmdTool;

	/**
	 * The constructor.
	 */
	public PmdUIPlugin() {
		// default constructor is required by Eclipse
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception { // NOCS
		super.start(context);
		PmdUIPlugin.plugin = this;
		this.context = context;
		this.pmdTool = new PmdTool();

		// IDocumentProvider provider = ((ITextEditor)
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getDocumentProvider();
		// AnnotationModel am =
		// (AnnotationModel)provider.getAnnotationModel((PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput()));
		// DocumentProviderRegistry.getDefault().getDocumentProvider(null);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	// represents: analyze on save
	public void resourceChanged(final IResourceChangeEvent event) {
		final ResourceDeltaFileCollector resourceDeltaFileCollector = new ResourceDeltaFileCollector();

		try {
			event.getDelta().accept(resourceDeltaFileCollector);
		} catch (final CoreException e) {
			PmdUIPlugin.getDefault().logThrowable("Error on resource changed.", e);
		}

		for (final Entry<IProject, List<IFile>> addedFiles : resourceDeltaFileCollector.getAddedFiles().entrySet()) {
			this.pmdTool.startAsyncAnalysis(addedFiles.getValue());
		}

		for (final Entry<IProject, List<IFile>> changedFiles : resourceDeltaFileCollector.getChangedFiles()
				.entrySet()) {
			this.pmdTool.startAsyncAnalysis(changedFiles.getValue());
		}

		// our view listens to marker changes and thus is indirectly notified about
		// removed resource
	}

	/**
	 * Register builder.
	 *
	 * @param project
	 *            current project
	 * @throws CoreException
	 *             when setting the project description
	 */
	public void registerBuilder(final IProject project) throws CoreException {
		final IProjectDescription desc;
		try {
			desc = project.getDescription();
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}
		final ICommand[] commands = desc.getBuildSpec();

		for (final ICommand command : commands) {
			if (command.getBuilderName().equals(IncrementalViolationMarkerBuilder.BUILDER_ID)) {
				this.addBuilderToProject(desc, commands, project);
				break;
			}
		}
	}

	private void addBuilderToProject(final IProjectDescription description,
			final ICommand[] commands, final IProject project) throws CoreException {
		// add builder to project
		final ICommand command = description.newCommand();
		command.setBuilderName(IncrementalViolationMarkerBuilder.BUILDER_ID);
		final ICommand[] newCommands = new ICommand[commands.length + 1];

		// Add it before other builders.
		System.arraycopy(commands, 0, newCommands, 1, commands.length);
		newCommands[0] = command;
		description.setBuildSpec(newCommands);

		project.setDescription(description, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception { // NOCS context is API
		PmdUIPlugin.plugin = null; // NOPMD
		super.stop(context);
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry registry) {
		super.initializeImageRegistry(registry);

		ImageRegistryKeyUtils.initialize(PmdUIPlugin.class, PmdUIPlugin.PMD_PREFIX, registry,
				PmdUIPlugin.PMD_MIN_PRIORITY, PmdUIPlugin.PMD_MAX_PRIORITY);
	}

	/**
	 * @return the shared instance
	 */
	public static PmdUIPlugin getDefault() {
		return PmdUIPlugin.plugin;
	}

	public BundleContext getContext() {
		return this.context;
	}

	public PmdTool getPmdTool() {
		return this.pmdTool;
	}

	/**
	 * Log error message.
	 *
	 * @param message
	 *            message
	 * @param throwable
	 *            associated throwable
	 */
	@Override
	public void logThrowable(final String message, final Throwable throwable) {
		final IStatus status = new Status(IStatus.ERROR, PmdUIPlugin.PLUGIN_ID, message, throwable);
		this.getLog().log(status);
	}

	/**
	 * Log warning message.
	 *
	 * @param message
	 *            message
	 */
	@Override
	public void logWarning(final String message) {
		final IStatus status = new Status(IStatus.WARNING, PmdUIPlugin.PLUGIN_ID, message);
		this.getLog().log(status);
	}

}
