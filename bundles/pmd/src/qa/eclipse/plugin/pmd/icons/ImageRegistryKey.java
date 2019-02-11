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
package qa.eclipse.plugin.pmd.icons;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import qa.eclipse.plugin.pmd.PmdUIPlugin;

public final class ImageRegistryKey {

	private static final Class<?> ACTIVATOR_CLASS = PmdUIPlugin.class;

	private ImageRegistryKey() {
		// utility class
	}

	/**
	 * @param pmdPriority priority value
	 * @return "pmdPriority"-decorator
	 */
	public static String getFileDecoratorKeyByPriority(final int pmdPriority) {
		return String.valueOf(pmdPriority) + "-decorator";
	}

	/**
	 * @param pmdPriority priority value
	 * @return "pmdPriority"-annotation
	 */
	public static String getAnnotationKeyByPriority(final int pmdPriority) {
		return String.valueOf(pmdPriority) + "-annotation";
	}

	/**
	 * @param pmdPriority priority value
	 * @return "pmdPriority"-annotation (yes, same as for annotations)
	 */
	public static String getPriorityColumnKeyByPriority(final int pmdPriority) {
		return String.valueOf(pmdPriority) + "-annotation";
	}

	public static void initialize(final ImageRegistry reg) {
		for (int priority = 1; priority <= 5; priority++) {
			final String imageRegistryKey = ImageRegistryKey.getFileDecoratorKeyByPriority(priority);
			final String imageFilePath = "/icons/priority" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			final ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ImageRegistryKey.ACTIVATOR_CLASS, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}

		for (int priority = 1; priority <= 5; priority++) {
			final String imageRegistryKey = ImageRegistryKey.getAnnotationKeyByPriority(priority);
			final String imageFilePath = "/icons/pmd" + imageRegistryKey + ".png";
			// AbstractUIPlugin.imageDescriptorFromPlugin always returns null
			final ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ImageRegistryKey.ACTIVATOR_CLASS, imageFilePath);
			reg.put(imageRegistryKey, imageDescriptor);
		}
	}
}
