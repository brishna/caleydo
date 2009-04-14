package org.caleydo.rcp.views.swt.toolbar.content;

import org.eclipse.swt.widgets.Group;

/**
 * TollBarContainer for adding swt widgets to a toolbar
 * @author Werner Puff
 */
public abstract class WidgetToolBarContainer
	extends ToolBarContainer {

	/**
	 * Adds the toolbar widgets to the given Group-composite
	 * @param group compiosite to add the toolbar widgets to
	 * @param viewID id of the target view as used by IViewManager implementations
	 */
	public abstract void render(Group group);
}
