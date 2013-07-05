/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dvi.tableperspective;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.view.dvi.GLDataViewIntegrator;
import org.caleydo.view.dvi.node.IDVINode;

public abstract class AMultiTablePerspectiveRenderer extends ALayoutRenderer {

	protected IDVINode node;
	protected GLDataViewIntegrator view;
	protected DragAndDropController dragAndDropController;
	protected Map<Integer, Pair<Point2D, Point2D>> bottomDimensionGroupPositions;
	protected Map<Integer, Pair<Point2D, Point2D>> topDimensionGroupPositions;
	protected List<Pair<String, Integer>> pickingIDsToBePushed;
	protected boolean isUpsideDown = false;
	protected boolean arePickingListenersRegistered = false;

	public AMultiTablePerspectiveRenderer(IDVINode node, GLDataViewIntegrator view,
			DragAndDropController dragAndDropController) {
		this.node = node;
		this.view = view;
		this.dragAndDropController = dragAndDropController;
		bottomDimensionGroupPositions = new HashMap<Integer, Pair<Point2D, Point2D>>();
		topDimensionGroupPositions = new HashMap<Integer, Pair<Point2D, Point2D>>();
	}

	public abstract void setTablePerspectives(List<TablePerspective> tablePerspectives);

	public Pair<Point2D, Point2D> getBottomAnchorPointsOfTablePerspective(
			TablePerspective tablePerspective) {
		return bottomDimensionGroupPositions.get(tablePerspective.getID());
	}

	public Pair<Point2D, Point2D> getTopAnchorPointsOfTablePerspective(
			TablePerspective tablePerspective) {
		return topDimensionGroupPositions.get(tablePerspective.getID());
	}

	public void registerPickingListeners() {
		if (arePickingListenersRegistered)
			return;

		createPickingListeners();

		arePickingListenersRegistered = true;
	}

	protected abstract void createPickingListeners();

	public void unregisterPickingListeners() {
		removePickingListeners();
		arePickingListenersRegistered = false;
	}

	protected abstract void removePickingListeners();

	public void destroy() {
		unregisterPickingListeners();
	}

	public abstract void setUpsideDown(boolean isUpsideDown);

	public boolean isUpsideDown() {
		return isUpsideDown;
	}

	public List<Pair<String, Integer>> getPickingIDsToBePushed() {
		return pickingIDsToBePushed;
	}

	public void setPickingIDsToBePushed(List<Pair<String, Integer>> pickingIDsToBePushed) {
		this.pickingIDsToBePushed = pickingIDsToBePushed;
	}

	protected abstract Collection<TablePerspectiveRenderer> getDimensionGroupRenderers();

	public boolean arePickingListenersRegistered() {
		return arePickingListenersRegistered;
	}

}
