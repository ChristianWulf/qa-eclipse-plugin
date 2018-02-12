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
		imageRegistry = Activator.getDefault().getImageRegistry();
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof IResource)) {
			return;
		}

		IResource resource = (IResource) element;
		// do not decorate if the project has been closed
		if (!resource.isAccessible()) {
			return;
		}

		int depth = IResource.DEPTH_INFINITE;
		// if (resource instanceof IFolder) {
		// depth = IResource.DEPTH_INFINITE;
		// } else if (resource instanceof IFile) {
		// depth = IResource.DEPTH_ZERO;
		// }

		IMarker[] markers;
		try {
			markers = resource.findMarkers(CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER, true, depth);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}

		// do not display any file decorator if there are no markers
		if (markers.length == 0) {
			return;
		}

		int highestPriority = SeverityLevel.IGNORE.ordinal();
		for (IMarker marker : markers) {
			CheckstyleViolationMarker violationMarker = new CheckstyleViolationMarker(marker);
			int priority = violationMarker.getSeverityLevelIndex();
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
		int lowestAllowedPriority = 0;
		if (highestPriority >= lowestAllowedPriority) {
			String imageRegistryKey = ImageRegistryKey.getFileDecoratorKeyByPriority(highestPriority);
			imageDescriptor = imageRegistry.getDescriptor(imageRegistryKey);
		}

		decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
	}

	public static void refresh() {
		IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		IBaseLabelProvider decorator = manager.getBaseLabelProvider(FileIconDecorator.ID);
		if (decorator != null) { // decorator is enabled
			ILabelProviderListener listener = (ILabelProviderListener) manager;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.labelProviderChanged(new LabelProviderChangedEvent(decorator));
				}
			});
		}
	}

}
