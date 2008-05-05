package org.caleydo.rcp.views;

//import java.awt.FlowLayout;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.RowLayout;
import java.util.ArrayList;

import org.caleydo.core.command.CommandQueueSaxType;
import org.caleydo.core.command.event.CmdEventCreateMediator;
import org.caleydo.core.manager.IViewManager;
import org.caleydo.core.manager.IEventPublisher.MediatorType;
import org.caleydo.core.manager.type.ManagerObjectType;
import org.caleydo.core.view.swt.browser.HTMLBrowserViewRep;
import org.caleydo.rcp.Application;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class HTMLBrowserView 
extends ViewPart {

	public static final String ID = "org.caleydo.rcp.views.HTMLBrowserView";
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		parent.setLayout(new RowLayout(SWT.VERTICAL));
		
		IViewManager viewManager = (IViewManager) Application.generalManager.getViewGLCanvasManager();
		
		int iUniqueId = 85401;
		
		HTMLBrowserViewRep browserView = (HTMLBrowserViewRep)viewManager
				.createView(ManagerObjectType.VIEW_SWT_BROWSER,
						iUniqueId,
						-1, 
						"Browser");
		
		viewManager.registerItem(
				browserView, 
				iUniqueId, 
				ManagerObjectType.VIEW);

		browserView.setAttributes(1000, 800);
		browserView.initViewRCP(parent);
		browserView.drawView();	
		
		ArrayList<Integer> iAlSender = new ArrayList<Integer>();
		ArrayList<Integer> iAlReceiver = new ArrayList<Integer>();
		iAlSender.add(iUniqueId);
		//iAlReceiver.add(85401); 
		iAlReceiver.add(83401);
		
		CmdEventCreateMediator cmd = (CmdEventCreateMediator)Application.generalManager.getCommandManager()
		 	.createCommandByType(CommandQueueSaxType.CREATE_EVENT_MEDIATOR);
		cmd.setAttributes(-1, iAlSender, iAlReceiver, MediatorType.SELECTION_MEDIATOR);
		cmd.doCommand();

		CmdEventCreateMediator cmdReverse = (CmdEventCreateMediator)Application.generalManager.getCommandManager()
	 		.createCommandByType(CommandQueueSaxType.CREATE_EVENT_MEDIATOR);
		cmdReverse.setAttributes(-1, iAlReceiver, iAlSender, MediatorType.SELECTION_MEDIATOR);
		cmdReverse.doCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
