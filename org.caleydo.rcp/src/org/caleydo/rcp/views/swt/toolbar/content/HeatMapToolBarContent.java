package org.caleydo.rcp.views.swt.toolbar.content;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.canvas.storagebased.GLHeatMap;
import org.caleydo.rcp.action.toolbar.view.storagebased.ChangeOrientationAction;
import org.caleydo.rcp.action.toolbar.view.storagebased.ClearSelectionsAction;
import org.caleydo.rcp.action.toolbar.view.storagebased.PropagateSelectionsAction;
import org.caleydo.rcp.action.toolbar.view.storagebased.ResetViewAction;

/**
 * ToolBarContent implementation for heatmap specific toolbar items.  
 * @author Werner Puff
 */
public class HeatMapToolBarContent
	extends AToolBarContent {

	public static final String IMAGE_PATH = "resources/icons/view/storagebased/heatmap/heatmap.png";

	public static final String VIEW_TITLE = "Heat Map";

	@Override
	public Class<?> getViewClass() {
		return GLHeatMap.class;
	}
	
	@Override
	public List<ToolBarContainer> getDefaultToolBar() {
		ActionToolBarContainer container = new ActionToolBarContainer();

		container.setImagePath(IMAGE_PATH);
		container.setTitle(VIEW_TITLE);
		List<IToolBarItem> actionList = new ArrayList<IToolBarItem>();
		container.setToolBarItems(actionList);

		IToolBarItem switchAxesToPolylinesAction = new ChangeOrientationAction(targetViewID);
		actionList.add(switchAxesToPolylinesAction);
		if (contentType == STANDARD_CONTENT) {
			IToolBarItem clearSelectionsAction = new ClearSelectionsAction(targetViewID);
			actionList.add(clearSelectionsAction);
			IToolBarItem resetViewAction = new ResetViewAction(targetViewID);
			actionList.add(resetViewAction);
			IToolBarItem propagateSelectionAction = new PropagateSelectionsAction(targetViewID);
			actionList.add(propagateSelectionAction);
		}
		
		ArrayList<ToolBarContainer> list = new ArrayList<ToolBarContainer>();
		list.add(container);

		return list;
	}

}
