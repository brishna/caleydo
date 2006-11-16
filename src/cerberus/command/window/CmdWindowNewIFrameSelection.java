/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.command.window;

import cerberus.manager.IGeneralManager;
import cerberus.command.ICommand;
import cerberus.command.CommandType;
import cerberus.command.base.ACmdHandleSet;
import cerberus.command.base.ICmdHandleSet;
//import cerberus.net.dwt.swing.collection.DSwingSelectionCanvas;
//import cerberus.net.dwt.swing.mdi.DInternalFrame;
import cerberus.util.exception.CerberusRuntimeException;

/**
 * Creates a internal frame dispaying a 2D histogram.
 * 
 * @author Michael Kalkusch
 *
 */
public class CmdWindowNewIFrameSelection 
extends ACmdHandleSet 
implements ICommand, ICmdHandleSet {

//	private DSwingSelectionCanvas refDSwingSelectionCanvas = null;

	
	/**
	 * ISet the reference to the parent JComponent.
	 * 
	 * @param setParentComonent parent JComponenet
	 */
	public CmdWindowNewIFrameSelection( final IGeneralManager refGeneralManager,
			final int iTargetFrameId ) {
		super( refGeneralManager,
				iTargetFrameId,
				"IVirtualArray" );
	}
	
	
	/* (non-Javadoc)
	 * @see cerberus.command.ICommand#doCommand()
	 */
	public void doCommand() throws CerberusRuntimeException {
		
		setGuiTextHeader( "IVirtualArray" );
		
//		DInternalFrame newDInternalFrame = subCmdNewIFrame.doCommand_getDInternalFrame();
//	
//		refDSwingSelectionCanvas = new DSwingSelectionCanvas( refGeneralManager );
//		refDSwingSelectionCanvas.updateState();
//		refDSwingSelectionCanvas.setVisible( true );
//		
//		newDInternalFrame.add( refDSwingSelectionCanvas );
//		newDInternalFrame.setMaximizable( false );		
//		newDInternalFrame.pack();
	}

	/* (non-Javadoc)
	 * @see cerberus.command.ICommand#undoCommand()
	 */
	public void undoCommand() throws CerberusRuntimeException {
		
	}

	/* (non-Javadoc)
	 * @see cerberus.command.ICommand#getCommandType()
	 */
	public CommandType getCommandType() throws CerberusRuntimeException {
		return CommandType.WINDOW_IFRAME_OPEN_SELECTION;
	}

}
