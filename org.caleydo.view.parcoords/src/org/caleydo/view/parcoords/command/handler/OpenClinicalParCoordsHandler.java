package org.caleydo.view.parcoords.command.handler;

import org.caleydo.view.parcoords.RcpGLParCoordsClinicalView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenClinicalParCoordsHandler extends AbstractHandler
		implements
			IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.showView(RcpGLParCoordsClinicalView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		return null;
	}

}
