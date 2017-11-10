package pmdeclipseplugin.decorators;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;

import pmdeclipseplugin.PmdUIPlugin;

public class PmdImageDescriptors {

	private final Map<Integer, ImageDescriptor> imageDescriptors = new HashMap<>();

	public PmdImageDescriptors() {
		for (int i = 1; i <= 5; i++) {
			String imageFilePath = "/icons/priority" + i + ".png";
			// AbstractUIPlugin.imageDescriptorFromPluginalways returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(PmdUIPlugin.class, imageFilePath);
			imageDescriptors.put(i, imageDescriptor);
		}
	}

	public ImageDescriptor getForPriority(int priority) {
		// FIXME only for testing purposes
		if (priority != 1) {
			priority = 3;
		}
		return imageDescriptors.get(priority);
	}

}
