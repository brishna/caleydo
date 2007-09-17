package cerberus.view.opengl.util;

import java.awt.Font;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.geneview.graph.EGraphItemProperty;
import org.geneview.graph.IGraphItem;

import cerberus.data.graph.item.vertex.EPathwayVertexType;
import cerberus.data.graph.item.vertex.PathwayVertexGraphItem;
import cerberus.data.graph.item.vertex.PathwayVertexGraphItemRep;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle;
import cerberus.manager.IGeneralManager;
import cerberus.util.mapping.GeneAnnotationMapper;
import cerberus.view.opengl.canvas.pathway.GLPathwayManager;

import com.sun.opengl.util.j2d.TextRenderer;

public class GLInfoAreaRenderer {
	
	private float[] fArWorldCoordinatePosition;
	
	private float fScaleFactor = 0.0f;

	private GLStarEffectRenderer starEffectRenderer;
	
	private float fZValue = 1f;
	
	private GLPathwayManager refGLPathwayManager;
		
	private LinkedList<PathwayVertexGraphItem> llMultipleMappingGenes;
	
	private GeneAnnotationMapper geneAnnotationMapper;
	
	private float fHeight = 0.4f;
	private float fWidth = 1.0f;
	
	private TextRenderer textRenderer;
	
	private PathwayRenderStyle refRenderStyle;
	
	private boolean bEnableColorMapping = false;
	
	public GLInfoAreaRenderer(final IGeneralManager refGeneralManager,
			final GLPathwayManager refGLPathwayManager) {
		
		fArWorldCoordinatePosition = new float[3];
		starEffectRenderer = new GLStarEffectRenderer();		
		
		this.refGLPathwayManager = refGLPathwayManager;
		
		llMultipleMappingGenes = new LinkedList<PathwayVertexGraphItem>();

		geneAnnotationMapper = 
			new GeneAnnotationMapper(refGeneralManager);
		
		textRenderer = new TextRenderer(new Font("Arial",
				Font.BOLD, 16), false);
		
		refRenderStyle = new PathwayRenderStyle();
	}
	
    
    public void renderInfoArea(final GL gl,
    		final PathwayVertexGraphItemRep pickedVertexRep) {
    	
    	if (fScaleFactor < 1.0)
    		fScaleFactor += 0.1f;
    	
    	extractMultipleGeneMapping(pickedVertexRep);
    	
    	// Check if vertex has multiple mapping and draw info areas in star formation
    	if (pickedVertexRep.getAllItemsByProp(EGraphItemProperty.ALIAS_PARENT).size() > 1)
    	{
    		drawPickedObjectInfoStar(gl);
    	}
    	// In case of single mapping draw single info area
    	else
    	{
        	drawPickedObjectInfoSingle(gl, true);
    	}
    }
    
    public void renderInfoArea(final GL gl,
    		final PathwayVertexGraphItem pickedVertex) {
    	
    	if (fScaleFactor < 1.0)
    		fScaleFactor += 0.06;
    	
		llMultipleMappingGenes.clear();
		llMultipleMappingGenes.add(pickedVertex);
		
		drawPickedObjectInfoSingle(gl, true);
    }
	
