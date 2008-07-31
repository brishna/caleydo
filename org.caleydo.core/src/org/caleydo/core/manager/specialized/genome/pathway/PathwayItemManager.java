package org.caleydo.core.manager.specialized.genome.pathway;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.caleydo.core.data.graph.pathway.item.edge.PathwayReactionEdgeGraphItem;
import org.caleydo.core.data.graph.pathway.item.edge.PathwayReactionEdgeGraphItemRep;
import org.caleydo.core.data.graph.pathway.item.edge.PathwayRelationEdgeGraphItem;
import org.caleydo.core.data.graph.pathway.item.edge.PathwayRelationEdgeGraphItemRep;
import org.caleydo.core.data.graph.pathway.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.data.graph.pathway.item.vertex.PathwayVertexGraphItemRep;
import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.specialized.genome.IPathwayItemManager;
import org.caleydo.core.manager.type.EManagerObjectType;
import org.caleydo.core.manager.type.EManagerType;
import org.caleydo.util.graph.EGraphItemHierarchy;
import org.caleydo.util.graph.EGraphItemProperty;
import org.caleydo.util.graph.IGraph;
import org.caleydo.util.graph.IGraphItem;

/**
 * The element manager is in charge for handling the items. Items are vertices
 * and edges. The class is implemented as a Singleton.
 * 
 * @author Marc Streit
 * @author Michael Kalkusch
 */
