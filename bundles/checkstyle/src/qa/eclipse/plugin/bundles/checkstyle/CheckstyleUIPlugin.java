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
package qa.eclipse.plugin.bundles.checkstyle;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import qa.eclipse.plugin.bundles.checkstyle.tool.CheckstyleJob;
import qa.eclipse.plugin.bundles.common.ImageRegistryKeyUtils;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Christian Wulf
 */
public class CheckstyleUIPlugin extends AbstractUIPlugin implements IResourceChangeListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "qa.eclipse.plugin.bundles.checkstyle"; //$NON-NLS-1$

	public static final int CHECKSTYLE_MIN_PRIORITY = 0;

	public static final int CHECKSTYLE_MAX_PRIORITY = 3;

	public static final String CHECKSTYLE_PREFIX = "checkstyle";

	// The shared instance
	private static CheckstyleUIPlugin plugin;

	/**
	 * The constructor.
	 */
	public CheckstyleUIPlugin() {
		// nothing to do here
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		CheckstyleUIPlugin.plugin = this;

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		CheckstyleUIPlugin.plugin = null; // NOPMD necessary in context of plugins
		super.stop(context);
	}

	/**
	 * @return the shared instance of the plugin
	 */
	public static CheckstyleUIPlugin getDefault() {
		return CheckstyleUIPlugin.plugin;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry registry) {
		super.initializeImageRegistry(registry);

		ImageRegistryKeyUtils.initialize(CheckstyleUIPlugin.class, CheckstyleUIPlugin.CHECKSTYLE_PREFIX, registry,
				CheckstyleUIPlugin.CHECKSTYLE_MIN_PRIORITY, CheckstyleUIPlugin.CHECKSTYLE_MAX_PRIORITY);
	}

	/**
	 * Log message to view including the associated exception.
	 *
	 * @param message
	 *            message to display
	 * @param throwable
	 *            exception
	 */
	public void logThrowable(final String message, final Throwable throwable) {
		final IStatus status = new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID, message, throwable);
		this.getLog().log(status);
	}

	/**
	 * Log a warning message to the checkstyle summary view.
	 *
	 * @param message
	 *            the message to display
	 */
	public void logWarning(final String message) {
		final IStatus status = new Status(IStatus.WARNING, CheckstyleUIPlugin.PLUGIN_ID, message);
		this.getLog().log(status);
	}

	@Override
	// represents: analyze on save
	public void resourceChanged(final IResourceChangeEvent event) {
		final ResourceDeltaFileCollector resourceDeltaFileCollector = new ResourceDeltaFileCollector();

		try {
			event.getDelta().accept(resourceDeltaFileCollector);
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}

		for (final Entry<IProject, List<IFile>> addedFiles : resourceDeltaFileCollector.getAddedFiles().entrySet()) {
			CheckstyleJob.startAsyncAnalysis(addedFiles.getValue());
		}

		for (final Entry<IProject, List<IFile>> changedFiles : resourceDeltaFileCollector.getChangedFiles().entrySet()) {
			CheckstyleJob.startAsyncAnalysis(changedFiles.getValue());
		}

		// our view listens to marker changes and thus is indirectly notified about
		// removed resource
	}
}
