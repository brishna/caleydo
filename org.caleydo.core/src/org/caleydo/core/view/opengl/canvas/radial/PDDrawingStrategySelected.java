package org.caleydo.core.view.opengl.canvas.radial;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.PickingManager;

public class PDDrawingStrategySelected
	extends PDDrawingStrategy {
	
	public PDDrawingStrategySelected(PickingManager pickingManager, int iViewID) {
		super(pickingManager, iViewID);
	}

	@Override
	public void drawFullCircle(GL gl, GLU glu, PartialDisc pdDiscToDraw) {

		if (pdDiscToDraw == null)
			return;

		float fRadius = pdDiscToDraw.getCurrentWidth();

		gl.glPushName(pickingManager.getPickingID(iViewID, EPickingType.RAD_HIERARCHY_PDISC_SELECTION,
			pdDiscToDraw.getElementID()));
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT);
		gl.glColor4fv(RadialHierarchyRenderStyle.PARTIAL_DISC_MOUSE_OVER_COLOR, 0);
		GLPrimitives.renderCircle(gl, glu, fRadius, iNumSlicesPerFullDisc);

		gl.glColor4fv(RadialHierarchyRenderStyle.MOUSE_OVER_COLOR, 0);
		GLPrimitives.renderCircleBorder(gl, glu, fRadius, iNumSlicesPerFullDisc,
			RadialHierarchyRenderStyle.PARTIAL_DISC_BORDER_WIDTH);
		gl.glPopAttrib();
		gl.glPopName();

	}

	@Override
	public void drawPartialDisc(GL gl, GLU glu, PartialDisc pdDiscToDraw) {

		if (pdDiscToDraw == null)
			return;

		float fStartAngle = pdDiscToDraw.getCurrentStartAngle();
		float fAngle = pdDiscToDraw.getCurrentAngle();
		float fInnerRadius = pdDiscToDraw.getCurrentInnerRadius();
		float fWidth = pdDiscToDraw.getCurrentWidth();

		float fMidAngle = fStartAngle + (fAngle / 2.0f);
		while (fMidAngle > 360) {
			fMidAngle -= 360;
		}
		
		gl.glPushName(pickingManager.getPickingID(iViewID, EPickingType.RAD_HIERARCHY_PDISC_SELECTION,
			pdDiscToDraw.getElementID()));
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT);

		gl.glColor4fv(RadialHierarchyRenderStyle.PARTIAL_DISC_MOUSE_OVER_COLOR, 0);
		GLPrimitives.renderPartialDisc(gl, glu, fInnerRadius, fInnerRadius + fWidth, fStartAngle, fAngle,
			iNumSlicesPerFullDisc);
		gl.glColor4fv(RadialHierarchyRenderStyle.MOUSE_OVER_COLOR, 0);
		GLPrimitives.renderPartialDiscBorder(gl, glu, fInnerRadius, fInnerRadius + fWidth, fStartAngle,
			fAngle, iNumSlicesPerFullDisc, RadialHierarchyRenderStyle.PARTIAL_DISC_BORDER_WIDTH);
		
		gl.glPopAttrib();
		gl.glPopName();

	}

}