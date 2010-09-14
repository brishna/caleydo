package org.caleydo.core.data.filter.event;

import org.caleydo.core.data.filter.FilterManager;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;

public class ReEvaluateFilterListListener<FilterManagerType extends FilterManager<?, ?, ?, ?>>
	extends AEventListener<FilterManagerType> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof ReEvaluateFilterListEvent<?>) {
			handler.reEvaluateFilters();
		}

	}

}
