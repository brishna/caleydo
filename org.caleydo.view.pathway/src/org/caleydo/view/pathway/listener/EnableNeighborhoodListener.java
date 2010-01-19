package org.caleydo.view.pathway.listener;

import org.caleydo.core.manager.event.AEvent;

public class EnableNeighborhoodListener extends APathwayListener {

	@Override
	public void handleEvent(AEvent event) {
		handler.enableNeighborhood(true);
	}

}
