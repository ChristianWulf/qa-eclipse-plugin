package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

public class MyMenuContribution extends AbstractContributionFactory {

	public MyMenuContribution() {
		super("menu:x", "my.namespace");
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		IHandlerService handlerService = serviceLocator.getService(IHandlerService.class);
		// handlerService.getCurrentState();

		// additions.addContributionItem(item, visibleWhen);
		// TODO Auto-generated method stub
		return;
	}

}
