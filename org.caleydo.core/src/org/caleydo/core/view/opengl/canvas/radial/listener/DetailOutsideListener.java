package org.caleydo.core.view.opengl.canvas.radial.listener;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.view.radial.DetailOutsideEvent;
import org.caleydo.core.view.opengl.canvas.radial.GLRadialHierarchy;

public class DetailOutsideListener
	extends AEventListener<GLRadialHierarchy> {

	@Override
	public void handleEvent(AEvent event) {

		if (event instanceof DetailOutsideEvent) {
			DetailOutsideEvent detailOutsideEvent = (DetailOutsideEvent) event;
			handler.handleAlternativeSelection(detailOutsideEvent.getElementID());
		}

	}

}
