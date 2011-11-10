package org.caleydo.core.event.data;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.event.AEvent;

/**
 * Event that signals that a given set needs to be clustered.
 * 
 * @author Marc Streit
 */
@XmlRootElement
@XmlType
public class ClusterSetEvent
	extends AEvent {

	private ArrayList<DataContainer> sets;

	/**
	 * default no-arg constructor
	 */
	public ClusterSetEvent() {
		// nothing to initialize here
	}

	public ClusterSetEvent(ArrayList<DataContainer> sets) {
		this.sets = sets;
	}

	public ArrayList<DataContainer> setTables() {
		return sets;
	}

	public ArrayList<DataContainer> getTables() {
		return sets;
	}

	@Override
	public boolean checkIntegrity() {
		if (sets == null || sets.size() == 0)
			return false;
		return true;
	}

}
