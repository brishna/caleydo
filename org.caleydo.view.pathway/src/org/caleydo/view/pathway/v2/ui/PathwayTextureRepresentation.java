/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.pathway.v2.ui;

import gleem.linalg.Vec2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.datadomain.pathway.IVertexRepSelectionListener;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayItemManager;
import org.caleydo.view.pathway.v2.internal.GLPathwayView;

/**
 * Pathway representation that renders a single KEGG- or Wikipathway as texture. The locations {@link PathwayVertexRep}s
 * are masked to allow background augmentations.
 *
 *
 * @author Christian
 *
 */
public class PathwayTextureRepresentation extends APathwayElementRepresentation {

	protected PathwayGraph pathway;

	protected boolean isShaderInitialized = false;
	protected int shaderProgramTextOverlay;

	protected Vec2f renderSize = new Vec2f();
	protected Vec2f origin = new Vec2f();
	protected Vec2f scaling = new Vec2f();

	protected PickingPool pool;

	protected List<IVertexRepSelectionListener> vertexListeners = new ArrayList<>();

	protected float minWidth = -1;
	protected float minHeight = -1;

	protected GLPadding padding = GLPadding.ZERO;

	// protected List<VertexRepBasedContextMenuItem> contextMenuItems = new ArrayList<>();

	public PathwayTextureRepresentation() {
	}

	public PathwayTextureRepresentation(PathwayGraph pathway) {
		this.pathway = pathway;

		// DefaultDirectedGraph<PathwayVertexRep, DefaultEdge> testGraph1 = new
		// DefaultDirectedGraph<>(DefaultEdge.class);
		// PathwayVertexRep v1 = new PathwayVertexRep("A", EPathwayVertexShape.rectangle.name(), (short) 0, (short) 0,
		// (short) 0, (short) 0);
		// PathwayVertexRep v2 = new PathwayVertexRep("B", EPathwayVertexShape.rectangle.name(), (short) 0, (short) 0,
		// (short) 0, (short) 0);
		// PathwayVertexRep v3 = new PathwayVertexRep("C", EPathwayVertexShape.rectangle.name(), (short) 0, (short) 0,
		// (short) 0, (short) 0);
		//
		// testGraph1.addVertex(v1);
		// testGraph1.addVertex(v2);
		// testGraph1.addVertex(v3);
		//
		// testGraph1.addEdge(v1, v2);
		// testGraph1.addEdge(v1, v2);
		// testGraph1.addEdge(v2, v1);
		// testGraph1.addEdge(v1, v1);
		//
		// System.out.println("Edges of A: ");
		// for (DefaultEdge e : testGraph1.edgesOf(v1)) {
		// System.out.println(e);
		// }
	}

