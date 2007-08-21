/**
 * 
 */
package org.geneview.graph;

import java.util.Collection;

import org.geneview.graph.EGraphItemKind;
import org.geneview.graph.EGraphItemProperty;
import org.geneview.graph.item.IGraphDataHandler;
import org.geneview.graph.IGraphComponent;
import org.geneview.graph.IGraphItemHierarchy;
import org.geneview.graph.GraphRuntimeException;

/**
 * Interface for all graph items. Could be nodes or edges.
 * 
 * @see org.geneview.graph.EGraphItemHierarchy
 * @see org.geneview.graph.EGraphItemProperty
 * @see org.geneview.graph.EGraphItemKind
 * @see org.geneview.graph.IGraph
 * 
 * @author Michael Kalkusch
 *
 */
public interface IGraphItem extends IGraphDataHandler, IGraphItemHierarchy, IGraphComponent {

	
	/* ---------------- */

	/**
	 * @see org.geneview.graph.IGraphItem#setGraphKind(EGraphItemKind)
	 * 
	 *  @return type of this GraphItem
	 */
	public EGraphItemKind getGraphKind();
	
	/**
	 * Set the type of this GraphItem.
	 * 
	 * @see org.geneview.graphB.IGraphItem#getGraphKind()
	 * 
	 * @param kind type for this GraphItem
	 */
	public void setGraphKind( EGraphItemKind kind );
	
	
	/* ---------------- */

	/**
	 * Get a Collection of IGraphItem with respect to their EGraphItemProperty.
	 * Note, if prop == EGraphItemProperty.NONE or null all IGraphItem's are returned.
	 *  
	 * @param prop specify, which IGraphItem's should be returned; if prop == EGraphItemProperty.NONE or null all IGraphItem's are returned.
	 * @return collection of IGraphItems matching prop
	 */
	public Collection<IGraphItem> getAllItemsByProp(EGraphItemProperty prop);
	
	/**
	 * Adds a new IGraphItem with prop.
	 * 
	 * Note, if prop == EGraphItemProperty.NONE or null a GraphRuntimeException is thrown
	 * 
	 * @param item new IGraphItem to be added
	 * @param prop property linked to the added item
	 */
	public void addItem(IGraphItem item, EGraphItemProperty prop) throws GraphRuntimeException;
	
	/**
	 * 
	 * Note, if prop == EGraphItemProperty.NONE or null IGraphItem is removed from any of the lists "INCOMING,OUTGOING" and "ALIAS"
	 * 
	 * @param item IGraphItem to be removed
	 * @param prop specify from which internal structure item should be removed
	 * @return TRUE if removal was successful, FLASE indicates that item was not registered 
	 */
	public boolean removeItem(IGraphItem item, EGraphItemProperty prop);
	
	/**
	 * Test if IGraphItem item is contained depending on EGraphItemProperty.
	 * 
	 * Note, if prop == EGraphItemProperty.NONE or null all types are matched.
	 * 
	 * @param item object to be tested
	 * @param prop specify context; if prop == EGraphItemProperty.NONE or null all types are matched
	 * @return TURE indicates item is contained
	 */
	public boolean containsItem(IGraphItem item, EGraphItemProperty prop);
	


}
