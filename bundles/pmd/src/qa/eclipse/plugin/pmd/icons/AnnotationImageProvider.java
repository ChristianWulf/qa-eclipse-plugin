/***************************************************************************
 * Copyright (C) 2019 christian Wulf
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import qa.eclipse.plugin.bundles.common.ImageRegistryKeyUtils;
import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.markers.PmdViolationMarker;

/**
 * @author Christian Wulf
 *
 */
public class AnnotationImageProvider implements IAnnotationImageProvider {

	private final ImageRegistry imageRegistry;

	/**
	 * Is created automatically once from the Eclipse Plugin Environment.
	 */
	public AnnotationImageProvider() {
		this.imageRegistry = PmdUIPlugin.getDefault().getImageRegistry();
	}

	@Override
	public Image getManagedImage(final Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		// MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
		// IMarker marker = markerAnnotation.getMarker();
		// PmdViolationMarker violationMarker = new PmdViolationMarker(marker);

		final int priority = this.getPriorityFromAnnotation((MarkerAnnotation) annotation);

		// apply filter
		String imageRegistryKey = null;
		final int lowestAllowedPriority = 5;
		if (priority <= lowestAllowedPriority) {
			imageRegistryKey = ImageRegistryKeyUtils.getAnnotationKeyByPriority("pmd", priority);
		}

		return this.imageRegistry.get(imageRegistryKey);
		// return null;
	}

	// letting getManagedImage() and getImageDescriptorId() return null
	// only hides the editor annotation icon,
	// but text hovering and left-click still work

	@Override
	public String getImageDescriptorId(final Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		final int priority = this.getPriorityFromAnnotation((MarkerAnnotation) annotation);

		// apply filter
		String imageRegistryKey = null;
		final int lowestAllowedPriority = 5;
		if (priority <= lowestAllowedPriority) {
			imageRegistryKey = ImageRegistryKeyUtils.getAnnotationKeyByPriority("pmd", priority);
		}

		return imageRegistryKey;
		// return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(final String imageDescriptorId) {
		return this.imageRegistry.getDescriptor(imageDescriptorId);
	}

	private int getPriorityFromAnnotation(final MarkerAnnotation markerAnnotation) {
		final IMarker marker = markerAnnotation.getMarker();
		final PmdViolationMarker violationMarker = new PmdViolationMarker(marker);
		return violationMarker.getPriority();
	}

}
