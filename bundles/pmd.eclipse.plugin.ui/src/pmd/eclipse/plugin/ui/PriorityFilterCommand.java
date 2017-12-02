package pmd.eclipse.plugin.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import pmd.eclipse.plugin.views.PmdViolationsView;

public class PriorityFilterCommand extends AbstractHandler {

	private static final String PARAM_PRIORITY = "pmd.eclipse.plugin.ui.command.filter.priority.parameter";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String priorityParameter = event.getParameter(PARAM_PRIORITY);
		Integer priority = Integer.valueOf(priorityParameter);

		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		if (oldValue) { // if was enabled, i.e., now disabled

		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
				IWorkbenchPage page = window.getActivePage();
				try {
					IViewPart viewPart = page.showView(PmdViolationsView.ID);
					PmdViolationsView violationView = (PmdViolationsView) viewPart;
					violationView.filterByPriority(priority);
				} catch (PartInitException e) {
					throw new IllegalArgumentException(e);
				}
			}
		});

		return null;
	}

}
