package pmd.eclipse.plugin.icons;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import pmd.eclipse.plugin.PmdUIPlugin;

public final class ImageRegistryKey {

	private ImageRegistryKey() {
		// utility class
	}

	/**
	 * @return "pmdPriority"-decorator
	 */
	public static String getFileDecoratorKeyByPriority(int pmdPriority) {
		return String.valueOf(pmdPriority) + "-decorator";
	}

	/**
	 * @return "pmdPriority"-annotation
	 */
	public static String getAnnotationKeyByPriority(int pmdPriority) {
		return String.valueOf(pmdPriority) + "-annotation";
	}

	/**
	 * @return "pmdPriority"-annotation (yes, same as for annotations)
	 */
	public static String getPriorityColumnKeyByPriority(int pmdPriority) {
		return String.valueOf(pmdPriority) + "-annotation";
	}

	public static void initialize(ImageRegistry reg) {
		for (int priority = 1; priority <= 5; priority++) {
			String imageRegistryKey = getFileDecoratorKeyByPriority(priority);
			String imageFilePath = "/icons/priority" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(PmdUIPlugin.class, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}

		for (int priority = 1; priority <= 5; priority++) {
			String imageRegistryKey = getAnnotationKeyByPriority(priority);
			String imageFilePath = "/icons/pmd" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(PmdUIPlugin.class, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}
	}
}
