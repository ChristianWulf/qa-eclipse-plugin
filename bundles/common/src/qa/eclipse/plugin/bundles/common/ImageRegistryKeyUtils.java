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
package qa.eclipse.plugin.bundles.common;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 *
 * @author Christian Wulf
 *
 */
public final class ImageRegistryKeyUtils {

	private ImageRegistryKeyUtils() {
		// utility class
	}

	/**
	 * 8x8 image.
	 *
	 * @param toolName
	 *            name of the check tool used
	 * @param priority
	 *            priority to be displayed
	 * @return decorator-"priority"
	 */
	public static String getFileDecoratorKeyByPriority(final String toolName, final int priority) {
		return toolName + "-decorator-" + priority;
	}

	/**
	 * 16x16 image.
	 *
	 * @param toolName
	 *            name of the check tool used
	 * @param priority
	 *            priority to be displayed
	 *
	 * @return annotation-"priority"
	 */
	public static String getAnnotationKeyByPriority(final String toolName, final int priority) {
		return toolName + "-annotation-" + priority;
	}

	/**
	 * 16x16 image.
	 *
	 * @param toolName
	 *            name of the check tool used
	 * @param priority
	 *            priority to be displayed
	 *
	 * @return annotation-"priority" (yes, same as for annotations)
	 */
	public static String getPriorityColumnKeyByPriority(final String toolName, final int priority) {
		return toolName + "-annotation-" + priority;
	}

	/**
	 * Initialize image registry.
	 *
	 * @param location
	 *            activator class to indicate the plugin root
	 * @param toolName
	 *            name of the check tool used
	 * @param registry
	 *            image registry
	 * @param minPriority
	 *            minimal priority
	 * @param maxPriority
	 *            maximal priority
	 */
	public static void initialize(final Class<?> location, final String toolName, final ImageRegistry registry, final int minPriority, final int maxPriority) {
		for (int priority = minPriority; priority <= maxPriority; priority++) {
			final String imageRegistryKey = ImageRegistryKeyUtils.getFileDecoratorKeyByPriority(toolName, priority);
			final String imageFilePath = "/icons/" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			final ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(location,
					imageFilePath);
			registry.put(imageRegistryKey, imageDescriptor);
		}

		for (int priority = minPriority; priority <= maxPriority; priority++) {
			final String imageRegistryKey = ImageRegistryKeyUtils.getAnnotationKeyByPriority(toolName, priority);
			final String imageFilePath = "/icons/" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			final ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(location,
					imageFilePath);
			registry.put(imageRegistryKey, imageDescriptor);
		}
	}
}
