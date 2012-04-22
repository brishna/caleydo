/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 * 
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.linearizedpathway.mappeddataview;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.mapping.IDMappingManager;
import org.caleydo.core.data.mapping.IDMappingManagerRegistry;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.PixelGLConverter;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;

/**
 * Renders a row caption based on david IDs
 * 
 * @author Alexander Lex
 * 
 */
public class RowCaptionRenderer extends RowRenderer {

	private CaleydoTextRenderer textRenderer;
	private PixelGLConverter pixelGLConverter;

	/**
	 * Constructor
	 * 
	 * @param textRenderer
	 *            the <code>CaleydoTextRenderer</code> of the parent GL view
	 * @param pixelGLConverter
	 *            the <code>PixelGLConverter</code> of the parent GL view
	 * @param davidID
	 *            the id used for the resolution of the human readable id type
	 *            that is rendered
	 */
	public RowCaptionRenderer(Integer davidID, AGLView parentView,
			MappedDataRenderer parent, float[] backgroundColor) {
		super(davidID, parentView, parent);
		textRenderer = parentView.getTextRenderer();
		pixelGLConverter = parentView.getPixelGLConverter();
		topBarColor = backgroundColor;
		bottomBarColor = backgroundColor;

	}

	@Override
	public void render(GL2 gl) {
		ArrayList<SelectionType> selectionTypes = parent.geneSelectionManager
				.getSelectionTypes(davidID);

		calculateColors(selectionTypes);
		float backgroundZ = 0;
		float frameZ = 0.3f;

		gl.glPushName(parentView.getPickingManager().getPickingID(parentView.getID(),
				PickingType.GENE.name(), davidID));

		gl.glColor4fv(topBarColor, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0, 0, backgroundZ);
		gl.glVertex3f(0, y, backgroundZ);
		gl.glColor3f(bottomBarColor[0] * 1.05f, bottomBarColor[1] * 1.05f,
				bottomBarColor[2] * 1.05f);
		// gl.glColor3f(bottomBarColor[0] * 1.f, bottomBarColor[1] * 1.f,
		// bottomBarColor[2] * 1.f);

		gl.glVertex3f(x, y, backgroundZ);
		gl.glVertex3f(x, 0, backgroundZ);
		gl.glEnd();

		gl.glLineWidth(1);
		gl.glColor4fv(MappedDataRenderer.FRAME_COLOR, 0);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, frameZ);
		gl.glVertex3f(0, y, frameZ);
		gl.glVertex3f(x, y, frameZ);
		gl.glVertex3f(x, 0, frameZ);
		gl.glEnd();

		float sideSpacing = pixelGLConverter.getGLWidthForPixelWidth(8);
		float height = pixelGLConverter.getGLHeightForPixelHeight(15);
		IDMappingManager geneIDMappingManager = IDMappingManagerRegistry.get()
				.getIDMappingManager(IDCategory.getIDCategory("GENE"));
		String geneName = geneIDMappingManager.getID(IDType.getIDType("DAVID"),
				IDType.getIDType("GENE_SYMBOL"), davidID);
		if (geneName != null)
			textRenderer.renderTextInBounds(gl, geneName, sideSpacing, (y - height) / 2,
					0.1f, x, height);

		gl.glPopName();
	}

}