    private void drawPickedObjectInfoSingle(final GL gl,
			final boolean bDrawDisplaced) {

    	if (fArWorldCoordinatePosition == null)
    		return;
    	
    	gl.glPushMatrix();		
		
		gl.glTranslatef(fArWorldCoordinatePosition[0], fArWorldCoordinatePosition[1], fZValue);
				
    	if (bDrawDisplaced)
    	{   
    		float fOffsetX = 0.5f;
    		float fOffsetY = 0.7f;
    		
    		gl.glScalef(fScaleFactor, fScaleFactor, fScaleFactor);
        	
			gl.glLineWidth(2);
			gl.glColor4f(0.5f, 0.5f, 0.5f, 0.8f);
			gl.glBegin(GL.GL_TRIANGLES);
			gl.glVertex3f(0, 0, -fZValue);
			gl.glVertex3f(fOffsetX, fOffsetY - fHeight, 0);
			gl.glVertex3f(fOffsetX, fOffsetY, 0);
			gl.glEnd();
			
			gl.glColor3f(0.2f, 0.2f, 0.2f);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3f(0, 0, -fZValue);
			gl.glVertex3f(fOffsetX, fOffsetY - fHeight, 0);
			gl.glVertex3f(fOffsetX, fOffsetY, 0);
			gl.glEnd();
			
			gl.glTranslatef(fOffsetX, fOffsetY, 0.0f);
    	}
		
		if (fScaleFactor < 1.0f)
		{
			gl.glPopMatrix();
			return;
		}		
		
		// FIXME: Workflow is not optimal
		// Do composition of info label string only once and store them (heading + text)
		float fMaxWidth = calculateInfoAreaWidth(llMultipleMappingGenes.get(0));
		
		if (fMaxWidth < 1.0f)
			fMaxWidth = 1.0f;

		gl.glColor4f(0.5f, 0.5f, 0.5f, 0.8f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(fMaxWidth, 0, 0);
		gl.glVertex3f(fMaxWidth, -fHeight, 0);
		gl.glVertex3f(0, -fHeight, 0);
		gl.glEnd();
		
		gl.glColor3f(0.2f, 0.2f, 0.2f);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(fMaxWidth, 0, 0);
		gl.glVertex3f(fMaxWidth, -fHeight, 0);
		gl.glVertex3f(0, -fHeight, 0);
		gl.glEnd();
		
		drawMappingAnnotation(gl);
					
		gl.glPopMatrix();
	}
    
    private void drawPickedObjectInfoStar(final GL gl) {
    	
    	// Calculate star points by taking the gene number as edge count
    	float fStarRadius = llMultipleMappingGenes.size() / 4.0f;
    	
    	if (fStarRadius >= 1.2f)
    		fStarRadius = 1.2f;
    	
		starEffectRenderer.calculateStarPoints(llMultipleMappingGenes.size(), fStarRadius, 0, 0);
    	
		// Draw star effect 
		Iterator<float[]>iterStarPoints = starEffectRenderer.getStarPoints().iterator();			
		float[] fArTmpPosition; 
		
		gl.glPushMatrix();
		
    	if (iterStarPoints.hasNext())
		{
			iterStarPoints.next();
		}
		
    	float fStarElementZDisplacement = 0f;
		gl.glTranslatef(getWorldCoordinatePosition()[0], 
				getWorldCoordinatePosition()[1], 0);
    	gl.glScalef(fScaleFactor, fScaleFactor, fScaleFactor);
		GLStarEffectRenderer.drawStar(gl, starEffectRenderer.getStarPoints());
		gl.glTranslatef(-getWorldCoordinatePosition()[0], 
				-getWorldCoordinatePosition()[1], 0);
		
		while(iterStarPoints.hasNext()) 
		{
			fArTmpPosition = iterStarPoints.next();
			gl.glTranslatef(fArTmpPosition[0]-fWidth/2f, 
					fArTmpPosition[1]+fHeight/2f, fStarElementZDisplacement);				
			drawPickedObjectInfoSingle(gl, false);
			gl.glTranslatef(-fArTmpPosition[0]+fWidth/2f, 
					-fArTmpPosition[1]-fHeight/2f, fStarElementZDisplacement);
			
			fStarElementZDisplacement += 0.002f;
		}
		
		gl.glPopMatrix();

    }
    
    private float calculateInfoAreaWidth(
    		final PathwayVertexGraphItem pickedVertex) {
    	
		textRenderer.begin3DRendering();
		
		float fMaxWidth = 0.0f;
		String sElementId = pickedVertex.getName();
		
		// Save text length as new width if it bigger than previous one
		float fCurrentWidth = 2.2f * (float)textRenderer.getBounds("ID: " 
				+sElementId).getWidth() * GLPathwayManager.SCALING_FACTOR_X;
		
		if (fMaxWidth < fCurrentWidth)
			fMaxWidth = fCurrentWidth;

		String sTmp = "";
		if (pickedVertex.getType().equals(EPathwayVertexType.gene))		
		{
			sTmp = "Gene short name:" +geneAnnotationMapper.getGeneShortNameByNCBIGeneId(sElementId);
		}
		else if (pickedVertex.getType().equals(EPathwayVertexType.map))
		{
			sTmp = pickedVertex.getName();
			
			// Remove "TITLE: "
			if (sTmp.contains("TITLE:"))
				sTmp = sTmp.substring(6);
			
			sTmp = "Pathway name: " + sTmp;
		}
		
		// Save text length as new width if it bigger than previous one
		fCurrentWidth = 2.2f * (float)textRenderer.getBounds(sTmp).getWidth() * GLPathwayManager.SCALING_FACTOR_X;
		if (fMaxWidth < fCurrentWidth)
			fMaxWidth = fCurrentWidth;
		
		textRenderer.end3DRendering();
		
    	return fMaxWidth;
    }
    
    private void drawMappingAnnotation(final GL gl) {
    	
		textRenderer.begin3DRendering();
    	
		float fLineHeight = 2.3f * (float)textRenderer.getBounds("A").getHeight() * GLPathwayManager.SCALING_FACTOR_Y;
		float fXOffset = 0.03f;
		float fYOffset = -0.03f;
		
		PathwayVertexGraphItem tmpVertexGraphItem = llMultipleMappingGenes.getFirst();

		String sElementId = llMultipleMappingGenes.getFirst().getName();
		
		gl.glColor3f(1, 1, 1);
		textRenderer.draw3D("ID: " +sElementId,
				fXOffset, 
				fYOffset - fLineHeight, 
				0.001f,
				0.005f);  // scale factor
		
		String sTmp = "";
		if (tmpVertexGraphItem.getType().equals(EPathwayVertexType.gene))		
		{
			sTmp = "Gene short name:" +geneAnnotationMapper.getGeneShortNameByNCBIGeneId(sElementId);
		}
		else if (tmpVertexGraphItem.getType().equals(EPathwayVertexType.map))
		{
			sTmp = ((PathwayVertexGraphItemRep)tmpVertexGraphItem.getAllItemsByProp(
					EGraphItemProperty.ALIAS_CHILD).get(0)).getName();
			
			// Remove "TITLE: "
			if (sTmp.contains("TITLE:"))
				sTmp = sTmp.substring(6);
			
			sTmp = "Pathway name: " + sTmp;
		}
		textRenderer.draw3D(sTmp,
				fXOffset, 
				fYOffset - 2*fLineHeight, 
				0.001f,
				0.005f);  // scale factor
		
		textRenderer.end3DRendering();
		
		if (bEnableColorMapping) 
		{
			// Render mapping if available
			gl.glTranslatef(10*fXOffset, -3.6f*fLineHeight, -0.045f);
			gl.glScalef(3.0f, 3.0f, 3.0f);
			if (tmpVertexGraphItem.getType().equals(EPathwayVertexType.gene))
			{					
				float fNodeWidth = refRenderStyle.getEnzymeNodeWidth(true);
				
				refGLPathwayManager.mapExpressionByGeneId(
						gl, llMultipleMappingGenes.get(0).getName(), fNodeWidth);
				
				llMultipleMappingGenes.remove(0);
			}
		}
		gl.glScalef(1 / 3.0f, 1 / 3.0f, 1 / 3.0f);
    }
    
    public void convertWindowCoordinatesToWorldCoordinates(final GL gl, 
    		final int iWindowCoordinatePositionX, final int iWindowCoordinatePositionY) {
    	
		double mvmatrix[] = new double[16];
		double projmatrix[] = new double[16];
		int realy = 0;// GL y coord pos
		double[] wcoord = new double[4];// wx, wy, wz;// returned xyz coords
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);
		/* note viewport[3] is height of window in pixels */
		realy = viewport[3] - iWindowCoordinatePositionY - 1;

//		System.out.println("Coordinates at cursor are (" + point.x + ", "
//				+ realy);
		
		GLU glu = new GLU();
		glu.gluUnProject((double) iWindowCoordinatePositionX, (double) realy, 0.0, //
				mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);
		
//		System.out.println("World coords at z=0.0 are ( " //
//				+ wcoord[0] + ", " + wcoord[1] + ", " + wcoord[2]);
		
		if (fArWorldCoordinatePosition == null)
			fArWorldCoordinatePosition = new float[3];
		
		fArWorldCoordinatePosition[0] = (float)wcoord[0];
		fArWorldCoordinatePosition[1] = (float)wcoord[1];
		fArWorldCoordinatePosition[2] = (float)wcoord[2];
    }
    
