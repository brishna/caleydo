package org.geneview.core.view.opengl.canvas.pathway;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL;

import org.geneview.core.command.CommandQueueSaxType;
import org.geneview.core.command.view.swt.CmdViewLoadURLInHTMLBrowser;
import org.geneview.core.data.collection.ISet;
import org.geneview.core.data.collection.set.selection.ISetSelection;
import org.geneview.core.data.graph.core.PathwayGraph;
import org.geneview.core.data.graph.item.vertex.PathwayVertexGraphItem;
import org.geneview.core.data.graph.item.vertex.PathwayVertexGraphItemRep;
import org.geneview.core.data.mapping.EGenomeMappingType;
import org.geneview.core.data.view.rep.selection.SelectedElementRep;
import org.geneview.core.manager.IGeneralManager;
import org.geneview.core.manager.ILoggerManager.LoggerType;
import org.geneview.core.manager.data.IPathwayManager;
import org.geneview.core.manager.data.pathway.EPathwayDatabaseType;
import org.geneview.core.manager.event.mediator.IMediatorReceiver;
import org.geneview.core.manager.event.mediator.IMediatorSender;
import org.geneview.core.manager.view.ESelectionMode;
import org.geneview.core.manager.view.Pick;
import org.geneview.core.manager.view.SelectionManager;
import org.geneview.core.view.opengl.canvas.AGLCanvasUser;
import org.geneview.core.view.opengl.util.GLInfoAreaRenderer;
import org.geneview.util.graph.EGraphItemHierarchy;
import org.geneview.util.graph.EGraphItemProperty;
import org.geneview.util.graph.IGraph;
import org.geneview.util.graph.IGraphItem;

/**
 * Single OpenGL pathway view
 * 
 * @author Marc Streit
 */