	@Override
	protected void init(IGLElementContext context) {
		setVisibility(EVisibility.PICKABLE);

		IPickingListener pickingListener = PickingListenerComposite.concat(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onVertexPick(pick);
			}
		}, context.getSWTLayer().createTooltip(new IPickingLabelProvider() {
			@Override
			public String getLabel(Pick pick) {
				PathwayVertexRep vertexRep = PathwayItemManager.get().getPathwayVertexRep(pick.getObjectID());
				return vertexRep.getLabel();
			}
		}));

		pool = new PickingPool(context, pickingListener);

		super.init(context);
	}

	@Override
	protected void takeDown() {
		pool.clear();
		pool = null;
		super.takeDown();
	}

	private void onVertexPick(Pick pick) {
		PathwayVertexRep vertexRep = PathwayItemManager.get().getPathwayVertexRep(pick.getObjectID());
		for (IVertexRepSelectionListener listener : vertexListeners) {
			listener.onSelect(vertexRep, pick);
		}

		// if (pick.getPickingMode() == PickingMode.RIGHT_CLICKED) {
		// ContextMenuCreator creator = new ContextMenuCreator();
		// for (VertexRepBasedContextMenuItem item : contextMenuItems) {
		// item.setVertexRep(vertexRep);
		// creator.add(item);
		// }
		// context.getSWTLayer().showContextMenu(creator);
		// }
	}

	private void initShaders(GLGraphics g) throws IOException {
		isShaderInitialized = true;
		shaderProgramTextOverlay = g.loadShader(this.getClass().getResourceAsStream("../../vsTextOverlay.glsl"), this
				.getClass().getResourceAsStream("../../fsTextOverlay.glsl"));
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		if (pathway == null)
			return;

		if (!isShaderInitialized) {
			try {
				initShaders(g);
			} catch (IOException e) {
				GLPathwayView.log.error("Error while reading shader file");
			}
		}

		calculateTransforms(w, h);

		if (pathway.getType() == EPathwayDatabaseType.WIKIPATHWAYS) {
			renderBackground(g);
		}

		GL2 gl = g.gl;
		// g.gl.glEnable(GL.GL_BLEND);
		// g.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		if (shaderProgramTextOverlay > 0) {
			gl.glUseProgram(shaderProgramTextOverlay);
			// texture
			gl.glUniform1i(gl.glGetUniformLocation(shaderProgramTextOverlay, "pathwayTex"), 0);
			// which type
			gl.glUniform1i(gl.glGetUniformLocation(shaderProgramTextOverlay, "mode"), this.pathway.getType().ordinal());
		}

		g.fillImage(pathway.getImage().getPath(), origin.x(), origin.y(), renderSize.x(), renderSize.y());

		if (shaderProgramTextOverlay > 0)
			gl.glUseProgram(0);

		// repaint();
		// g.color(0f, 0f, 0f, 1f);
		// for (PathwayVertexRep vertexRep : pathway.vertexSet()) {
		// g.fillRect(getVertexRepBounds(vertexRep));
		// }

	}

	private void renderBackground(GLGraphics g) {
		GL2 gl = g.gl;

		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL.GL_STENCIL_TEST);
		gl.glColorMask(false, false, false, false);
		gl.glDepthMask(false);
		gl.glStencilFunc(GL.GL_NEVER, 1, 0xFF);
		gl.glStencilOp(GL.GL_REPLACE, GL.GL_KEEP, GL.GL_KEEP); // draw 1s on test fail (always)

		// draw stencil pattern
		gl.glStencilMask(0xFF);
		gl.glClear(GL.GL_STENCIL_BUFFER_BIT); // needs mask=0xFF

		for (PathwayVertexRep vertex : pathway.vertexSet()) {
			g.fillRect(getVertexRepBounds(vertex));
		}

		gl.glColorMask(true, true, true, true);
		gl.glDepthMask(true);
		gl.glStencilMask(0x00);
		// draw where stencil's value is 0
		gl.glStencilFunc(GL.GL_EQUAL, 0, 0xFF);

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		gl.glPushMatrix();
		gl.glPushAttrib(GL2.GL_LINE_BIT);

		g.color(1, 1, 1, 1).fillRect(origin.x(), origin.y(), renderSize.x(), renderSize.y());
		g.color(0, 0, 0, 1).drawRect(origin.x(), origin.y(), renderSize.x(), renderSize.y());

		gl.glPopAttrib();
		gl.glPopMatrix();

		gl.glDisable(GL.GL_STENCIL_TEST);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		for (PathwayVertexRep vertexRep : pathway.vertexSet()) {
			g.pushName(pool.get(vertexRep.getID()));
			g.fillRect(getVertexRepBounds(vertexRep));
			g.popName();
		}

	}

	private void calculateTransforms(float w, float h) {

		float availableWidth = w - (padding.left + padding.right);
		float availableHeight = h - (padding.top + padding.bottom);

		float pathwayWidth = pathway.getWidth();
		float pathwayHeight = pathway.getHeight();

		float pathwayAspectRatio = pathwayWidth / pathwayHeight;
		float viewFrustumAspectRatio = availableWidth / availableHeight;

		if (pathwayWidth <= availableWidth && pathwayHeight <= h) {
			scaling.set(1f, 1f);
			renderSize.setX(pathwayWidth);
			renderSize.setY(pathwayHeight);
		} else {
			if (viewFrustumAspectRatio > pathwayAspectRatio) {
				renderSize.setX((availableHeight / pathwayHeight) * pathwayWidth);
				renderSize.setY(availableHeight);
			} else {
				renderSize.setX(availableWidth);
				renderSize.setY((availableWidth / pathwayWidth) * pathwayHeight);
			}
			scaling.set(renderSize.x() / pathwayWidth, renderSize.y() / pathwayHeight);
		}
		origin.set(padding.left + (availableWidth - renderSize.x()) / 2.0f,
				padding.top + (availableHeight - renderSize.y()) / 2.0f);
	}

	@Override
	public PathwayGraph getPathway() {
		return pathway;
	}

	@Override
	public List<PathwayGraph> getPathways() {
		if (pathway == null)
			return new ArrayList<>();
		return Arrays.asList(pathway);
	}

	@Override
	public Rect getVertexRepBounds(PathwayVertexRep vertexRep) {
		if (pathway == null || !pathway.containsVertex(vertexRep))
			return null;
		int coordsX = vertexRep.getCoords().get(0).getFirst();
		int coordsY = vertexRep.getCoords().get(0).getSecond();

		float x = origin.x() + (scaling.x() * coordsX);
		float y = origin.y() + (scaling.y() * coordsY);

		float width = scaling.x() * vertexRep.getWidth();
		float height = scaling.y() * vertexRep.getHeight();

		return new Rect(x, y, width, height);
	}

	@Override
	public List<Rect> getVertexRepsBounds(PathwayVertexRep vertexRep) {
		Rect bounds = getVertexRepBounds(vertexRep);
		if (bounds == null)
			return new ArrayList<>();
		return Arrays.asList(bounds);
	}

	/**
	 * @param minHeight
	 *            setter, see {@link minHeight}
	 */
	public void setMinHeight(float minHeight) {
		this.minHeight = minHeight;
	}

	/**
	 * @param minWidth
	 *            setter, see {@link minWidth}
	 */
	public void setMinWidth(float minWidth) {
		this.minWidth = minWidth;
	}

	@Override
	public void addVertexRepSelectionListener(IVertexRepSelectionListener listener) {
		vertexListeners.add(listener);
	}

	@Override
	public Rect getPathwayBounds() {
		if (pathway == null)
			return null;
		return new Rect(origin.x(), origin.y(), renderSize.x(), renderSize.y());
	}

	@Override
	public float getMinWidth() {
		if (minWidth > 0)
			return minWidth;
		if (pathway == null)
			return 120;
		return pathway.getWidth() * 0.8f;
	}

	@Override
	public float getMinHeight() {
		if (minHeight > 0)
			return minHeight;
		if (pathway == null)
			return 120;
		return pathway.getHeight() * 0.8f;
	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(getMinWidth(), getMinHeight());
	}

	/**
	 * @param padding
	 *            setter, see {@link padding}
	 */
	public void setPadding(GLPadding padding) {
		this.padding = padding;
		repaintAll();
	}

}
