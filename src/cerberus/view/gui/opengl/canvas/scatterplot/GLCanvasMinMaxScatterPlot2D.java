/**
 * 
 */
package cerberus.view.gui.opengl.canvas.scatterplot;

import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.GLUT;
//import javax.media.opengl.GLCanvas;

//import gleem.linalg.Vec3f;
//import gleem.linalg.Vec4f;

import cerberus.data.collection.ISelection;
import cerberus.data.collection.ISet;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.selection.iterator.ISelectionIterator;
import cerberus.manager.IGeneralManager;
import cerberus.manager.ILoggerManager.LoggerType;
import cerberus.manager.type.ManagerObjectType;
import cerberus.math.statistics.minmax.MinMaxDataInteger;
import cerberus.view.gui.opengl.IGLCanvasUser;
import cerberus.view.gui.opengl.canvas.AGLCanvasUser_OriginRotation;

/**
 * @author kalkusch
 *
 */
public class GLCanvasMinMaxScatterPlot2D 
extends AGLCanvasUser_OriginRotation 
implements IGLCanvasUser
{
	
	protected MinMaxDataInteger minMaxSeaker;
	
	private float [][] viewingFrame;
	
	private int iGridSize = 40;
	
	/**
	 * Color for grid (0,1,2) 
	 * grid text (3,4,5)
	 * and point color (6,7,8)
	 */
	private float[] colorGrid = { 0.1f, 0.1f , 0.9f, 
			0.1f, 0.9f, 0.1f,
			0.9f, 0.1f, 0.1f };
	
	protected float[][] fAspectRatio;
	
	protected float[] fResolution;
	
	protected ISet targetSet;
	
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int MIN = 0;
	public static final int MAX = 1;
	public static final int OFFSET = 2;

	
	/**
	 * @param setGeneralManager
	 */
	public GLCanvasMinMaxScatterPlot2D( final IGeneralManager setGeneralManager,
			int iViewId, 
			int iParentContainerId, 
			String sLabel )
	{
		super( setGeneralManager, 
				iViewId,  
				iParentContainerId, 
				sLabel );
		
		fAspectRatio = new float [2][3];
		viewingFrame = new float [2][2];
		
		fAspectRatio[X][MIN] = 0.0f;
		fAspectRatio[X][MAX] = 20.0f; 
		fAspectRatio[Y][MIN] = 0.0f; 
		fAspectRatio[Y][MAX] = 20.0f; 
		
		fAspectRatio[Y][OFFSET] = 0.0f; 
		fAspectRatio[Y][OFFSET] = -2.0f; 
		
		viewingFrame[X][MIN] = -1.0f;
		viewingFrame[X][MAX] = 1.0f; 
		viewingFrame[Y][MIN] = 1.0f; 
		viewingFrame[Y][MAX] = -1.0f; 
	}
	
	public void renderText( GL gl, 
			final String showText,
			final float fx, 
			final float fy, 
			final float fz ) {
		
		
		final float fFontSizeOffset = 0.09f;
		
	        GLUT glut = new GLUT();
	        
//	        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
//	        gl.glLoadIdentity();
//	        gl.glTranslatef(0.0f,0.0f,-1.0f);
	        
	        // Pulsing Colors Based On Text Position
	        gl.glColor3fv( colorGrid, 3);
	        // Position The Text On The Screen...fullscreen goes much slower than the other
	        //way so this is kind of necessary to not just see a blur in smaller windows
	        //and even in the 640x480 method it will be a bit blurry...oh well you can
	        //set it if you would like :)
	        gl.glRasterPos2f( fx-fFontSizeOffset, fy-fFontSizeOffset );
	        
	        //Take a string and make it a bitmap, put it in the 'gl' passed over and pick
	        //the GLUT font, then provide the string to show
	        glut.glutBitmapString( GLUT.BITMAP_TIMES_ROMAN_24,
	        		showText);
	         
	}
	

	public void setResolution( float[] setResolution ) {
		
//		if ( fResolution.length < 6 ) {
//			throw new RuntimeException("GLCanvasMinMaxScatterPlot2D.setResolution() array must contain 3 items.");
//		}
		
		this.fResolution = setResolution;
		
		fAspectRatio[X][MIN] = fResolution[0];
		fAspectRatio[X][MAX] = fResolution[1]; 
		fAspectRatio[Y][MIN] = fResolution[2]; 
		fAspectRatio[Y][MAX] = fResolution[3]; 
		
		fAspectRatio[X][OFFSET] = fResolution[4]; 
		fAspectRatio[Y][OFFSET] = fResolution[5];
		
		viewingFrame[X][MIN] = fResolution[6];
		viewingFrame[X][MAX] = fResolution[7]; 
		viewingFrame[Y][MIN] = fResolution[8]; 
		viewingFrame[Y][MAX] = fResolution[9]; 
		
		iGridSize = (int) fResolution[10]; 
		
	}
	
	public void setTargetSetId( final int iTargetCollectionSetId ) {
		
		targetSet = 
			refGeneralManager.getSingelton().getSetManager(
					).getItemSet( iTargetCollectionSetId );
		
		if ( targetSet == null ) {
			refGeneralManager.getSingelton().getLoggerManager().logMsg(
					"GLCanvasScatterPlot2D.setTargetSetId(" +
					iTargetCollectionSetId + ") failed, because Set is not registed!");
		}
		
		refGeneralManager.getSingelton().getLoggerManager().logMsg(
				"GLCanvasScatterPlot2D.setTargetSetId(" +
				iTargetCollectionSetId + ") done!");
		
		updateMinMax();
	}
	
	@Override
	public void renderPart(GL gl)
	{
		

		float fx = 1.0f;
		float fy = 1.5f;
		
		float fOffX = 0.0f;
		float fOffY = -2.0f;
		
		gl.glTranslatef( 0,0, 0.01f);
		
//		// x,y [-1 .. 1, -3,5 .. -0.5]
//		gl.glBegin(GL.GL_LINE_LOOP); // Drawing using triangles
//		gl.glVertex3f(-fx+fOffX, -fy+fOffY, 0.0f); // Set the color to red
//		gl.glVertex3f(-fx+fOffX, fy+fOffY, 0.0f); // Top
//		gl.glVertex3f(fx+fOffX, fy+fOffY, 0.0f); // Set the color to green
//		gl.glVertex3f(fx+fOffX, -fy+fOffY, 0.0f); // Bottom left
//		gl.glEnd(); // Finish drawing the triangle
		
		if ( iGridSize > 1 ) 
		{
			drawScatterPlotGrid( gl ,iGridSize );
		}
		
		drawScatterPlotInteger( gl );
		
	
		//System.err.println(" MinMax ScatterPlot2D .render(GLCanvas canvas)");
	}

	protected void drawScatterPlotGrid( GL gl, int iResolution) 
	{
		gl.glColor3fv( colorGrid, 0); // Set the color to red
		
		
		float fIncX = (viewingFrame[X][MAX] - viewingFrame[X][MIN]) / (iResolution + 1);
		float fIncY = (viewingFrame[Y][MAX] - viewingFrame[Y][MIN]) / (iResolution + 1);
		
		float fXvertical = viewingFrame[X][MIN] + fIncX;
		float fYhoricontal = viewingFrame[Y][MIN] + fIncY;
		
		
		for ( int i=0; i < iResolution; i++ )
		{
			gl.glBegin(GL.GL_LINES); // Drawing using triangles
			gl.glVertex3f(fXvertical, viewingFrame[Y][MIN], 0.0f); // Top
			gl.glVertex3f(fXvertical, viewingFrame[Y][MAX], 0.0f); // Bottom left
			
			gl.glVertex3f(viewingFrame[X][MIN], fYhoricontal, 0.0f); // Top
			gl.glVertex3f(viewingFrame[X][MAX], fYhoricontal, 0.0f); // Bottom left
			
//			gl.glVertex3f(fX, fX, 0.0f); // Top
//			gl.glVertex3f(fX, fX, 0.0f); // Bottom left
			gl.glEnd(); // Finish drawing the triangle
			
			fXvertical += fIncX;
			fYhoricontal += fIncY;
		}
		

		
		
		renderText( gl, "X-Axis",
				viewingFrame[X][MIN], 
				-2.0f, 
				0 );
		
		renderText( gl, "Y-Axis", 
				0,
				viewingFrame[Y][MIN], 
				0 );
		
	}
	
	protected void drawScatterPlotInteger(GL gl) {

		
		/**
		 * Box..
		 */
		
		gl.glColor3fv( colorGrid, 0); // Set the color to red
		gl.glBegin(GL.GL_LINE_LOOP); // Drawing using triangles
		gl.glVertex3f(viewingFrame[X][MIN], viewingFrame[Y][MIN], 0.0f); // Top
		gl.glVertex3f(viewingFrame[X][MAX], viewingFrame[Y][MIN], 0.0f); // Bottom left
		gl.glVertex3f(viewingFrame[X][MAX], viewingFrame[Y][MAX], 0.0f); // Bottom left
		gl.glVertex3f(viewingFrame[X][MIN], viewingFrame[Y][MAX], 0.0f); // Bottom left
		gl.glEnd(); // Finish drawing the triangle
		
		/**
		 * End draw Box
		 */
		
		
		if ( targetSet.getDimensions() < 2 ) {
			return;
		}
		
		/**
		 * Check type of set...
		 */
		ManagerObjectType typeData = targetSet.getBaseType();
		
		switch ( typeData )
		{
			case SET_PLANAR: break;
			
			case SET_MULTI_DIM: break;
			
			default:
				refGeneralManager.getSingelton().getLoggerManager().logMsg(
						"GLCanvasScatterPlot assigned Set mut be at least 2-dimesional!",
						LoggerType.VERBOSE );
		} // switch
		
		ISelection [] arraySelectionX = targetSet.getSelectionByDim(0);
		ISelection [] arraySelectionY = targetSet.getSelectionByDim(1);
		
		IStorage [] arrayStorageX = targetSet.getStorageByDim(0);
		IStorage [] arrayStorageY = targetSet.getStorageByDim(1);
				
		int iLoopX = arraySelectionX.length;
		int iLoopY = arraySelectionY.length;
		int iLoopXY = iLoopX;
		
		if ( iLoopX != iLoopY )
		{
			if ( iLoopX < iLoopY )
			{
				iLoopXY = iLoopX;
			}
			else
			{
				iLoopXY = iLoopY;
			}
		}
		
		/**
		 * Consistency check...
		 */
		if (( arrayStorageX.length < iLoopXY)||
				( arrayStorageY.length < iLoopXY))
		{
			refGeneralManager.getSingelton().getLoggerManager().logMsg(
					"GLCanvasScatterPlot assigned Storage must contain at least equal number of Stprages as Selections!",
					LoggerType.ERROR_ONLY );
			return;
		}
		
		//gl.glTranslatef( 0, -2.5f, 0);
		
		gl.glPointSize( this.fResolution[10] );		
		gl.glDisable( GL.GL_LIGHTING );
		gl.glColor3fv( colorGrid, 6); // Set the color to blue one time only	
		
		
		for ( int iOuterLoop = 0; iOuterLoop < iLoopXY; iOuterLoop++  ) 
		{
			ISelection selectX = arraySelectionX[iOuterLoop];
			ISelection selectY = arraySelectionY[iOuterLoop];
			
			ISelectionIterator iterSelectX = selectX.iterator();
			ISelectionIterator iterSelectY = selectY.iterator();
			
			IStorage storeX = arrayStorageX[iOuterLoop];
			IStorage storeY = arrayStorageY[iOuterLoop];
			
			int [] arrayIntX = storeX.getArrayInt();
			int [] arrayIntY = storeY.getArrayInt();
			
			float fTri = 0.05f;
			
			//gl.glBegin(GL.GL_POINT); // Draw a quad
			
			while (( iterSelectX.hasNext() )&&( iterSelectY.hasNext() )) 
			{
				float fX = (float) arrayIntX[ iterSelectX.next() ] / fAspectRatio[X][MAX];
				float fY = (float) arrayIntY[ iterSelectY.next() ] / fAspectRatio[Y][MAX];
				
				//gl.glColor3f(fX * fY, 0.2f, 1 - fX); // Set the color to blue one time only
				
				
				// gl.glBegin(GL.GL_TRIANGLES); // Draw a quad		
				gl.glBegin(GL.GL_POINTS);
				
				gl.glVertex3f(fX + fAspectRatio[X][OFFSET] , fY +fAspectRatio[Y][OFFSET], 0.0f); // Point				
//				gl.glVertex3f(fX + fAspectRatio[X][OFFSET] , fY-fTri +fAspectRatio[Y][OFFSET], 0.0f); // Point
//				gl.glVertex3f(fX-fTri + fAspectRatio[X][OFFSET] , fY +fAspectRatio[Y][OFFSET], 0.0f); // Point
				
				gl.glEnd(); // Done drawing the quad
				
//				gl.glBegin(GL.GL_TRIANGLES); // Drawing using triangles
//				gl.glColor3f(0.0f, 0.0f, 1.0f); // Set the color to red
//				gl.glVertex3f(0.0f, -2.0f, 0.0f); // Top
//				gl.glColor3f(0.0f, 1.0f, 1.0f); // Set the color to green
//				gl.glVertex3f(-1.0f, -1.0f, 0.0f); // Bottom left
//				gl.glColor3f(1.0f, 1.0f, 0.0f); // Set the color to blue
//				gl.glVertex3f(1.0f, -1.0f, 0.0f); // Bottom right
//				gl.glEnd(); // Finish drawing the triangle
				
				//System.out.println( fX + " ; " + fY );
								
			} // while (( iterSelectX.hasNext() )&&( iterSelectY.hasNext() )) 
			
			//gl.glEnd(); // Done drawing the quad
			
		} // for ( int iOuterLoop = 0; iOuterLoop < iLoopXY; iOuterLoop++  ) 			

	}
	
	public void update(GLAutoDrawable canvas)
	{
		// TODO Auto-generated method stub
		System.err.println(" TestTriangle.update(GLCanvas canvas)");
		
		updateMinMax();
	}

	public void destroy()
	{
		// TODO Auto-generated method stub
		System.err.println(" TestTriangle.destroy(GLCanvas canvas)");
	}
	
	protected void updateMinMax() {
		
		if ( targetSet == null )
		{
			return;
		}
		
		if ( minMaxSeaker == null ) 
		{
			minMaxSeaker = new MinMaxDataInteger( targetSet.getId() );		
		}
		else
		{
			minMaxSeaker.useSet( targetSet );
		}
		
		minMaxSeaker.updateData();
		
		if ( minMaxSeaker.getDimension() < 2 ) 
		{
			return;
		}
		
		
//		fAspectRatio[X][MIN] = minMaxSeaker.getMin(0);
//		fAspectRatio[Y][MIN] = minMaxSeaker.getMin(1);
//		fAspectRatio[X][MAX] = minMaxSeaker.getMax(0);
//		fAspectRatio[Y][MAX] = minMaxSeaker.getMax(1);
	}
}
