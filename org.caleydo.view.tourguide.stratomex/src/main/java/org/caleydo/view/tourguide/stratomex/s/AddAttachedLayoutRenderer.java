/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.stratomex.s;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.stratomex.EPickingType;
import org.caleydo.view.stratomex.column.BrickColumn;

/**
 * @author Samuel Gratzl
 *
 */
public class AddAttachedLayoutRenderer extends ALayoutRenderer implements IPickingListener {
	private final BrickColumn view;
	private final int id;
	private final TourGuideAddin tourguide;
	private final boolean left;
	private boolean show = false;

	public AddAttachedLayoutRenderer(BrickColumn view, TourGuideAddin tourguide, boolean left) {
		this.view = view;
		this.id = view.getID();
		this.tourguide = tourguide;
		this.left = left;
		view.getStratomexView().addTypePickingListener(this, EPickingType.BRICK_PENETRATING.name());
	}

	@Override
	protected void renderContent(GL2 gl) {
		if (!show)
			return;
		float w1px = view.getPixelGLConverter().getGLWidthForPixelWidth(1);
		float h1px = view.getPixelGLConverter().getGLHeightForPixelHeight(1);
		float hi = h1px * 24;
		float wi = w1px * 24;
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);

		if (left) {
			tourguide.renderAddInDependentButton(gl, -wi - w1px * 2, y - hi * 2, wi, hi, id);
		} else
			tourguide.renderAddDependentButton(gl, x + w1px * 2, y - hi * 2, wi, hi, id);

		gl.glPopAttrib();
	}

	@Override
	protected boolean permitsWrappingDisplayLists() {
		return false;
	}

	@Override
	public void pick(Pick pick) {
		switch(pick.getPickingMode()) {
		case MOUSE_OVER:
			show = view.getHeaderBrick() != null && pick.getObjectID() == view.getHeaderBrick().getID()
					&& !view.getStratomexView().isDetailMode();
			setDisplayListDirty(true);
			break;
		default:
			break;
		}

	}

	@Override
	public void destroy(GL2 gl) {
		view.getStratomexView().removeTypePickingListener(this, EPickingType.BRICK.name());
		super.destroy(gl);
	}

}
