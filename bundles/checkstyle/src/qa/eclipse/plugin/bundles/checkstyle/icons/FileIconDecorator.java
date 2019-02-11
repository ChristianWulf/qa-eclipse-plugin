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
package qa.eclipse.plugin.bundles.checkstyle.icons;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.Activator;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleMarkers;
import qa.eclipse.plugin.bundles.checkstyle.marker.CheckstyleViolationMarker;
import qa.eclipse.plugin.bundles.checkstyle.marker.ImageRegistryKey;

public class FileIconDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ID = "qa.eclipse.plugin.bundles.checkstyle.decorator";

	private final ImageRegistry imageRegistry;

	public FileIconDecorator() {
		this.imageRegistry = Activator.getDefault().getImageRegistry();
	}

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (!(element instanceof IResource)) {
			return;
		}

		final IResource resource = (IResource) element;
		// do not decorate if the project has been closed
		if (!resource.isAccessible()) {
			return;
		}

		ImageDescriptor imageDescriptor = null;
		try {
			imageDescriptor = this.getImageDescriptor(resource);
		} catch (final CoreException e) {
			Activator.getDefault().logThrowable("Error on decorating element.", e);
		}

		decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
	}

	private ImageDescriptor getImageDescriptor(final IResource resource) throws CoreException {
		final int depth = IResource.DEPTH_INFINITE;
		// if (resource instanceof IFolder) {
		// depth = IResource.DEPTH_INFINITE;
		// } else if (resource instanceof IFile) {
		// depth = IResource.DEPTH_ZERO;
		// }

		final IMarker[] markers = resource.findMarkers(CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER, true, depth);

		// do not display any file decorator if there are no markers
		if (markers.length == 0) {
			return null;
		}

		int highestPriority = SeverityLevel.IGNORE.ordinal();
		for (final IMarker marker : markers) {
			final CheckstyleViolationMarker violationMarker = new CheckstyleViolationMarker(marker);
			final int priority = violationMarker.getSeverityLevelIndex();
			// 3 is the highest priority, so compare with '<'
			if (priority > highestPriority) {
				highestPriority = priority;
				// fast exit: if highest priority is max priority, then quit at once
				if (highestPriority == SeverityLevel.IGNORE.ordinal()) {
					break;
				}
			}
		}

		// apply filter
		ImageDescriptor imageDescriptor = null;
		final int lowestAllowedPriority = 0;
		if (highestPriority >= lowestAllowedPriority) {
			final String imageRegistryKey = ImageRegistryKey.getFileDecoratorKeyByPriority(highestPriority);
			imageDescriptor = this.imageRegistry.getDescriptor(imageRegistryKey);
		}

		return imageDescriptor;
	}

	public static void refresh() {
		final IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		final IBaseLabelProvider decorator = manager.getBaseLabelProvider(FileIconDecorator.ID);
		if (decorator != null) { // decorator is enabled
			final ILabelProviderListener listener = (ILabelProviderListener) manager;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.labelProviderChanged(new LabelProviderChangedEvent(decorator));
				}
			});
		}
	}

}
