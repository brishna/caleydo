package org.caleydo.core.view.opengl.util.overlay.infoarea;

import gleem.linalg.Vec3f;
import java.awt.Point;
import javax.media.opengl.GL2;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
import org.caleydo.core.view.opengl.renderstyle.InfoAreaRenderStyle;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;

/**
 * Draw Info Areas Pass a point, and ID and a data type and this class manages all the drawing of the info
 * areas uses GLTextInfoAreaRenderer to draw the actual rectangle, is responsible for creating the renderers
 * (multiple in case of star rendering) and draw the connections from the infoarea to the selected element
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class GLInfoAreaManager {

	private Point pickedPoint;

	private GLInPlaceInfoRenderer infoArea;

	private float fXOrigin = 0;

	private float fYOrigin = 0;

	private float fXElementOrigin = 0;

	private float fYElementOrigin = 0;

	private float fDepth = 0;

	private Vec3f vecLowerLeft;

	// private InformationContentCreator contentCreator;
	//
	// private boolean bUpdateViewInfo = true;
	//
	// private boolean bEnableRendering = true;

	private boolean bRenderInfoArea = false;
	private boolean bFirstTime = false;

	/**
	 * Constructor.
	 */
	public GLInfoAreaManager() {

	}

	public void initInfoInPlace(final ViewFrustum viewFrustum) {

		infoArea = new GLInPlaceInfoRenderer(viewFrustum);
	}

	/**
	 * Render the data previously set
	 * 
	 * @param gl
	 * @param isFirstTime
	 *            this has to be true only the first time you render it and can never be true after that
	 */
	public void renderInPlaceInfo(GL2 gl) {
		if (!bRenderInfoArea)
			return;
		if (bFirstTime) {
			float[] fArWorldCoords =
				GLCoordinateUtils
					.convertWindowCoordinatesToWorldCoordinates(gl, pickedPoint.x, pickedPoint.y);

			fXOrigin = fArWorldCoords[0];
			fYOrigin = fArWorldCoords[1];

			fXElementOrigin = fXOrigin + 0.2f;
			fYElementOrigin = fYOrigin + 0.2f;
			vecLowerLeft.set(fXElementOrigin, fYElementOrigin, 0);

		}

		gl.glColor3fv(InfoAreaRenderStyle.INFO_AREA_COLOR, 0);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3f(fXOrigin, fYOrigin, GeneralRenderStyle.INFO_AREA_CONNECTION_Z);
		gl.glVertex3f(fXElementOrigin, fYElementOrigin, GeneralRenderStyle.INFO_AREA_CONNECTION_Z);
		gl.glVertex3f(fXElementOrigin, fYElementOrigin + infoArea.getHeight(),
			GeneralRenderStyle.INFO_AREA_CONNECTION_Z);
		gl.glEnd();

		infoArea.renderInfoArea(gl, vecLowerLeft, bFirstTime, 0.05f);
		bFirstTime = false;
	}

	/**
	 * Render the data previously set
	 * 
	 * @param gl
	 * @param isFirstTime
	 *            this has to be true only the first time you render it and can never be true after that
	 */
	public void renderRemoteInPlaceInfo(GL2 gl, int iWindowWidth, int iWindowHeight, ViewFrustum frustum) {
		if (!bRenderInfoArea)
			return;
		if (bFirstTime) {
			// float[] fArWorldCoords =
			// GLCoordinateUtils
			// .convertWindowToGLCoordinates(iWindowWidth, iWindowHeight, pickedPoint.x, pickedPoint.y,
			// frustum);

			float[] fArWorldCoords =
				GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(gl, pickedPoint.x,
					pickedPoint.y, fDepth);
			fXOrigin = fArWorldCoords[0];
			fYOrigin = fArWorldCoords[1];

			fXElementOrigin = fXOrigin + 0.2f;
			fYElementOrigin = fYOrigin + 0.2f;
			vecLowerLeft.set(fXElementOrigin, fYElementOrigin, 0);

		}

		gl.glColor3fv(InfoAreaRenderStyle.INFO_AREA_COLOR, 0);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3f(fXOrigin, fYOrigin, 4);
		gl.glVertex3f(fXElementOrigin, fYElementOrigin, 4);
		gl.glVertex3f(fXElementOrigin, fYElementOrigin + infoArea.getHeight(), 4);
		gl.glEnd();

		infoArea.renderInfoArea(gl, vecLowerLeft, bFirstTime, 4);
		bFirstTime = false;
	}

	/**
	 * Set the data to be rendered.
	 * 
	 * @param iCaleydoID
	 * @param eInputDataTypes
	 * @param pickedPoint
	 */
	public void setData(int iCaleydoID, IDType eInputDataTypes, Point pickedPoint, float fDepth) {

		// this.sContent = contentCreator.getStringContentForID(iCaleydoID,
		// eInputDataTypes);
		this.fDepth = fDepth;
		bFirstTime = true;
		bRenderInfoArea = true;
		this.pickedPoint = pickedPoint;
		vecLowerLeft = new Vec3f();

		infoArea.setData(iCaleydoID, eInputDataTypes);
		// miniView = new AGLParCoordsMiniView();
		// fXOrigin = 0;
		// fYOrigin = 0;
		// fHeight = 0;
		// fWidth = 0;
	}

	public void enable(final boolean bEnableRendering) {

		// this.bEnableRendering = bEnableRendering;
	}
}
