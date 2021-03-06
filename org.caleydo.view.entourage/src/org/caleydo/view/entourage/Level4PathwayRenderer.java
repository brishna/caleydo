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
package org.caleydo.view.entourage;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.datadomain.pathway.IPathwayRepresentation;
import org.caleydo.datadomain.pathway.IVertexRepSelectionListener;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

/**
 * @author Christian
 *
 */
public class Level4PathwayRenderer extends ALayoutRenderer implements IPathwayRepresentation {

	protected final PathwayGraph pathway;

	public Level4PathwayRenderer(PathwayGraph pathway) {
		this.pathway = pathway;
	}

	@Override
	protected void renderContent(GL2 gl) {
		// render nothing
	}

	@Override
	protected boolean permitsWrappingDisplayLists() {
		// it is actually better not to create a display list if there is nothing to render
		return false;
	}

	@Override
	public int getMinHeightPixels() {
		return 5;
	}

	@Override
	public int getMinWidthPixels() {
		return 5;
	}

	@Override
	public PathwayGraph getPathway() {
		return pathway;
	}

	@Override
	public List<PathwayGraph> getPathways() {
		List<PathwayGraph> pathways = new ArrayList<>(1);
		pathways.add(pathway);
		return pathways;
	}

	@Override
	public Rect getVertexRepBounds(PathwayVertexRep vertexRep) {
		if (pathway.containsVertex(vertexRep))
			return new Rect();
		return null;
	}

	@Override
	public List<Rect> getVertexRepsBounds(PathwayVertexRep vertexRep) {
		if (pathway.containsVertex(vertexRep)) {
			List<Rect> list = new ArrayList<>(1);
			list.add(new Rect());
			return list;
		}
		return null;
	}

	@Override
	public void addVertexRepSelectionListener(IVertexRepSelectionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Rect getPathwayBounds() {
		return new Rect(0, 0, 0, 0);
	}

	@Override
	public GLElement asGLElement() {
		return null;
	}

	@Override
	public AGLView asAGLView() {
		return null;
	}

	@Override
	public ALayoutRenderer asLayoutRenderer() {
		return this;
	}

	@Override
	public float getMinWidth() {
		return getMinWidthPixels();
	}

	@Override
	public float getMinHeight() {
		return getMinHeightPixels();
	}

	// @Override
	// public void addVertexRepBasedSelectionEvent(IVertexRepBasedEventFactory eventFactory, PickingMode pickingMode) {
	//
	// }

}
