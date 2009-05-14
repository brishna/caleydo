package org.caleydo.rcp.view.swt;

import java.util.ArrayList;

import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.event.view.ClearSelectionsEvent;
import org.caleydo.core.manager.event.view.browser.ChangeURLEvent;
import org.caleydo.core.manager.event.view.remote.LoadPathwayEvent;
import org.caleydo.core.manager.event.view.remote.LoadPathwaysByGeneEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.specialized.genetic.GeneticIDMappingHelper;
import org.caleydo.rcp.Activator;
import org.eclipse.core.runtime.Status;

public class SearchViewMediator {
	
	IEventPublisher eventPublisher;
	
	public SearchViewMediator() {
		eventPublisher = GeneralManager.get().getEventPublisher();
	}

	public void loadPathway(int pathwayID) {
		LoadPathwayEvent event = new LoadPathwayEvent();
		event.setSender(this);
		event.setPathwayID(pathwayID);
		eventPublisher.triggerEvent(event);
	}
	
	public void loadURLInBrowser(String url) {
		ChangeURLEvent event = new ChangeURLEvent();
		event.setSender(this);
		event.setUrl(url);
		eventPublisher.triggerEvent(event);
	}
	
	public void loadPathwayByGene(int davidID) {
		LoadPathwaysByGeneEvent loadPathwaysByGeneEvent = new LoadPathwaysByGeneEvent();
		loadPathwaysByGeneEvent.setSender(this);
		loadPathwaysByGeneEvent.setGeneID((davidID));
		loadPathwaysByGeneEvent.setIdType(EIDType.DAVID);
		eventPublisher.triggerEvent(loadPathwaysByGeneEvent);		
	}
	
	public void selectGeneSystemWide(int davidID) {
		
		// First the current selections need to be cleared
		ClearSelectionsEvent clearSelectionsEvent = new ClearSelectionsEvent();
		clearSelectionsEvent.setSender(this);
		eventPublisher.triggerEvent(clearSelectionsEvent);
		
		// Create new selection with the selected david ID
		SelectionUpdateEvent selectionUpdateEvent = new SelectionUpdateEvent();
		selectionUpdateEvent.setSender(this);
		
		ISelectionDelta delta = new SelectionDelta(EIDType.EXPRESSION_INDEX);
		
		ArrayList<Integer> alExpressionIndex = GeneticIDMappingHelper.get().getExpressionIndicesFromDavid(davidID);
		
		if (alExpressionIndex == null) {
			GeneralManager.get().getLogger().log(new Status(Status.WARNING, Activator.PLUGIN_ID,
				"Cannot load gene in heat map because no gene expression is associated."));
			return;			
		}
		
		for (Integer expressionIndex : alExpressionIndex) {
			delta.addSelection(expressionIndex, ESelectionType.SELECTION);
		}
		
		selectionUpdateEvent.setSelectionDelta(delta);
		eventPublisher.triggerEvent(selectionUpdateEvent);
	}
}
