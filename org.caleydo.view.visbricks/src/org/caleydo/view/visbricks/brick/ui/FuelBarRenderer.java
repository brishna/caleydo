package org.caleydo.view.visbricks.brick.ui;

import javax.media.opengl.GL2;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.view.visbricks.GLVisBricks;
import org.caleydo.view.visbricks.PickingType;
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

		selectionManager = brick.getDataContainerSelectionManager();
	}

	@Override
	public void render(GL2 gl) {

		RecordVirtualArray recordVA = brick.getDataContainer().getRecordPerspective()
				.getVirtualArray();

		if (recordVA == null)
			return;

		RecordVirtualArray setRecordVA = brick.getDimensionGroup().getDataContainer()
				.getRecordPerspective().getVirtualArray();

		if (setRecordVA == null)
			return;

		int totalNumElements = setRecordVA.size();

		int currentNumElements = recordVA.size();

		float fuelWidth = (float) x / totalNumElements * currentNumElements;

		GLVisBricks visBricks = brick.getDimensionGroup().getVisBricksView();

		gl.glPushName(visBricks.getPickingManager().getPickingID(visBricks.getID(),
				PickingType.BRICK.name(), brick.getID()));
		// gl.glPushName(brick.getPickingManager().getPickingID(brick.getID(),
		// PickingType.BRICK, brick.getID()));
		gl.glBegin(GL2.GL_QUADS);

		// if (selectionManager.checkStatus(SelectionType.SELECTION,
		// brick.getGroup()
		// .getID()))
		// gl.glColor4fv(SelectionType.SELECTION.getColor(),0);
		// else
		gl.glColor3f(0.3f, 0.3f, 0.3f);

		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(x, 0, 0);
		gl.glColor3f(0.1f, 0.1f, 0.1f);
		gl.glVertex3f(x, y, 0);
		gl.glVertex3f(0, y, 0);

		gl.glColor3f(0, 0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(fuelWidth, 0, 0);
		if (selectionManager.checkStatus(SelectionType.SELECTION, brick
				.getDataContainer().getRecordPerspective().getTreeRoot().getID())) {
			float[] baseColor = SelectionType.SELECTION.getColor();

			gl.glColor3f(baseColor[0] + 0.3f, baseColor[1] + 0.3f, baseColor[2] + 0.3f);
		} else
			gl.glColor3f(1f, 1f, 1f);
		gl.glVertex3f(fuelWidth, y, 0);
		gl.glVertex3f(0, y, 0);
		gl.glEnd();
		// gl.glPopName();
		gl.glPopName();

	}
}
