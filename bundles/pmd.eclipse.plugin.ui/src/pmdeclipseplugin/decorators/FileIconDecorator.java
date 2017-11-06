package pmdeclipseplugin.decorators;

//architectural hint: may use eclipse packages
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import pmdeclipseplugin.PmdUIPlugin;

public class FileIconDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final ImageDescriptor SAMPLE_DECORATOR;

	static {
		SAMPLE_DECORATOR = AbstractUIPlugin.imageDescriptorFromPlugin(PmdUIPlugin.PLUGIN_ID,
				"icons/sample_decorator.gif");
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// update if property "xx" of element has been updated
		// update if pmd has run for the file represented by the element
		return true;
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		// if (element instanceof IProject) {
		//
		// }
		// if (element instanceof IFolder) {
		//
		// }
		if (element instanceof IFile) { // decorator for file could/should(?) be defined in the xml config file
			IPath location = ((IFile) element).getLocation();
			if (location == null) return;

			File file = location.makeAbsolute().toFile();
//			PmdPriority highestPriority = pmdTool.getHighestPriorityForFile(file);
			
			ImageDescriptor imageDescriptor;
//			imageDescriptor = PmdImageDescriptors.getForPriority(highestPriority);
			imageDescriptor = SAMPLE_DECORATOR;
			decoration.addOverlay(imageDescriptor, IDecoration.TOP_LEFT);
		}
	}

}
