package org.geneview.rcp;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import cerberus.application.core.CerberusBootloader;
import cerberus.manager.IGeneralManager;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	// FIXME: should not be static!
	public static IGeneralManager refGeneralManager;
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {

		startCerberusCore();
		
		Display display = PlatformUI.createDisplay();
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		
//		if ( Application.refGeneralManager != null ) {
//			Application.refGeneralManager.stop();
//		}
		
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	protected void startCerberusCore() {
		
		CerberusBootloader prototype = new CerberusBootloader();
			
		prototype.setXmlFileName(
				"data/bootstrap/rcp/bootstrap_sample_RCP.xml"); 	

		Application.refGeneralManager = prototype.getGeneralManager();

		prototype.run();
	}
		
}
