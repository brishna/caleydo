package org.caleydo.core.view.opengl.canvas.storagebased.scatterplot;

import java.util.HashMap;

import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.storagebased.heatmap.GLHeatMap;
import org.caleydo.core.view.opengl.canvas.storagebased.heatmap.GLHierarchicalHeatMap;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;

/**
 * ScatterPlot render styles
 * 
 * @author J�rgen Pillhofer
 */

public class ScatterPlotRenderStyle
	extends GeneralRenderStyle {

		
	public static final float FIELD_Z = 0.001f;

	public static final float SELECTION_Z = 0.005f;
	
	public static final float[] X_AXIS_COLOR = { 0.0f, 0.0f, 0.0f, 1.0f };
	public static final float X_AXIS_LINE_WIDTH = 2.0f;
	public static final float[] Y_AXIS_COLOR = { 0.0f, 0.0f, 0.0f, 1.0f };
	public static final float Y_AXIS_LINE_WIDTH = 2.0f;
	public static final float XYAXISDISTANCE = 0.2f;
	public static final float AXIS_Z = 0.0f;
		
	public static float POINTSIZE = 0.05f;
	
	private int iPointSize = 1;
	public static EScatterPointType POINTSTYLE = EScatterPointType.BOX;
	
	public static final float XLABELROTATIONNAGLE = 0.0f;
	public static final float YLABELROTATIONNAGLE = 90.0f;
	public static final float XLABELDISTANCE = 0.03f;
	public static final float YLABELDISTANCE = 0.10f;
	
	public static final int NUMBER_AXIS_MARKERS = 19;
	
	public static final int MIN_AXIS_LABEL_TEXT_SIZE = 60;
	public static final int MIN_NUMBER_TEXT_SIZE = 55;
	
	public static final float LABEL_Z = 0.004f;
	public static final float TEXT_ON_LABEL_Z = LABEL_Z + 0.0001f;
	public static final float AXIS_MARKER_WIDTH = 0.02f;
	
	

	private static final float SELECTED_FIELD_WIDTH_PERCENTAGE = 0.1f;
	private static final float MAXIMUM_SELECTED_AREA_PERCENTAGE = 0.8f;
	public static final int LABEL_TEXT_MIN_SIZE = 50;

	public static final float[] BACKGROUND_COLOR = { 0.8f, 0.8f, 0.8f, 1 };
	public static final float[] DRAGGING_CURSOR_COLOR = { 0.2f, 0.2f, 0.2f, 1 };
	public static final float[] DENDROGRAM_BACKROUND = { 0.5f, 0.5f, 0.5f, 1 };
	public static final float CLUSTER_BORDERS_Z = 0.009f;
	public static final float BUTTON_Z = 0.01f;
	public static final float BACKGROUND_Z = -0.1f;
	
	
	
	private float fSelectedFieldWidth = 1.0f;

	private float fNormalFieldWidth = 0f;

	private float fFieldHeight = 0f;

	private int iLevels = 1;

	private int iNotSelectedLevel = 1000;

	private float fWidthLevel1 = 0.2f;
	private float fWidthLevel2 = 0.0f;
	private float fWidthLevel3 = 0.0f;
	private float fWidthClusterVisualization = 0.1f;
	private float fHeightExperimentDendrogram = 1.45f;
	private float fWidthGeneDendrogram = 1.6f;
	private float fSizeHeatmapArrow = 0.17f;

	// private ArrayList<FieldWidthElement> alFieldWidths;

	private HashMap<Integer, Float> hashLevelToWidth;

	GLHeatMap heatMap;
	GLHierarchicalHeatMap hierarchicalHeatMap;

	private boolean useFishEye = true;

	public void disableFishEye() {
		useFishEye = false;
	}

	//public ScatterPlotRenderStyle(GLHeatMap heatMap, IViewFrustum viewFrustum) {
	public ScatterPlotRenderStyle(GLScatterplot scatterPlot, IViewFrustum viewFrustum) {

		super(viewFrustum);

		//this.heatMap = heatMap;

		// alFieldWidths = new ArrayList<FieldWidthElement>();

		// init fish eye
		float fDelta = (fSelectedFieldWidth - fNormalFieldWidth) / (iLevels + 1);
		hashLevelToWidth = new HashMap<Integer, Float>();
		hashLevelToWidth.put(iNotSelectedLevel, fNormalFieldWidth);
		float fCurrentWidth = fNormalFieldWidth;
		for (int iCount = -iLevels; iCount <= iLevels; iCount++) {
			if (iCount < 0) {
				fCurrentWidth += fDelta;
			}
			else if (iCount == 0) {
				fCurrentWidth = fSelectedFieldWidth;
			}
			else {
				fCurrentWidth -= fDelta;
			}

			hashLevelToWidth.put(iCount, fCurrentWidth);
		}

	}

	public ScatterPlotRenderStyle(GLHierarchicalHeatMap hierarchicalHeatMap, IViewFrustum viewFrustum) {

		super(viewFrustum);

		this.hierarchicalHeatMap = hierarchicalHeatMap;

	}




	
	public float getHeightExperimentDendrogram() {

		return fHeightExperimentDendrogram;
	}

	// function called by HHM to set height of experiment dendrogram
	public void setHeightExperimentDendrogram(float fHeightExperimentDendrogram) {

		this.fHeightExperimentDendrogram = fHeightExperimentDendrogram;
	}

	public float getWidthGeneDendrogram() {

		return fWidthGeneDendrogram;
	}

	// function called by HHM to set width of gene dendrogram
	public void setWidthGeneDendrogram(float fWidthGeneDendrogram) {

		this.fWidthGeneDendrogram = fWidthGeneDendrogram;
	}

	public float getWidthClusterVisualization() {

		return fWidthClusterVisualization;
	}

	public float getWidthLevel1() {

		return fWidthLevel1;
	}

	public float getWidthLevel2() {

		fWidthLevel2 = hierarchicalHeatMap.getViewFrustum().getWidth() / 5;

		return fWidthLevel2;
	}

	public float getWidthLevel3() {

		return fWidthLevel3;
	}

	// function called by HHM to set width of embedded HM
	public void setWidthLevel3(float fWidthLevel3) {

		this.fWidthLevel3 = fWidthLevel3;
	}

	public float getNormalFieldWidth() {

		return fNormalFieldWidth;
	}

	public float getSelectedFieldWidth() {
		return fSelectedFieldWidth;
	}

	public float getFieldHeight() {
		return fFieldHeight;
	}

	public float getYCenter() {

		// TODO: this is only correct for 4 rows
		return viewFrustum.getHeight() / 2;
	}

	public float getXCenter() {

		return viewFrustum.getWidth() / 2;
	}

	public float getXSpacing() {
		return 0.4f;
	}

	public float getYSpacing() {
		return 0.3f;
	}

	public void setPOINTSTYLE(EScatterPointType Type)
	{
		POINTSTYLE = Type;
	}
	
	public void  setPointSize(int value) {
		POINTSIZE = value / 100.0f;
		iPointSize = value;
	}
	
	public int  getPointSize() {
		return iPointSize;
	}
	
	
	// public void setBRenderStorageHorizontally(boolean
	// bRenderStorageHorizontally)
	// {
	// this.bRenderStorageHorizontally = bRenderStorageHorizontally;
	// }

	public float getRenderWidth() {

	
		return viewFrustum.getWidth();
	}

	public float getRenderHeight() {
		
		return viewFrustum.getHeight();

	}
	
public float getLabelHeight() {
		
		return viewFrustum.getHeight()/2;
		

	}

public float getLAbelWidth() {
	
	return viewFrustum.getWidth()/2;

}
public float getAxisHeight()
{
	return viewFrustum.getHeight()-2*XYAXISDISTANCE ;
}
	
public float getAxisWidth()
{
	return viewFrustum.getWidth()-2*XYAXISDISTANCE ;
}


	public float getSizeHeatmapArrow() {
		return fSizeHeatmapArrow;
	}
}
	
	

