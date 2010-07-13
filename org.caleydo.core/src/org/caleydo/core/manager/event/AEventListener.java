package org.caleydo.core.manager.event;

/**
 * <p>
 * General event listener interface for classes that handle events. Inherited classes should usually handle
 * exactly one type of event. Related views or mediators should register one listener to exactly one event
 * within the event system.
 * </p>
 * <p>
 * Optionally, a listener can have a dataDomainType set. As a consequence, it will receive only those events
 * that are of the same dataDomainType or have no dataDomainType specified.
 * </p>
 * 
 * @author Werner Puff
 * @author Alexander Lex
 */
public abstract class AEventListener<T extends IListenerOwner> {

	/** related handling object, usually a view or manager-type class */
	protected T handler = null;
	/** the dataDomainType string that decides whether a listenere listenes for events for this data domain */
	protected String dataDomainType = null;
	/**
	 * flag determining whether a listener is listenig to both it's datadomain events and events where no
	 * datadomain is specified (false), or only to events with the datadomain specified
	 */
	protected boolean isOnlyDataDomain = false;

	/**
	 * Returns the related handler object to this listener.
	 * 
	 * @return related handler object
	 */
	public T getHandler() {
		return handler;
	}

	/**
	 * Sets the related handler object to this listener. Usually listeners are created by its handler object.
	 * 
	 * @param handler
	 *            related handler object to set.
	 */
	public void setHandler(T handler) {
		this.handler = handler;
	}

	/**
	 * Set the dataDomainType - if this is set the listener will receive only events that have this or no
	 * dataDomainType. Notice that this has to be set before the listener is registered with the event
	 * publisher.
	 * 
	 * @param dataDomainType
	 */
	public void setDataDomainType(String dataDomainType) {
		this.dataDomainType = dataDomainType;
	}

	/**
	 * Returns the dataDomainType or null if none is specified
	 * 
	 * @return
	 */
	public String getDataDomainType() {
		return dataDomainType;
	}

	/**
	 * @param dataDomainType
	 */
	public void setOnlyToDataDomain(String dataDomainType) {

	}

	public boolean isOnlyDataDomain() {
		return isOnlyDataDomain;
	}

	/**
	 * <p>
	 * Takes care of thread safe execution of events.
	 * </p>
	 * <p>
	 * Filters events based on the events-sender and the listeners related event-handler (the receiver).
	 * Events are only passed to the handleEvent(AEvent) method, if sender and receiver are not the same.
	 * </p>
	 * 
	 * @param event
	 *            event object to handle by this listener
	 */
	public void queueEvent(AEvent event) {
		// if (event.getSender() == null) {
		// GeneralManager.get().getLogger().log(
		// new Status(Status.WARNING, GeneralManager.PLUGIN_ID, "handling " + this.getClass().getName()
		// + " with sender==null"));
		// }
		if (event.getSender() != this.getHandler()) {
			handler.queueEvent(this, event);
		}
	}

	/**
	 * <p>
	 * Decodes the event and its payload and calls view or management related methods of related views,
	 * mediators or managers.
	 * </p>
	 * <p>
	 * Do not call this method from any other thread than the one where the changes are applied. To submit a
	 * event from a different thread call {@link #queueEvent(AEvent)} instead.
	 * </p>
	 * 
	 * @param event
	 *            event object to handle by this listener
	 */
	public abstract void handleEvent(AEvent event);

	/**
	 * Integrity check for listener. Most importantly the existence of a handler is checked. This method can
	 * be overridden, if further checks are necessary.
	 * 
	 * @return true if a handler is present else throws exception
	 * @throws NullPointerException
	 *             when handler is null
	 */
	public boolean checkIntegrity() {
		if (handler == null)
			throw new NullPointerException("Handler in " + this + " was null");

		return true;
	}
}
