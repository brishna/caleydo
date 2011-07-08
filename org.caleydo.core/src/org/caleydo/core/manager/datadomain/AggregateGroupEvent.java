package org.caleydo.core.manager.datadomain;

import java.util.Set;

import org.caleydo.core.manager.event.AEvent;

/**
 * Event signaling that the groups specified should be aggregated into one storage
 * 
 * @author Alexander Lex
 */
public class AggregateGroupEvent
	extends AEvent {

	private Set<Integer> setGroupsToAggregate;

	public AggregateGroupEvent(Set<Integer> setGroupsToAggregate) {
		this.setGroupsToAggregate = setGroupsToAggregate;
	}

	@Override
	public boolean checkIntegrity() {
		return setGroupsToAggregate != null;
	}

	public Set<Integer> getGroups() {
		return setGroupsToAggregate;
	}

	public void setGroupsToDelete(Set<Integer> setGroupsToDelete) {
		this.setGroupsToAggregate = setGroupsToDelete;
	}

}