public class PathwayItemManager
	extends AManager
	implements IPathwayItemManager, Serializable
{

	private static final long serialVersionUID = 1L;

	private HashMap<Integer, IGraphItem> hashVertexIdToGraphItem;

	private HashMap<String, IGraphItem> hashVertexNameToGraphItem;

	private HashMap<Integer, Integer> hashDavidIdToPathwayVertexGraphItemId;

	private HashMap<Integer, Integer> hashPathwayVertexGraphItemIdToDavidId;

	private boolean bHashDavidIdToPathwayVertexGraphItemIdInvalid = true;

	/**
	 * Constructor.
	 */
	public PathwayItemManager(final IGeneralManager generalManager)
	{

		super(generalManager, IGeneralManager.iUniqueId_TypeOffset_Pathways_Vertex,
				EManagerType.DATA_PATHWAY_ELEMENT);

		hashVertexIdToGraphItem = new HashMap<Integer, IGraphItem>();
		hashVertexNameToGraphItem = new HashMap<String, IGraphItem>();
		hashDavidIdToPathwayVertexGraphItemId = new HashMap<Integer, Integer>();
		hashPathwayVertexGraphItemIdToDavidId = new HashMap<Integer, Integer>();
	}

	public IGraphItem createVertex(final String sName, final String sType,
			final String sExternalLink, final String sReactionId)
	{

		// Check if same vertex is already contained
		if (hashVertexNameToGraphItem.containsKey(sName))
		{
			// Return existing vertex
			return hashVertexNameToGraphItem.get(sName);
		}

		int iGeneratedId = createId(EManagerObjectType.PATHWAY_VERTEX);

		IGraphItem pathwayVertex = new PathwayVertexGraphItem(iGeneratedId, sName, sType,
				sExternalLink, sReactionId);

		hashVertexIdToGraphItem.put(iGeneratedId, pathwayVertex);

		generalManager.getPathwayManager().getRootPathway().addItem(pathwayVertex);

		hashVertexNameToGraphItem.put(sName, pathwayVertex);

		return pathwayVertex;
	}

	public IGraphItem createVertexGene(final String sName, final String sType,
			final String sExternalLink, final String sReactionId, final int iDavidId)
	{

		IGraphItem tmpGraphItem = createVertex(sName, sType, sExternalLink, sReactionId);

		// Extract David gene ID and add element
		hashDavidIdToPathwayVertexGraphItemId.put(iDavidId, tmpGraphItem.getId());
		hashPathwayVertexGraphItemIdToDavidId.put(tmpGraphItem.getId(), iDavidId);

		return tmpGraphItem;
	}

	public IGraphItem createVertexRep(final IGraph parentPathway,
			final ArrayList<IGraphItem> alVertexGraphItem, final String sName,
			final String sShapeType, final short shHeight, final short shWidth,
			final short shXPosition, final short shYPosition)
	{

		int iGeneratedId = createId(EManagerObjectType.PATHWAY_VERTEX_REP);
		IGraphItem pathwayVertexRep = new PathwayVertexGraphItemRep(iGeneratedId, sName,
				sShapeType, shHeight, shWidth, shXPosition, shYPosition);

		hashVertexIdToGraphItem.put(iGeneratedId, pathwayVertexRep);

		parentPathway.addItem(pathwayVertexRep);

		pathwayVertexRep.addGraph(parentPathway, EGraphItemHierarchy.GRAPH_PARENT);

		Iterator<IGraphItem> iterVertexGraphItem = alVertexGraphItem.iterator();
		IGraphItem pathwayVertex;
		while (iterVertexGraphItem.hasNext())
		{
			pathwayVertex = iterVertexGraphItem.next();

			pathwayVertexRep.addItem(pathwayVertex, EGraphItemProperty.ALIAS_PARENT);

			pathwayVertex.addItem(pathwayVertexRep, EGraphItemProperty.ALIAS_CHILD);
		}
		return pathwayVertexRep;
	}

	public IGraphItem createVertexRep(final IGraph parentPathway,
			final IGraphItem parentVertex, final String sName, final String sShapeType,
			final String sCoords)
	{

		int iGeneratedId = createId(EManagerObjectType.PATHWAY_VERTEX_REP);
		IGraphItem pathwayVertexRep = new PathwayVertexGraphItemRep(iGeneratedId, sName,
				sShapeType, sCoords);

		hashVertexIdToGraphItem.put(iGeneratedId, pathwayVertexRep);

		parentPathway.addItem(pathwayVertexRep);

		pathwayVertexRep.addGraph(parentPathway, EGraphItemHierarchy.GRAPH_PARENT);

		pathwayVertexRep.addItem(parentVertex, EGraphItemProperty.ALIAS_PARENT);

		parentVertex.addItem(pathwayVertexRep, EGraphItemProperty.ALIAS_CHILD);

		return pathwayVertexRep;
	}

	public IGraphItem createRelationEdge(final IGraphItem graphItemIn,
			final IGraphItem graphItemOut, final String sType)
	{

		int iGeneratedId = createId(EManagerObjectType.PATHWAY_EDGE);
		IGraphItem pathwayRelationEdge = new PathwayRelationEdgeGraphItem(iGeneratedId, sType);

		IGraph rootPathway = generalManager.getPathwayManager().getRootPathway();

		// Add edge to root pathway
		rootPathway.addItem(pathwayRelationEdge);

		// Add root pathway to edge
		pathwayRelationEdge.addGraph(rootPathway, EGraphItemHierarchy.GRAPH_PARENT);

		// Add connection to incoming and outgoing items
		pathwayRelationEdge.addItemDoubleLinked(graphItemIn, EGraphItemProperty.INCOMING);
		pathwayRelationEdge.addItemDoubleLinked(graphItemOut, EGraphItemProperty.OUTGOING);

		return pathwayRelationEdge;
	}

	public void createRelationEdgeRep(final IGraph parentPathway,
			final IGraphItem pathwayRelationEdge, final IGraphItem graphItemIn,
			final IGraphItem graphItemOut)
	{

		int iGeneratedId = createId(EManagerObjectType.PATHWAY_EDGE_REP);
		IGraphItem pathwayRelationEdgeRep = new PathwayRelationEdgeGraphItemRep(iGeneratedId);

		// Add edge to pathway representation
		parentPathway.addItem(pathwayRelationEdgeRep);

		// Add pathway representation to created edge
		pathwayRelationEdgeRep.addGraph(parentPathway, EGraphItemHierarchy.GRAPH_PARENT);

		// Add edge data to representation as ALIAS_PARENT
		pathwayRelationEdgeRep.addItem(pathwayRelationEdge, EGraphItemProperty.ALIAS_PARENT);

		// Add edge representation to data as ALIAS_CHILD
		pathwayRelationEdge.addItem(pathwayRelationEdgeRep, EGraphItemProperty.ALIAS_CHILD);

		// Add connection to incoming and outgoing items
		pathwayRelationEdgeRep.addItemDoubleLinked(graphItemIn, EGraphItemProperty.INCOMING);
		pathwayRelationEdgeRep.addItemDoubleLinked(graphItemOut, EGraphItemProperty.OUTGOING);
	}

	public IGraphItem createReactionEdge(final IGraph parentPathway,
			final String sReactionName, final String sReactionType)
	{

		// Create edge (data)
		int iGeneratedId = createId(EManagerObjectType.PATHWAY_EDGE_REP);
		IGraphItem pathwayReactionEdge = new PathwayReactionEdgeGraphItem(iGeneratedId,
				sReactionName, sReactionType);

		// Create edge representation
		iGeneratedId = createId(EManagerObjectType.PATHWAY_EDGE_REP);
		IGraphItem pathwayReactionEdgeRep = new PathwayReactionEdgeGraphItemRep(iGeneratedId);

		IGraph rootPathway = generalManager.getPathwayManager().getRootPathway();

		// Add edge to root pathway
		rootPathway.addItem(pathwayReactionEdge);

		// Add root pathway to edge
		pathwayReactionEdge.addGraph(rootPathway, EGraphItemHierarchy.GRAPH_PARENT);

		// Add edge to pathway representation
		parentPathway.addItem(pathwayReactionEdgeRep);

		// Add pathway representation to created edge
		pathwayReactionEdgeRep.addGraph(parentPathway, EGraphItemHierarchy.GRAPH_PARENT);

		// Add edge data to representation as ALIAS_PARENT
		pathwayReactionEdgeRep.addItem(pathwayReactionEdge, EGraphItemProperty.ALIAS_PARENT);

		// Add edge representation to data as ALIAS_CHILD
		pathwayReactionEdge.addItem(pathwayReactionEdgeRep, EGraphItemProperty.ALIAS_CHILD);

		return pathwayReactionEdgeRep;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#getItem(int)
	 */
	public Object getItem(int iItemId)
	{

		if (!hashVertexIdToGraphItem.containsKey(iItemId))
			return null;

		return hashVertexIdToGraphItem.get(iItemId);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.caleydo.core.manager.data.IPathwayItemManager#
	 * getPathwayVertexGraphItemIdByDavidId(int)
	 */
	// TODO: throw exception
	public final int getPathwayVertexGraphItemIdByDavidId(final int iDavidId)
	{

		if (hashDavidIdToPathwayVertexGraphItemId.containsKey(iDavidId))
			return hashDavidIdToPathwayVertexGraphItemId.get(iDavidId);

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.caleydo.core.manager.data.IPathwayItemManager#
	 * getDavidIdByPathwayVertexGraphItemId(int)
	 */
	public int getDavidIdByPathwayVertexGraphItemId(final int iPathwayVertexGraphItemId)
	{

		if (hashPathwayVertexGraphItemIdToDavidId.containsKey(iPathwayVertexGraphItemId))
			return hashPathwayVertexGraphItemIdToDavidId.get(iPathwayVertexGraphItemId);

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IGeneralManager#hasItem(int)
	 */
	public boolean hasItem(int iItemId)
	{

		if (hashVertexIdToGraphItem.containsKey(iItemId))
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IManager#registerItem(java.lang.Object,
	 * int, org.caleydo.core.manager.type.ManagerObjectType)
	 */
	public boolean registerItem(Object registerItem, int itemId)
	{

		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IManager#size()
	 */
	public int size()
	{

		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.IManager#unregisterItem(int,
	 * org.caleydo.core.manager.type.ManagerObjectType)
	 */
	public boolean unregisterItem(int itemId)
	{

		// TODO Auto-generated method stub
		return false;
	}
}
