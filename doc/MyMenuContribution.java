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
package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 *
 * @author Christian Wulf
 *
 */
public class MyMenuContribution extends AbstractContributionFactory {

	public MyMenuContribution() {
		super("menu:x", "my.namespace");
	}

	@Override
	public void createContributionItems(final IServiceLocator serviceLocator, final IContributionRoot additions) {
		final IHandlerService handlerService = serviceLocator.getService(IHandlerService.class);
		// handlerService.getCurrentState();

		// additions.addContributionItem(item, visibleWhen);
		// TODO Auto-generated method stub
		return;
	}

}
