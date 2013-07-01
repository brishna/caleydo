/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.radial.listener;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.AEventListener;
import org.caleydo.view.radial.GLRadialHierarchy;
import org.caleydo.view.radial.event.ChangeColorModeEvent;

/**
 * Listener that reacts on change color mode events for RadialHierarchy.
 * 
 * @author Christian Partl
 */
public class ChangeColorModeListener extends AEventListener<GLRadialHierarchy> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof ChangeColorModeEvent) {
			handler.changeColorMode();
		}

	}

}
