/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.data.collection.selection.iterator;


import cerberus.data.collection.iterator.ICollectionIterator;
import cerberus.util.exception.CerberusRuntimeException;


/**
 * Iterator for prometheus.data.collection.Selection
 * 
 * @author Michael Kalkusch
 *
 */
public interface ISelectionIterator  
extends ICollectionIterator {

	
	/**
	 * Returns the total number of elements in the list.
	 * 
	 * @return total number of items to be iterated
	 */
	public int size();
	
	/**
	 * Numer of remaining objects.
	 * 
	 * @return number of remaining obejcts
	 */
	public int remaining();	
	
	/**
	 * Get the current virtual index.
	 * 
	 * The real array indexs is returned by calling (int) prometheus.data.collection.iterator.CollectionIterator#next()
	 * 
	 * @return current virtual index
	 */
	public int getVirtualIndex();

	/**
	 * Sets a new current virtual indexs.
	 * 
	 * @param iSetVirtualIndex new virtual index
	 * @throws CerberusRuntimeException if iSetVirtualIndex can not be set
	 */
	public void setVirtualIndex(int iSetVirtualIndex)
			throws CerberusRuntimeException;
	
	/**
	 * Sets iterator at + 1 element and terminates the iteration.
	 *
	 */
	public void setToEnd();
	
}
