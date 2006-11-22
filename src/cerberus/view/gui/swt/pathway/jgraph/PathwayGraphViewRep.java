package cerberus.view.gui.swt.pathway.jgraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;

import cerberus.util.system.StringConversionTool;
import cerberus.data.pathway.element.APathwayEdge;
import cerberus.data.pathway.element.PathwayRelationEdge;
import cerberus.data.pathway.element.APathwayEdge.EdgeType;
import cerberus.data.pathway.element.PathwayRelationEdge.EdgeRelationType;
import cerberus.data.view.rep.pathway.IPathwayVertexRep;
import cerberus.data.view.rep.pathway.jgraph.PathwayVertexRep;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle.EdgeArrowHeadStyle;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle.EdgeLineStyle;
import cerberus.manager.IGeneralManager;
import cerberus.view.gui.swt.pathway.APathwayGraphViewRep;
import cerberus.view.gui.swt.pathway.PathwayViewRep;
import cerberus.view.gui.swt.pathway.jgraph.GPCellViewFactory;
import cerberus.view.gui.swt.pathway.jgraph.GPOverviewPanel;

/**
 * In this class the real drawing of the Pathway happens.
 * For the drawing the JGraph package is used.
 * We can decide here if we want to draw in a new widget
 * or if we want to draw in an existing one.
 * 
 * @author Marc Streit
 *
 */
