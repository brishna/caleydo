package org.caleydo.core.data.selection.delta;

import java.util.Collection;

import org.caleydo.core.data.id.IDType;

/**
 * Interface for all deltas that contain information on changes and are used to submit information to other
 * views. A delta contains a number of {@link IDeltaItem}s.
 * 
 * @author Alexander Lex
 */
public interface IDelta<T extends IDeltaItem>
	extends Iterable<T> {
	/**
	 * Return an array list of {@link SelectionDeltaItem}. This contains data on what selections have changed
	 * 
	 * @return
	 */
	public Collection<T> getAllItems();

	/** Set the id type of the delta */
	public void setIDType(IDType idType);

	/**
	 * Get the type of the id, which has to be listed in {@link EIDType}
	 * 
	 * @return the type of the id
	 */
	public IDType getIDType();

	/**
	 * Get the type of the secondary ID, which has to be listed in {@link EIDType}. Returns null if no
	 * internal ID type was set
	 * 
	 * @return the type of the internal id
	 */
	public IDType getSecondaryIDType();

	/**
	 * Returns the number of elements in the selection delta
	 * 
	 * @return the size
	 */
	public int size();

	/**
	 * Add a new item to the delta
	 * 
	 * @param deltaItem
	 *            the delta item
	 */
	public void add(T deltaItem);

}
