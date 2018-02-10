package qa.eclipse.plugin.bundles.checkstyle.marker;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import qa.eclipse.plugin.bundles.checkstyle.Activator;

public final class ImageRegistryKey {

	private static final int MIN_PRIORITY = 0;
	private static final int MAX_PRIORITY = 3;
	private static final Class<?> ACTIVATOR_CLASS = Activator.class;

	private ImageRegistryKey() {
		// utility class
	}

	/**
	 * @return decorator-"priority"
	 */
	public static String getFileDecoratorKeyByPriority(int priority) {
		return "decorator-" + String.valueOf(priority);
	}

	/**
	 * @return annotation-"priority"
	 */
	public static String getAnnotationKeyByPriority(int priority) {
		return "annotation-" + String.valueOf(priority);
	}

	/**
	 * @return annotation-"priority" (yes, same as for annotations)
	 */
	public static String getPriorityColumnKeyByPriority(int priority) {
		return "annotation-" + String.valueOf(priority);
	}

	public static void initialize(ImageRegistry reg) {
		for (int priority = MIN_PRIORITY; priority <= MAX_PRIORITY; priority++) {
			String imageRegistryKey = getFileDecoratorKeyByPriority(priority);
			String imageFilePath = "/icons/" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ACTIVATOR_CLASS, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}

		for (int priority = MIN_PRIORITY; priority <= MAX_PRIORITY; priority++) {
			String imageRegistryKey = getAnnotationKeyByPriority(priority);
			String imageFilePath = "/icons/" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ACTIVATOR_CLASS, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}
	}
}
