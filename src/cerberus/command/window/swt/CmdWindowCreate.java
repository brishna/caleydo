package cerberus.command.window.swt;


import cerberus.command.ICommand;
import cerberus.command.base.ACmdCreate_IdTargetLabelAttr;
import cerberus.manager.IGeneralManager;
import cerberus.util.exception.CerberusRuntimeException;
//import cerberus.xml.parser.command.CommandQueueSaxType;
import cerberus.xml.parser.parameter.IParameterHandler;

/**
 * Command class triggers the creation of
 * a SWT GUI window.
 * 
 * @author Marc Streit
 * @author Michael Kalkusch
 *
 */
public class CmdWindowCreate
extends ACmdCreate_IdTargetLabelAttr
implements ICommand 
{
	protected String sLayoutAttributes;
	
	public CmdWindowCreate( final IGeneralManager refGeneralManager ) 
	{
		super( refGeneralManager );	
	}

	public void doCommand() throws CerberusRuntimeException
	{
		refGeneralManager.getSingelton().
			getSWTGUIManager().createWindow( 
					iUniqueTargetId, sLabel, sLayoutAttributes);	
	}

	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
		
		assert refParameterHandler != null: "ParameterHandler object is null!";	
		
		super.setParameterHandler(refParameterHandler);	
		
		sLayoutAttributes = sAttribute1;
	}
	
	public void undoCommand() throws CerberusRuntimeException
	{
		// TODO Auto-generated method stub
	}
}
