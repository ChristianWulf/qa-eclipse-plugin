package qa.eclipse.plugin.pmd.icons;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import qa.eclipse.plugin.pmd.PmdUIPlugin;

public final class ImageRegistryKey {

	private final static Class<?> ACTIVATOR_CLASS = PmdUIPlugin.class;

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
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ACTIVATOR_CLASS, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}

		for (int priority = 1; priority <= 5; priority++) {
			String imageRegistryKey = getAnnotationKeyByPriority(priority);
			String imageFilePath = "/icons/pmd" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ACTIVATOR_CLASS, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}
	}
}
