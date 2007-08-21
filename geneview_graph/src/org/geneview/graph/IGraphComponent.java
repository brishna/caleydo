/**
 * 
 */
package org.geneview.graph;


/**
 * Top level interface for all graph components.
 * 
 * @see org.geneview.graph.IGraph
 * @see org.geneview.graph.IGraphItem
 * 
 * @author Michael Kalkusch
 *
 */
public interface IGraphComponent {

	
	/**
	 * Removes this GraphItem from all objects linked to it.
	 * Calls all other GraphItmes and removes the reference; also removed references inside all linked graphs
	 * based on the data stored inside this IGraphItem.
	 * 
	 * Attention: IGraph objects ignore this call; its implementation is an empty method, because 
	 * the IGraphItem are responsible for unregistering themselves at their referenced graphs.
	 * 
	 * Note: If the hole graph is disposed and the IGraphItem is not linked to other graphs this method can be skipped.
	 * To test if only a graph is linked call containsOtherGraph(IGraph)
	 * 
	 * @see org.geneview.graph.IGraphItem#containsOtherGraph(IGraph)
	 */
	public void disposeItem();
	
}
