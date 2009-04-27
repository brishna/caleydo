package org.caleydo.core.manager.event;

/**
 * Interface for a mediator sender TODO doku
 * 
 * @author Michael Kalkusch
 * @author Alexander Lex
 * @author Marc Streit
 */
@Deprecated
public interface IMediatorSender {
	/**
	 * Trigger an update on the selections. The information is contained in the selection delta specified
	 * 
	 * @param eMediatorType
	 *            TODO
	 * @param colSelectionCommand
	 *            TODO
	 */
	// public void triggerSelectionUpdate(EMediatorType eMediatorType,
	// ISelectionDelta selectionDelta, Collection<SelectionCommand>
	// colSelectionCommand);
	//
	// public void triggerVAUpdate(EMediatorType eMediatorType,
	// IVirtualArrayDelta delta,
	// Collection<SelectionCommand> colSelectionCommand);
	public void triggerEvent(EMediatorType eMediatorType, IEventContainer eventContainer);
}
