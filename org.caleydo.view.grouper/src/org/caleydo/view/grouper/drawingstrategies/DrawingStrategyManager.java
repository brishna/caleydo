package org.caleydo.view.grouper.drawingstrategies;

import java.util.HashMap;
import org.caleydo.core.view.opengl.picking.PickingManager;
import org.caleydo.view.grouper.GrouperRenderStyle;
import org.caleydo.view.grouper.drawingstrategies.group.EGroupDrawingStrategyType;
import org.caleydo.view.grouper.drawingstrategies.group.GroupDrawingStrategyDragged;
import org.caleydo.view.grouper.drawingstrategies.group.GroupDrawingStrategyMouseOver;
import org.caleydo.view.grouper.drawingstrategies.group.GroupDrawingStrategyNormal;
import org.caleydo.view.grouper.drawingstrategies.group.GroupDrawingStrategySelection;
import org.caleydo.view.grouper.drawingstrategies.group.IGroupDrawingStrategy;

public class DrawingStrategyManager {

	private HashMap<EGroupDrawingStrategyType, IGroupDrawingStrategy> hashGroupDrawingStrategies;

	public DrawingStrategyManager(String dimensionPerspectiveID,
			PickingManager pickingManager, int viewID, GrouperRenderStyle renderStyle) {

		hashGroupDrawingStrategies = new HashMap<EGroupDrawingStrategyType, IGroupDrawingStrategy>();

		hashGroupDrawingStrategies.put(EGroupDrawingStrategyType.NORMAL,
				new GroupDrawingStrategyNormal(dimensionPerspectiveID, pickingManager,
						viewID, renderStyle));
		hashGroupDrawingStrategies.put(EGroupDrawingStrategyType.MOUSE_OVER,
				new GroupDrawingStrategyMouseOver(pickingManager, viewID, renderStyle));
		hashGroupDrawingStrategies.put(EGroupDrawingStrategyType.SELECTION,
				new GroupDrawingStrategySelection(pickingManager, viewID, renderStyle));
		hashGroupDrawingStrategies.put(EGroupDrawingStrategyType.DRAGGED,
				new GroupDrawingStrategyDragged(renderStyle));

	}

	public IGroupDrawingStrategy getGroupDrawingStrategy(EGroupDrawingStrategyType type) {
		return hashGroupDrawingStrategies.get(type);
	}

}
