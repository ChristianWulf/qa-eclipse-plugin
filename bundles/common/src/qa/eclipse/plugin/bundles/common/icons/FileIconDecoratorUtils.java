/***************************************************************************
 * Copyright (C) 2019
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
package qa.eclipse.plugin.bundles.common.icons;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

/**
 * @author Reiner Jung
 *
 * @since 1.1.0
 */
public final class FileIconDecoratorUtils {

	/** ensure only used as utility class. */
	private FileIconDecoratorUtils() {
		// nothing to be done here
	}

	/**
	 * Refresh decorators for file icons.
	 *
	 * @param decoratorId
	 *            id of the decorator
	 */
	public static void refresh(final String decoratorId) {
		final IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		final IBaseLabelProvider decorator = manager.getBaseLabelProvider(decoratorId);
		if (decorator != null) { // decorator is enabled
			final ILabelProviderListener listener = (ILabelProviderListener) manager;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.labelProviderChanged(new LabelProviderChangedEvent(decorator));
				}
			});
		}
	}
}
