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
package qa.eclipse.plugin.bundles.checkstyle.marker;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import qa.eclipse.plugin.bundles.checkstyle.Activator;

/**
 *
 * @author Christian Wulf
 *
 */
public final class ImageRegistryKey {

	private static final int MIN_PRIORITY = 0;
	private static final int MAX_PRIORITY = 3;
	private static final Class<?> ACTIVATOR_CLASS = Activator.class;

	private ImageRegistryKey() {
		// utility class
	}

	/**
	 * 8x8 image
	 *
	 * @return decorator-"priority"
	 */
	public static String getFileDecoratorKeyByPriority(final int priority) {
		return "decorator-" + String.valueOf(priority);
	}

	/**
	 * 16x16 image
	 *
	 * @return annotation-"priority"
	 */
	public static String getAnnotationKeyByPriority(final int priority) {
		return "annotation-" + String.valueOf(priority);
	}

	/**
	 * 16x16 image
	 *
	 * @return annotation-"priority" (yes, same as for annotations)
	 */
	public static String getPriorityColumnKeyByPriority(final int priority) {
		return "annotation-" + String.valueOf(priority);
	}

	public static void initialize(final ImageRegistry reg) {
		for (int priority = ImageRegistryKey.MIN_PRIORITY; priority <= ImageRegistryKey.MAX_PRIORITY; priority++) {
			final String imageRegistryKey = ImageRegistryKey.getFileDecoratorKeyByPriority(priority);
			final String imageFilePath = "/icons/" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			final ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ImageRegistryKey.ACTIVATOR_CLASS,
					imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}

		for (int priority = ImageRegistryKey.MIN_PRIORITY; priority <= ImageRegistryKey.MAX_PRIORITY; priority++) {
			final String imageRegistryKey = ImageRegistryKey.getAnnotationKeyByPriority(priority);
			final String imageFilePath = "/icons/" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			final ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ImageRegistryKey.ACTIVATOR_CLASS,
					imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}
	}
}