public class GLCanvasPathway3D 
extends AGLCanvasUser
implements IMediatorReceiver, IMediatorSender {
	
	private int iPathwayID = -1;
	
	private boolean bIsDisplayListDirtyLocal = true;
	private boolean bIsDisplayListDirtyRemote = true;
	
	private boolean bIsMouseOverPickingEvent = false;
	private boolean bEnablePathwayTexture = true;
	private boolean bSelectionChanged = false;
	private boolean bUpdateReceived = false;

	private int iMouseOverPickedPathwayId = -1;
	
	private int iGLDisplayListIndex = -1;

	private IPathwayManager pathwayManager;
	
	private GLPathwayManager refGLPathwayManager;

	private SelectionManager selectionManager;

	private PathwayVertexGraphItemRep selectedVertex;

	private GLInfoAreaRenderer infoAreaRenderer;

	/**
	 * Hash map stores which pathways contain the currently selected vertex and
	 * how often this vertex is contained.
	 */
	private HashMap<Integer, Integer> refHashPathwayContainingSelectedVertex2VertexCount;
	
	/**
	 * Own texture manager is needed for each GL context, 
	 * because textures cannot be bound to multiple GL contexts.
	 */
	private HashMap<GL, GLPathwayTextureManager> refHashGLcontext2TextureManager;
	
	private Vec3f vecScaling;
	private Vec3f vecTranslation;
	
	/**
	 * Constructor.
	 * 
	 */
	public GLCanvasPathway3D(final IGeneralManager generalManager,
			int iViewID,
			int iGLCanvasID,
			String sLabel) {

		super(generalManager, iViewID, iGLCanvasID, sLabel);
		
		pathwayManager = generalManager.getSingelton().getPathwayManager();
		
		refGLPathwayManager = new GLPathwayManager(generalManager);
		refHashGLcontext2TextureManager = new HashMap<GL, GLPathwayTextureManager>();
		refHashPathwayContainingSelectedVertex2VertexCount = new HashMap<Integer, Integer>();

		infoAreaRenderer = new GLInfoAreaRenderer(generalManager,
				refGLPathwayManager);
		
		infoAreaRenderer.enableColorMappingArea(true);	
		
		selectionManager = generalManager.getSingelton().getViewGLCanvasManager().getSelectionManager();
	
		vecScaling = new Vec3f(1,1,1);
		vecTranslation = new Vec3f(0,0,0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#initLocal(javax.media.opengl.GL)
	 */	
	public void initLocal(final GL gl)
	{
		init(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#initRemote(javax.media.opengl.GL)
	 */
	public void initRemote(final GL gl)
	{
		init(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#init(javax.media.opengl.GL)
	 */
	public void init(final GL gl) {

		initPathwayData(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#displayLocal(javax.media.opengl.GL)
	 */
	public void displayLocal(final GL gl) {
		
		pickingManager.handlePicking(this, gl, pickingTriggerMouseAdapter, false);
		if(bIsDisplayListDirtyLocal)
		{
			refGLPathwayManager.buildPathwayDisplayList(gl, this, iPathwayID);
			bIsDisplayListDirtyLocal = false;			
		}	
		display(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#displayRemote(javax.media.opengl.GL)
	 */
	public void displayRemote(final GL gl) {
		
		if(bIsDisplayListDirtyRemote)
		{
			refGLPathwayManager.buildPathwayDisplayList(gl, this, iPathwayID);
			bIsDisplayListDirtyRemote = false;
		}	
		display(gl);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#display(javax.media.opengl.GL)
	 */
	public void display(final GL gl) {
		
		checkForHits();
		renderScene(gl);
		renderInfoArea(gl);
	}
	
	protected void initPathwayData(final GL gl) {

		refGLPathwayManager.init(gl, alSetData, alSetSelection);
		
		// Create new pathway manager for GL context
		if(!refHashGLcontext2TextureManager.containsKey(gl))
		{
			refHashGLcontext2TextureManager.put(gl, 
					new GLPathwayTextureManager(generalManager));	
		}		
		
		loadAllPathways(gl);

		refGLPathwayManager.buildPathwayDisplayList(gl, this, iPathwayID);
	}


	public void renderScene(final GL gl) {
		
		renderPathwayById(gl, iPathwayID);
	}


	private void loadAllPathways(final GL gl) {
		
		// Check if pathways are already loaded
		if (pathwayManager.getRootPathway().getAllGraphByType(
				EGraphItemHierarchy.GRAPH_CHILDREN).isEmpty()) 
		{
			// Load KEGG pathways
			pathwayManager.loadAllPathwaysByType(EPathwayDatabaseType.KEGG);

			// Load BioCarta pathways
			pathwayManager.loadAllPathwaysByType(EPathwayDatabaseType.BIOCARTA);
		}
		
//		Iterator<IGraph> iterPathwayGraphs = pathwayManager
//			.getRootPathway().getAllGraphByType(EGraphItemHierarchy.GRAPH_CHILDREN).iterator();
//
//		while(iterPathwayGraphs.hasNext())
//		{
//			iterPathwayGraphs.next();
//			iPathwayID = iterPathwayGraphs.next().getId();
//			break;
//		}
		
		Random rand = new Random();
		
		List<IGraph> tmp = pathwayManager.getRootPathway().getAllGraphByType(EGraphItemHierarchy.GRAPH_CHILDREN);
		iPathwayID = tmp.get(rand.nextInt(500)).getId();
		
		calculatePathwayScaling(gl, iPathwayID);
	}

	private void renderPathwayById(final GL gl,
			final int iPathwayId) {
		
		gl.glPushMatrix();
		gl.glTranslatef(-vecTranslation.x(), -vecTranslation.y(), -vecTranslation.z());
		gl.glScalef(vecScaling.x(), vecScaling.y(), vecScaling.z());
		
		if (bEnablePathwayTexture)
		{
			refHashGLcontext2TextureManager.get(gl).renderPathway(gl, iPathwayId, 1.0f, false);
		}

		float tmp = GLPathwayManager.SCALING_FACTOR_Y * 
			((PathwayGraph)pathwayManager.getItem(iPathwayId)).getHeight();
		
		// Pathway texture height is subtracted from Y to align pathways to
		// front level
		gl.glTranslatef(0, tmp, 0);
		refGLPathwayManager.renderPathway(gl, iPathwayId, false);
		gl.glTranslatef(0, -tmp, 0);
		
		gl.glScalef(1/vecScaling.x(), 1/vecScaling.y(),1/ vecScaling.z());
		gl.glTranslatef(vecTranslation.x(), vecTranslation.y(), vecTranslation.z());
		
		gl.glPopMatrix();
		
	}
	
	private void renderInfoArea(final GL gl) {

		if (selectedVertex != null && infoAreaRenderer.isPositionValid())
		{
			infoAreaRenderer.renderInfoArea(gl, selectedVertex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geneview.core.manager.event.mediator.IMediatorReceiver#updateReceiver(java.lang.Object,
	 *      org.geneview.core.data.collection.ISet)
	 */
	public void updateReceiver(Object eventTrigger, ISet updatedSet) {
		
		generalManager.getSingelton().logMsg(
				this.getClass().getSimpleName()
						+ ": updateReceiver(Object eventTrigger, ISet updatedSet): Update called by "
						+ eventTrigger.getClass().getSimpleName(),
				LoggerType.VERBOSE);
		
		ISetSelection refSetSelection = (ISetSelection) updatedSet;

		refSetSelection.getReadToken();
		int[] iArSelection = refSetSelection.getSelectionIdArray();
		if (iArSelection.length != 0)
		{
			int iAccessionID = iArSelection[0];
			
			String sAccessionCode = generalManager.getSingelton().getGenomeIdManager()
				.getIdStringFromIntByMapping(iAccessionID, EGenomeMappingType.ACCESSION_2_ACCESSION_CODE);
		
			System.out.println("Accession Code: " +sAccessionCode);
			
//			int iPathwayHeight = ((PathwayGraph)generalManager.getSingelton().getPathwayManager().getItem(iPathwayID)).getHeight();
			
			selectionManager.modifySelection(iAccessionID, new SelectedElementRep(this.getId(), 
					0, 
					0), 
					ESelectionMode.AddPick);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.geneview.core.view.opengl.canvas.AGLCanvasUser#updateReceiver(java.lang.Object)
	 */
	public void updateReceiver(Object eventTrigger) {

		generalManager.getSingelton().logMsg(
				this.getClass().getSimpleName()
						+ ": updateReceiver(Object eventTrigger): Update called by "
						+ eventTrigger.getClass().getSimpleName(),
				LoggerType.VERBOSE);
	}
	
	protected void checkForHits()
	{
		if(pickingManager.getHits(this, GLPathwayManager.PATHWAY_SELECTION) != null)
		{
			ArrayList<Pick> tempList = pickingManager.getHits(this, GLPathwayManager.PATHWAY_SELECTION);
			
			if (tempList != null)
			{
				if (tempList.size() != 0 )
				{
					int iElementID = pickingManager.getExternalIDFromPickingID(this, tempList.get(0).getPickingID());
				
					PathwayVertexGraphItemRep tmpVertexGraphItemRep = (PathwayVertexGraphItemRep) generalManager.getSingelton()
						.getPathwayItemManager().getItem(iElementID);
					
					PathwayVertexGraphItem tmpVertexGraphItem = (PathwayVertexGraphItem) tmpVertexGraphItemRep
						.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).get(0);
					
					int iGeneID = generalManager.getSingelton().getGenomeIdManager()
						.getIdIntFromStringByMapping(
								tmpVertexGraphItem.getName().substring(4), 
								EGenomeMappingType.NCBI_GENEID_CODE_2_NCBI_GENEID);
							
					if (iGeneID == -1)
					{	
						return;
					}
					
					int iAccessionID = generalManager.getSingelton().getGenomeIdManager()
						.getIdIntFromIntByMapping(iGeneID, EGenomeMappingType.NCBI_GENEID_2_ACCESSION);
					
//					// Just for testing
//					String sAccessionCode = "NM_001565";					
//					int iAccessionID = generalManager.getSingelton().getGenomeIdManager()
//						.getIdIntFromStringByMapping(sAccessionCode, EGenomeMappingType.ACCESSION_CODE_2_ACCESSION);

					selectionManager.clear();

					int iPathwayHeight = ((PathwayGraph)generalManager.getSingelton().getPathwayManager().getItem(iPathwayID)).getHeight();
					
					selectionManager.modifySelection(iAccessionID, new SelectedElementRep(this.getId(), 
							tmpVertexGraphItemRep.getXOrigin() * GLPathwayManager.SCALING_FACTOR_X, 
							(iPathwayHeight - tmpVertexGraphItemRep.getYOrigin()) * GLPathwayManager.SCALING_FACTOR_Y), 
							ESelectionMode.AddPick);

					// Trigger update

					// Write currently selected vertex to selection set
					int[] iArTmpSelectionId = new int[1];
					int[] iArTmpDepth = new int[1];
					iArTmpSelectionId[0] = iAccessionID;
					iArTmpDepth[0] = 0;
					alSetSelection.get(0).getWriteToken();
					alSetSelection.get(0).updateSelectionSet(iUniqueId, iArTmpSelectionId, iArTmpDepth, new int[0]);
					alSetSelection.get(0).returnWriteToken();
				}
			}
			
			pickingManager.flushHits(this, GLPathwayManager.PATHWAY_SELECTION);
		}
	}

	private void loadPathwayToUnderInteractionPosition(final int iPathwayId) {
		
		loadNodeInformationInBrowser(((PathwayGraph)pathwayManager
				.getItem(iPathwayId)).getExternalLink());

		// Check if selected pathway is loaded.
		if (!pathwayManager.hasItem(iPathwayId))
		{
			return;
		}

		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;

		// Trigger update with current pathway that dependent pathways
		// know which pathway is currently under interaction
		int[] iArOptional = new int[1];
		iArOptional[0] = iPathwayId;
		alSetSelection.get(0).updateSelectionSet(iUniqueId, new int[0],
				new int[0], iArOptional);
	}
	
//	public void loadDependentPathwayBySingleVertex(final GL gl,
//			final PathwayVertexGraphItemRep vertex) {
//		
//		Iterator<IGraphItem> iterVertexGraphItems = 
//			vertex.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).iterator();
//
//		ArrayList<IGraphItem> alSelectedVertexGraphItemReps = 
//			new ArrayList<IGraphItem>();
//		
//		// Remove duplicates by adding to a hash list
//		HashSet<IGraphItem> set = new HashSet<IGraphItem>();
//		
//		while(iterVertexGraphItems.hasNext())
//		{
//			set.addAll(iterVertexGraphItems.next().getAllItemsByProp(
//					EGraphItemProperty.ALIAS_CHILD));
//		}
//		
//		alSelectedVertexGraphItemReps.addAll(set);
//				
//		loadDependentPathways(gl, alSelectedVertexGraphItemReps);
//	}
//		
//	public void loadDependentPathways(final GL gl,
//			final List<IGraphItem> alVertexRep) {
//
//		refHashPathwayContainingSelectedVertex2VertexCount.clear();
//		
//		Iterator<IGraphItem> iterIdenticalPathwayGraphItemReps = 
//			alVertexRep.iterator();
//		
//		IGraphItem identicalPathwayGraphItemRep;
//		int iPathwayId = 0;
//		int iMaxPathwayCount = 0;
//		
//		while (iterIdenticalPathwayGraphItemReps.hasNext())
//		{
//			identicalPathwayGraphItemRep = iterIdenticalPathwayGraphItemReps.next();
//	
//			iPathwayId = ((PathwayGraph)identicalPathwayGraphItemRep
//					.getAllGraphByType(EGraphItemHierarchy.GRAPH_PARENT).toArray()[0]).getKeggId();
//
//
//			// Check if pathway has already a vertex counted
//			if (refHashPathwayContainingSelectedVertex2VertexCount.containsKey(iPathwayId))
//			{
//				// Increase current stored identical vertex count by 1
//				refHashPathwayContainingSelectedVertex2VertexCount.put(
//						iPathwayId, refHashPathwayContainingSelectedVertex2VertexCount.get(iPathwayId) + 1);
//			}
//			else
//			{
//				refHashPathwayContainingSelectedVertex2VertexCount.put(iPathwayId, 1);
//			}
//		}
//		
//		bIsDisplayListDirtyLocal = true;
//		bIsDisplayListDirtyRemote = true;
//	}
//
//	private void rebuildPathwayDisplayList(final GL gl) {
//
////		// Reset rebuild trigger flag
////		bRebuildVisiblePathwayDisplayLists = false;
////		
////		refGLPathwayManager.clearOldPickingIDs();
////
////		if (selectedVertex != null)
////		{
////			// Write currently selected vertex to selection set
////			int[] iArTmpSelectionId = new int[1];
////			int[] iArTmpDepth = new int[1];
////			iArTmpSelectionId[0] = selectedVertex.getId();
////			iArTmpDepth[0] = 0;
////			alSetSelection.get(0).getWriteToken();
////			alSetSelection.get(0).updateSelectionSet(iUniqueId, iArTmpSelectionId, iArTmpDepth, new int[0]);
////			alSetSelection.get(0).returnWriteToken();
////		}
////			
////		refGLPathwayManager.performIdenticalNodeHighlighting();
////		
////
////		refGLPathwayManager.buildPathwayDisplayList(gl, this, 261);
////		
////		// Cleanup unused textures
//////		refGLPathwayTextureManager.unloadUnusedTextures(getVisiblePathways());
//
//	}
	
	private void calculatePathwayScaling(final GL gl, final int iPathwayId) {
		
		if (refHashGLcontext2TextureManager.get(gl) == null)
			return;
		
		int iImageWidth = ((PathwayGraph)generalManager.getSingelton()
				.getPathwayManager().getItem(iPathwayId)).getWidth();
		int iImageHeight = ((PathwayGraph)generalManager.getSingelton()
				.getPathwayManager().getItem(iPathwayId)).getHeight();
	
		float fAspectRatio = (float)iImageWidth / (float)iImageHeight;

		float fTmpPathwayWidth = iImageWidth * GLPathwayManager.SCALING_FACTOR_X * 3.2f;
		float fTmpPathwayHeight = iImageHeight * GLPathwayManager.SCALING_FACTOR_Y * 3.2f;
		
		if (fTmpPathwayWidth > (viewFrustum.getRight() - viewFrustum.getLeft()))
		{			
			vecScaling.setX((viewFrustum.getRight() - viewFrustum.getLeft()) / (iImageWidth * GLPathwayManager.SCALING_FACTOR_X));
			vecScaling.setY(vecScaling.x());

			vecTranslation.set((viewFrustum.getRight() - viewFrustum.getLeft()) / 2.0f,
					(viewFrustum.getRight() - viewFrustum.getLeft()) / 2.0f * 1/fAspectRatio, 0);			
		}
		else if (fTmpPathwayHeight > (viewFrustum.getTop() - viewFrustum.getBottom()))
		{
			vecScaling.setY((viewFrustum.getTop() - viewFrustum.getBottom()) / (iImageHeight * GLPathwayManager.SCALING_FACTOR_Y));
			vecScaling.setX(vecScaling.y());

			vecTranslation.set((viewFrustum.getTop() - viewFrustum.getBottom()) / 2.0f * fAspectRatio,
					(viewFrustum.getTop() - viewFrustum.getBottom()) / 2.0f, 0);
		}
		else
		{
			vecScaling.set(3.2f, 3.2f, 3.2f);
			
			vecTranslation.set(fTmpPathwayWidth / 2.0f,
					fTmpPathwayHeight / 2.0f, 0);
		}
	}

	public void loadNodeInformationInBrowser(String sUrl) {

		if (sUrl.isEmpty())
			return;

		CmdViewLoadURLInHTMLBrowser createdCmd = (CmdViewLoadURLInHTMLBrowser) generalManager
				.getSingelton().getCommandManager().createCommandByType(
						CommandQueueSaxType.LOAD_URL_IN_BROWSER);

		createdCmd.setAttributes(sUrl);
		createdCmd.doCommand();
	}
	
	public void setMappingRowCount(final int iMappingRowCount) {
		
		refGLPathwayManager.setMappingRowCount(iMappingRowCount);
	}
		
	public void enableGeneMapping(final boolean bEnableMapping) {
		
		refGLPathwayManager.enableGeneMapping(bEnableMapping);
		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;
	}
	
	public void enablePathwayTextures(final boolean bEnablePathwayTexture) {
		
		refGLPathwayManager.enableEdgeRendering(!bEnablePathwayTexture);
		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;
		
		this.bEnablePathwayTexture = bEnablePathwayTexture;
	}
	
	public void enableNeighborhood(final boolean bEnableNeighborhood) {
		
		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;
		refGLPathwayManager.enableNeighborhood(bEnableNeighborhood);
	}
	
	public void enableIdenticalNodeHighlighting(final boolean bEnableIdenticalNodeHighlighting) {
		
		bIsDisplayListDirtyLocal = true;
		bIsDisplayListDirtyRemote = true;
		refGLPathwayManager.enableIdenticalNodeHighlighting(bEnableIdenticalNodeHighlighting);
	}
	
	public void enableAnnotation(final boolean bEnableAnnotation) {
		
		refGLPathwayManager.enableAnnotation(bEnableAnnotation);
	}
}