package org.caleydo.rcp.command.handler.view;

import org.caleydo.rcp.view.opengl.RcpGLDendrogramVerticalView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenDendrogramVerticallHandler
	extends AbstractHandler
	implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView(RcpGLDendrogramVerticalView.ID);
		}
		catch (PartInitException e) {
			e.printStackTrace();
		}

		return null;
	}

}
