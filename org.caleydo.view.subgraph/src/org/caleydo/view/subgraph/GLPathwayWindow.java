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
package org.caleydo.view.subgraph;

import javax.media.opengl.GL;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.subgraph.GLSubGraph.PathwayMultiFormInfo;
import org.caleydo.view.subgraph.event.ShowCommonNodesPathwaysEvent;

/**
 * @author Christian
 *
 */
public class GLPathwayWindow extends GLMultiFormWindow {

	protected final GLButton pinButton;

	public GLPathwayWindow(String title, GLSubGraph view, final PathwayMultiFormInfo info, boolean isScrollable) {
		super(title, view, info, isScrollable);
		pinButton = new GLButton(EButtonMode.CHECKBOX);
		pinButton.setSize(16, 16);
		pinButton.setTooltip("Pin");
		pinButton.setRenderer(GLRenderers.fillImage("resources/icons/general/pin.png"));
		pinButton.setSelectedRenderer(new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				g.fillImage("resources/icons/general/pin.png", 0, 0, w, h);
				g.gl.glEnable(GL.GL_BLEND);
				g.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				g.gl.glEnable(GL.GL_LINE_SMOOTH);
				g.color(new Color(1, 1, 1, 0.5f)).fillRoundedRect(0, 0, w, h, Math.min(w, h) * 0.25f);
				g.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
		});
		pinButton.setVisibility(EVisibility.NONE);
		pinButton.setCallback(new ISelectionCallback() {

			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				if (selected) {
					GLPathwayWindow.this.view.addPinnedWindow(GLPathwayWindow.this);
				} else {
					GLPathwayWindow.this.view.removePinnedWindow(GLPathwayWindow.this);
				}
			}
		});
		titleBar.add(titleBar.size() - 2, pinButton);

		background.onPick(new APickingListener() {
			@Override
			protected void rightClicked(Pick pick) {
				ShowCommonNodesPathwaysEvent event = new ShowCommonNodesPathwaysEvent(info.pathway);
				event.setEventSpace(GLPathwayWindow.this.view.getPathEventSpace());
				GLPathwayWindow.this.view.getContextMenuCreator().add(
						new GenericContextMenuItem("Show Pathways with Common Nodes", event));
			}

		});
		background.setTooltip(info.pathway.getTitle());

	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);

		if (active) {
			pinButton.setVisibility(EVisibility.PICKABLE);
		} else {
			pinButton.setVisibility(EVisibility.NONE);
		}
		// background.setHovered(active);
		this.active = active;
	}

	@Override
	public String toString() {

		return ((PathwayMultiFormInfo) info).pathway.getTitle();
	}

}