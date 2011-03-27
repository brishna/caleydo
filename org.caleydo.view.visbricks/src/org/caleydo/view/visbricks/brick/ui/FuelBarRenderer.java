package org.caleydo.view.visbricks.brick.ui;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.set.Set;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.view.visbricks.brick.GLBrick;

/**
 * Renderer for a fuel bar. 
 * 
 * @author Christian Partl
 *
 */
public class FuelBarRenderer extends LayoutRenderer {

	private GLBrick brick;
	private SelectionManager selectionManager;

	public FuelBarRenderer(GLBrick brick) {
		this.brick = brick;
		selectionManager = brick.getContentGroupSelectionManager();
	}

	@Override
	public void render(GL2 gl) {

		ISet set = brick.getSet();
		ContentVirtualArray contentVA = brick.getContentVA();

		if (set == null || contentVA == null)
			return;

		ContentVirtualArray setContentVA = set.getContentData(Set.CONTENT).getContentVA();

		if (setContentVA == null)
			return;

		int totalNumElements = setContentVA.size();

		int currentNumElements = contentVA.size();

		float fuelWidth = (float) x / totalNumElements * currentNumElements;

		gl.glPushName(brick.getPickingManager().getPickingID(brick.getID(),
				EPickingType.BRICK, brick.getID()));

		gl.glBegin(GL2.GL_QUADS);
		if (selectionManager.checkStatus(SelectionType.SELECTION, brick.getGroup()
				.getID()))
			gl.glColor4fv(SelectionType.SELECTION.getColor(),0);
		else
			gl.glColor3f(0.3f, 0.3f, 0.3f);

		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(x, 0, 0);
		gl.glColor3f(0.1f, 0.1f, 0.1f);
		gl.glVertex3f(x, y, 0);
		gl.glVertex3f(0, y, 0);

		gl.glColor3f(0, 0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(fuelWidth, 0, 0);
		gl.glColor3f(1, 1f, 1);
		gl.glVertex3f(fuelWidth, y, 0);
		gl.glVertex3f(0, y, 0);
		gl.glEnd();
		gl.glPopName();

	}
}