    public void setWorldCoordinatePosition(float x, float y, float z) {
    	
		if (fArWorldCoordinatePosition == null)
			fArWorldCoordinatePosition = new float[3];
		
    	fArWorldCoordinatePosition[0] = x;
    	fArWorldCoordinatePosition[1] = y;
    	fArWorldCoordinatePosition[2] = z;
    }
    
    public float[] getWorldCoordinatePosition() {
    	
    	return fArWorldCoordinatePosition;
    }
    
    public void resetAnimation() {

    	fScaleFactor = 0.0f;
    }
    
    public void resetPoint() {
    	
    	fArWorldCoordinatePosition = null;
    }
    
    public final boolean isPositionValid() {
    	
    	if (fArWorldCoordinatePosition == null)
    		return false;
    	
    	return true;
    }
    
    private void extractMultipleGeneMapping(
    		final PathwayVertexGraphItemRep pickedVertexRep) {
    	
    	Iterator<IGraphItem> iterMappedGeneItems = 
    		pickedVertexRep.getAllItemsByProp(
    			EGraphItemProperty.ALIAS_PARENT).iterator();
    	
		llMultipleMappingGenes.clear();
		
		while (iterMappedGeneItems.hasNext())
		{
			llMultipleMappingGenes.add(
					(PathwayVertexGraphItem) iterMappedGeneItems.next());
		}
    }
    
    public void enableColorMappingArea(boolean bEnableColorMapping) {
    	
    	this.bEnableColorMapping = bEnableColorMapping;
    }
}