public class PathwayGraphViewRep
extends APathwayGraphViewRep {
	
	/**
	 * Pathway element positions are read from XML files.
	 * The scaling factor can scale the positions to blow up or
	 * shrink the pathway.
	 */
	protected static final float SCALING_FACTOR = 1.0f;
	
	protected GraphModel refGraphModel;
	
	protected GraphLayoutCache refGraphLayoutCache;
	
	protected JGraph refPathwayGraph;
	
	protected DefaultGraphCell refGraphCell;
	
	protected HashMap<Integer, DefaultGraphCell> vertexIdToCellLUT;
	
	protected boolean bGraphSet = false;
	
	protected boolean bBackgroundOverlaySet = false;
	
	protected GraphUndoManager refUndoManager;
	
	protected GPOverviewPanel refOverviewPanel;
	
	protected Vector<DefaultEdge> vecRelationEdges;
	
	protected Vector<DefaultEdge> vecReactionEdges;
	
	protected Vector<DefaultGraphCell> vecVertices;
	
	/**
	 * Specifies how deep the neighborhood recursion
	 * should visualize surrounding elements.
	 * Default value is 0.
	 */
	protected int iNeighbourhoodDistance = 0;
	
	protected float fScalingFactor;
	
	/**
	 * Counts how often the neighbour recursion is called.
	 * This is needed for the UNDO operation on the next selection.
	 */
	protected int iNeighbourhoodUndoCount = 0;
	
	/**
	 * Flag shows if the neighbours of a cell
	 * are currently displayed. Flag is needed
	 * for UNDO of previous neigbour highlighning.
	 */
	protected boolean bNeighbourhoodShown = false;
	
	public PathwayGraphViewRep(IGeneralManager refGeneralManager, 
			int iParentContainerId) {
		
		super(refGeneralManager, -1, iParentContainerId, "");
		
		vertexIdToCellLUT = new HashMap<Integer, DefaultGraphCell>();
		
		vecRelationEdges = new Vector<DefaultEdge>();
		vecReactionEdges = new Vector<DefaultEdge>();
		vecVertices = new Vector<DefaultGraphCell>();
		
		fScalingFactor = SCALING_FACTOR;
	}

	public void initView() {
		
		class PathwayMarqueeHandler 
		extends BasicMarqueeHandler {

			public boolean isForceMarqueeEvent(MouseEvent event) {
				
		       if(SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 1)
		          return true;
		       else
		          return super.isForceMarqueeEvent(event);
		    } 
			
		    public void mousePressed(final MouseEvent event) {

		    	if (refCurrentPathway != null) 
		    	{		    	
			    	DefaultGraphCell clickedCell = (DefaultGraphCell) refPathwayGraph
							.getFirstCellForLocation(event.getX(), event.getY());
	
			    	// Check if a node or edge was hit.
			    	// If not undo neighborhood visualization and return.
			    	if (clickedCell == null)
			    	{
						for (int iUndoCount = 0; iUndoCount < iNeighbourhoodUndoCount; iUndoCount++)
						{
							refUndoManager.undo(refGraphLayoutCache);
						}
						
						iNeighbourhoodUndoCount = 0;
						bNeighbourhoodShown = false;
			    		return;
			    	}
			    	
	//				if (event.getClickCount() == 2)
	//				{	
			    		// Check if cell has an user object attached
					if (clickedCell.getUserObject() == null)
					{
						super.mousePressed(event);
						return;						
					}
	
					if (!clickedCell.getUserObject().
							getClass().getSimpleName().equals("PathwayVertexRep"))
					{
						super.mousePressed(event);
						return;
					}
					
					final String sUrl = 
						((PathwayVertexRep)clickedCell.getUserObject()).
							getVertex().getVertexLink();
					
					if (sUrl == "") 
					{
						super.mousePressed(event);
						return;
					}
					
					String sSearchPattern = "pathway/map/";
					String sPathwayFilePath;
					
					// Check if clicked cell is another pathway
					if (sUrl.contains((CharSequence)sSearchPattern))
					{
						int iFilePathStartIndex = sUrl.lastIndexOf(sSearchPattern) + sSearchPattern.length();
						sPathwayFilePath = sUrl.substring(iFilePathStartIndex);
						sPathwayFilePath = sPathwayFilePath.replaceFirst("html", "xml");
						System.out.println("Load pathway from " +sPathwayFilePath);
						
						// Extract pathway clicked pathway ID
						int iPathwayIdIndex = sUrl.lastIndexOf("map00") + 5;
						System.out.println("Last index: " +iPathwayIdIndex);
						iPathwayId = StringConversionTool.
							convertStringToInt(sUrl.substring(iPathwayIdIndex, iPathwayIdIndex+3), 0);
	
						refGeneralManager.getSingelton().getLoggerManager().logMsg(
								"Load pathway with ID " +iPathwayId);
						
						// Load pathway
						loadPathwayFromFile("data/XML/pathways/" + sPathwayFilePath);	
					
						bNeighbourhoodShown = false;
					}
					else
					{
						loadNodeInformationInBrowser(sUrl);
	
						// UNDO old neighborhood visualization
						if (bNeighbourhoodShown == true)
						{
							for (int iUndoCount = 0; iUndoCount < iNeighbourhoodUndoCount; iUndoCount++)
							{
								refUndoManager.undo(refGraphLayoutCache);
							}
							iNeighbourhoodUndoCount = 0;
							bNeighbourhoodShown = false;
						}
						
						// Highlight current cell
						highlightCell(clickedCell, Color.RED);
						createSelectionSet(new int[]{((PathwayVertexRep)clickedCell.getUserObject()).
								getVertex().getElementId()});
						
						bNeighbourhoodShown = true;
						iNeighbourhoodUndoCount++;
	
						if (iNeighbourhoodDistance != 0)
						{
							showNeighbourhood(clickedCell, 
								iNeighbourhoodDistance, null);
					
							bNeighbourhoodShown = true;
						}
					}// if(sUrl.contains((CharSequence)sSearchPattern))
				}// if(refCurrentPathway != 0) 
		    	else if (refCurrentPathwayImageMap != null)
		    	{
		    		String sLink = refCurrentPathwayImageMap.processPoint(
		    				new Point(event.getX(), event.getY()));
		    		
		    		if (sLink == null || sLink.equals(""))
		    		{
		    			refGeneralManager.getSingelton().getLoggerManager().logMsg(
		    					this.getClass().getSimpleName() +
		    					":mousePressed(): No pathway link is available for that clicked point. Click ignored.");

		    			return;
		    		}
		    		
		    		iPathwayLevel++;
		    		if (iPathwayLevel >= 3)
		    		{
		    			//fScalingFactor = SCALING_FACTOR;
		    			bBackgroundOverlaySet = true;
		    			loadPathwayFromFile(sLink);
		    		}	
		    		else
		    		{
		    			loadImageMapFromFile(sLink);
		    		}
		    	}
		    		
				//super.mousePressed(event);
		    }
		}
		
		refGraphModel = new DefaultGraphModel();
		
		refGraphLayoutCache = 
			new GraphLayoutCache(refGraphModel, new GPCellViewFactory(),
					true);

		refPathwayGraph = new JGraph(refGraphModel, refGraphLayoutCache);
		
//		// Set own cell view factory
//		refPathwayGraph.getGraphLayoutCache().setFactory(
//				new GPCellViewFactory());

		// Control-drag should clone selection
		refPathwayGraph.setCloneable(true);
		
		// Turn on anti-aliasing
		//refPathwayGraph.setAntiAliased(true);
		
		refPathwayGraph.setMarqueeHandler(new PathwayMarqueeHandler());
		
		// Create and register Undo Manager
		refUndoManager = new GraphUndoManager();
		refGraphModel.addUndoableEditListener(refUndoManager);
	}
	
	public void drawView() {
		
		super.drawView();
		
        // Check if graph is already added to the frame
        if (bGraphSet == false)
        {
            final Dimension dimOverviewMap = new Dimension(200, 200);
            final Dimension dimPathway = new Dimension(iWidth, iHeight);

        	JScrollPane refScrollPane = new JScrollPane(refPathwayGraph);
        	refScrollPane.setMinimumSize(dimPathway);
        	refScrollPane.setMaximumSize(dimPathway);
        	refScrollPane.setPreferredSize(dimPathway);
        	refScrollPane.setAlignmentX(0.5f);
        	refScrollPane.setAlignmentY(0.5f);
        	refEmbeddedFrame.add(refScrollPane);

    		refOverviewPanel = 
    			new GPOverviewPanel(refPathwayGraph, refScrollPane);
    		
    		//showOverviewMapInNewWindow(dimOverviewMap);
    		
        	bGraphSet = true;
        }
	}
	
	public void createVertex(IPathwayVertexRep vertexRep, boolean bHightlighVertex) {
		
		//create node
		refGraphCell = new DefaultGraphCell(vertexRep);
		
		AttributeMap changedMap = refGraphCell.getAttributes(); 
			
		String sShapeType = vertexRep.getShapeType();
		
		Rectangle2D vertexRect = new Rectangle2D.Double(
				(int)((vertexRep.getXPosition() - (vertexRep.getWidth() / 2))  * fScalingFactor), 
				(int)((vertexRep.getYPosition() - (vertexRep.getHeight() / 2))  * fScalingFactor), 
				vertexRep.getWidth(), vertexRep.getHeight());
		
		if (sShapeType.equals("roundrectangle"))
		{				
			// Set vertex type to round rect
			GPCellViewFactory.setViewClass(
					refGraphCell.getAttributes(), 
					"cerberus.view.gui.swt.pathway.jgraph.JGraphMultilineView");
//					"cerberus.view.gui.swt.pathway.jgraph.JGraphRoundRectView");
 
			GraphConstants.setBackground(changedMap, Color.MAGENTA);
		}
		else if (sShapeType.equals("circle"))
		{	
			// Set vertex type to ellipse
			GPCellViewFactory.setViewClass(
					refGraphCell.getAttributes(), 
					"cerberus.view.gui.swt.pathway.jgraph.JGraphEllipseView");

			if (!bBackgroundOverlaySet)
			{	
				GraphConstants.setAutoSize(changedMap, true);
			}

			GraphConstants.setBackground(changedMap, Color.green);
		}	
		else if (sShapeType.equals("rectangle"))
		{	
			GraphConstants.setBackground(changedMap, new Color(0.53f, 0.81f, 1.0f)); // ligth blue
		}

		GraphConstants.setBounds(changedMap, vertexRect);
		
		// Some global attributes
		GraphConstants.setOpaque(changedMap, true);
		GraphConstants.setSelectable(changedMap, false);
		GraphConstants.setFont(changedMap, new Font("Arial", Font.BOLD, 11));
		//GraphConstants.setAutoSize(refGraphCell.getAttributes(), true);
		
		vecVertices.add(refGraphCell);
		
		vertexIdToCellLUT.put(
				vertexRep.getVertex().getElementId(), refGraphCell);
	}
	
	public void createEdge(int iVertexId1, 
			int iVertexId2, 
			boolean bDrawArrow,
			APathwayEdge refPathwayEdge) {
		
		DefaultPort port1 = new DefaultPort();
		DefaultGraphCell cell1 = vertexIdToCellLUT.get(iVertexId1);
		cell1.add(port1);
		
		DefaultPort port2 = new DefaultPort();
		DefaultGraphCell cell2 = vertexIdToCellLUT.get(iVertexId2);
		cell2.add(port2);
	
		DefaultEdge edge = new DefaultEdge(refPathwayEdge);
		edge.setSource(cell1.getChildAt(0));
		edge.setTarget(cell2.getChildAt(0));
		
		// Retrieve existing edges between nodes
		Object[] existingEdges  = 
			DefaultGraphModel.getEdgesBetween(
					refGraphModel, edge.getSource() , edge.getTarget() ,false); 
		
		// Return if edge of same type between two nodes already exists
		for (int iEdgeCount = 0; iEdgeCount < existingEdges.length; iEdgeCount++)
		{
			if (((APathwayEdge)((DefaultEdge)existingEdges[iEdgeCount]).
					getUserObject()).getEdgeType() == refPathwayEdge.getEdgeType())
			{
				return;
			}
		}
		
		AttributeMap changedMap = edge.getAttributes(); 	
		EdgeLineStyle edgeLineStyle = null;
		EdgeArrowHeadStyle edgeArrowHeadStyle = null;
		Color edgeColor = null;
	    
		GraphConstants.setLineWidth(changedMap, 2);
		GraphConstants.setSelectable(changedMap, false);
//		GraphConstants.setRouting(changedMap, JGraphParallelRouter.getSharedInstance());
//		GraphConstants.setRouting(edge.getAttributes(), GraphConstants.ROUTING_SIMPLE);
		
		// Differentiate between Relations and Reactions
		if (refPathwayEdge.getEdgeType() == EdgeType.REACTION)
		{
			edgeLineStyle = refRenderStyle.getReactionEdgeLineStyle();
			edgeArrowHeadStyle = refRenderStyle.getReactionEdgeArrowHeadStyle();
			edgeColor = refRenderStyle.getReactionEdgeColor();
			
			GraphConstants.setLineColor(changedMap, edgeColor);
			
			vecReactionEdges.add(edge);
		}
		else if (refPathwayEdge.getEdgeType() == EdgeType.RELATION)
		{
			// In case when relations are maplinks
			if (((PathwayRelationEdge)refPathwayEdge).getEdgeRelationType() 
					== EdgeRelationType.maplink)
			{
				edgeLineStyle = refRenderStyle.getMaplinkEdgeLineStyle();
				edgeArrowHeadStyle = refRenderStyle.getMaplinkEdgeArrowHeadStyle();
				edgeColor = refRenderStyle.getMaplinkEdgeColor();
			}
			else 
			{
				edgeLineStyle = refRenderStyle.getRelationEdgeLineStyle();
				edgeArrowHeadStyle = refRenderStyle.getRelationEdgeArrowHeadStyle();
				edgeColor = refRenderStyle.getRelationEdgeColor();
			}
			
			GraphConstants.setLineColor(changedMap, edgeColor);
			
			vecRelationEdges.add(edge);
			
		}// (refPathwayEdge.getEdgeType() == EdgeType.RELATION)
		
		// Assign render style
	    if (edgeLineStyle == EdgeLineStyle.DASHED)
	    {
	    	GraphConstants.setDashPattern(changedMap, new float[]{4,4});
	    }
	    	
		// Draw arrow
		if (bDrawArrow == true)
		{
			GraphConstants.setLineEnd(
					edge.getAttributes(), GraphConstants.ARROW_TECHNICAL);
		}
	    
	    if (edgeArrowHeadStyle == EdgeArrowHeadStyle.FILLED)
	    {
			GraphConstants.setEndFill(changedMap, true);
	    }
		else if (edgeArrowHeadStyle == EdgeArrowHeadStyle.EMPTY)
		{
			GraphConstants.setEndFill(changedMap, false);
		}
		
		refPathwayGraph.getGraphLayoutCache().insert(edge);
	}
	
	public void finishGraphBuilding() {
		
		refPathwayGraph.getGraphLayoutCache().insert(
				vecVertices.toArray());
//		refPathwayGraph.getGraphLayoutCache().insert(
//				vecRelationEdges.toArray());
//		refPathwayGraph.getGraphLayoutCache().insert(
//				vecReactionEdges.toArray());
	}
	
	public void setPathwayId(int iPathwayId) {
		
		this.iPathwayId = iPathwayId;
	}
	
	public void loadPathwayFromFile(String sFilePath) {
		
		super.loadPathwayFromFile(sFilePath);
		
		resetPathway();
	
		iPathwayId = Integer.parseInt(
				sFilePath.substring(sFilePath.lastIndexOf('/')+4, 
						sFilePath.lastIndexOf('/')+9));
		
		drawView();
		
		refPathwayGraph.setBackgroundImage(null);
		
		if (bBackgroundOverlaySet == true)
		{
			// Build current pathway file path of GIF
			String sPathwayImageFilePath = refCurrentPathway.getTitle();
			sPathwayImageFilePath = sPathwayImageFilePath.substring(5);
			sPathwayImageFilePath = "data/images/pathways/" 
				+sPathwayImageFilePath +".gif";
			
			loadBackgroundOverlayImage(sPathwayImageFilePath);
		}
		
		refGraphLayoutCache.reload();
	}
	
	public void loadImageMapFromFile(String sImageMapPath) {
		
		resetPathway();
		
		refGeneralManager.getSingelton().
			getXmlParserManager().parseXmlFileByName(sImageMapPath);
		
		refCurrentPathwayImageMap = 
			refGeneralManager.getSingelton().getPathwayManager().getCurrentPathwayImageMap();
		
		loadBackgroundOverlayImage(refCurrentPathwayImageMap.getImageLink());
	}
	
	public void zoomOrig() {
		refPathwayGraph.setScale(1.0);
	}
	
	public void zoomIn() {
		
		refPathwayGraph.setScale(1.2 * refPathwayGraph.getScale());
	}
	
	public void zoomOut() {
		
		refPathwayGraph.setScale(refPathwayGraph.getScale() / 1.2);
	}
	
	/**
	 * Method visualizes the neighborhood of a certain cell.
	 * 
	 * @param cell Cell around the neighborhood is shown.
	 * @param iDistance Neighborhood distance.
	 * @param parentCell The cell that should be excluded. This is needed 
	 * because the method is called recursive.
	 */
	protected void showNeighbourhood(DefaultGraphCell cell, 
			int iDistance, DefaultGraphCell parentCell) {
		
		HashSet<DefaultGraphCell> excludeParent = 
			new HashSet<DefaultGraphCell>();
		excludeParent.add(parentCell);

		List<DefaultGraphCell> neighbourCells = 
			refGraphLayoutCache.getNeighbours(cell, 
					excludeParent, false, false);
	
		Iterator<DefaultGraphCell> cellIter = neighbourCells.iterator();
		
		Map<DefaultGraphCell, Map> nested = 
			new Hashtable<DefaultGraphCell, Map>();
		Map attributeMap = new Hashtable();
		
//		GraphConstants.setBackground(cell.getAttributes(), 
//				new Color(0.7f, 0.133f, 0.133f));
		
		DefaultGraphCell tmpCell;
		
		// Color mapping will start with red (neigborhood = 1)
		// For graph parts far away the color will turn to yellow.
		float fGreenPortion = fGreenPortion = 1.0f - (iDistance / 10.0f * 3.0f) + 0.3f;
		
		if (fGreenPortion < 0.0)
			fGreenPortion = 0.0f;
		else if (fGreenPortion > 1.0f)
			fGreenPortion = 1.0f;
		
		GraphConstants.setBackground(
				attributeMap, new Color(1.0f, fGreenPortion, 0.0f));
		
		while (cellIter.hasNext())
		{
			tmpCell = cellIter.next();

			nested.put(tmpCell, attributeMap);
			
			for (int iDistanceCount = 1; iDistanceCount < iDistance; iDistanceCount++)
			{
				showNeighbourhood(tmpCell, iDistance-1, cell);
			}
		}
		
		refGraphLayoutCache.edit(nested, null, null, null);
		
		iNeighbourhoodUndoCount++;
		
		//createSelectionSet();
	}
	
	public void highlightCell(final DefaultGraphCell refCell, 
			final Color color) {
		
		Map<DefaultGraphCell, Map> nested = 
			new Hashtable<DefaultGraphCell, Map>();
		Map attributeMap = new Hashtable();
		
		GraphConstants.setBackground(attributeMap, color);
		
		nested.put(refCell, attributeMap);
		refGraphLayoutCache.edit(nested, null, null, null);
	}
	
	/**
	 * Methods puts the overview map in a new JFrame and
	 * displays the frame.
	 * 
	 * @param dim
	 */
	public void showOverviewMapInNewWindow(Dimension dim) {
		
        JFrame wnd = new JFrame();
        wnd.setLocation(800, 500);
        wnd.setSize(dim);
        wnd.setVisible(true);
        
		wnd.add(refOverviewPanel);
	}

	public void setNeighbourhoodDistance(int iNeighbourhoodDistance) {
	
		this.iNeighbourhoodDistance = iNeighbourhoodDistance;
	}
	
	public void showHideEdgesByType(boolean bShowEdges, EdgeType edgeType) {

		if (edgeType == EdgeType.REACTION)
		{
			refGraphLayoutCache.setVisible(
					vecRelationEdges.toArray(), bShowEdges);			
		}
		else if (edgeType == EdgeType.RELATION)
		{
			refGraphLayoutCache.setVisible(
					vecReactionEdges.toArray(), bShowEdges);
		}	
	}
	
	public void showBackgroundOverlay(boolean bTurnOn) {
		
		if (bTurnOn == true)
		{		
			bBackgroundOverlaySet = true;
			
			// Build current pathway file path of GIF
			String sPathwayImageFilePath = refCurrentPathway.getTitle();
			sPathwayImageFilePath = sPathwayImageFilePath.substring(5);
			sPathwayImageFilePath = "data/images/pathways/" +sPathwayImageFilePath +".gif";

			refGeneralManager.getSingelton().getLoggerManager().logMsg(
					"Load background pathway from file: " +sPathwayImageFilePath);
			
			// Set background image
			refPathwayGraph.setBackgroundImage(
					new ImageIcon(sPathwayImageFilePath));

			// Set scaling factor so that background image is an direct overlay
			fScalingFactor = 1.0f;
		}
		else
		{
			bBackgroundOverlaySet = false;
			refPathwayGraph.setBackgroundImage(null);
			fScalingFactor = SCALING_FACTOR;
		}
					
		resetPathway();
		
		// Attention: Performance problem.
		drawView();
		
		// Set edges to visible
		refGraphLayoutCache.setVisible(
				vecReactionEdges.toArray(), !bTurnOn);
		refGraphLayoutCache.setVisible(
				vecRelationEdges.toArray(), !bTurnOn);
	}
	
	public void resetPathway() {
		
		refCurrentPathway = null;
		refCurrentPathwayImageMap = null;
		
		refGraphModel = new DefaultGraphModel();
		refPathwayGraph.setModel(refGraphModel);
		refGraphLayoutCache.setModel(refGraphModel);
		refGraphModel.addUndoableEditListener(refUndoManager);
		
		vecVertices.removeAllElements();
		vecRelationEdges.removeAllElements();
		vecReactionEdges.removeAllElements();
		
		iNeighbourhoodUndoCount = 0;
		bNeighbourhoodShown = false;
	}
	
	public void loadBackgroundOverlayImage(String sPathwayImageFilePath) {
		
		refGeneralManager.getSingelton().getLoggerManager().logMsg(
				"Load background pathway image from file: " 
				+sPathwayImageFilePath);
		
		// Set background image
		refPathwayGraph.setBackgroundImage(
				new ImageIcon(sPathwayImageFilePath));

		// Set scaling factor so that background image is an direct overlay
		fScalingFactor = 1.0f;
		
		// Set edges to visible
		refGraphLayoutCache.setVisible(
				vecReactionEdges.toArray(), false);
		refGraphLayoutCache.setVisible(
				vecRelationEdges.toArray(), false);
	}
}
