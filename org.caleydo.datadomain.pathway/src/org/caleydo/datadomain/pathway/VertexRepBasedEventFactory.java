/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.datadomain.pathway;

import org.caleydo.core.util.logging.Logger;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.listener.AVertexRepBasedEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Generic Factory for {@link AVertexRepBasedEvent}s.
 *
 * @author Christian Partl
 *
 */
public class VertexRepBasedEventFactory extends AVertexRepBasedEventFactory {

	protected final Class<? extends AVertexRepBasedEvent> eventClass;

	public VertexRepBasedEventFactory(Class<? extends AVertexRepBasedEvent> eventClass, String eventSpace) {
		super(eventSpace);
		this.eventClass = eventClass;
	}

	/**
	 * Creates the event using the specified vertexRep.
	 *
	 * @param vertexRep
	 * @return The event, null if the event could not be created.
	 */
	@Override
	public AVertexRepBasedEvent create(PathwayVertexRep vertexRep) {
		AVertexRepBasedEvent event;
		try {
			event = eventClass.newInstance();
			event.setVertexRep(vertexRep);
			event.setEventSpace(eventSpace);
			return event;
		} catch (InstantiationException | IllegalAccessException e) {
			Logger.log(new Status(IStatus.WARNING, "VertexRepBasedContextMenuItem",
					"Could not instatiate VertexRepBasedEvent!"));
		}

		return null;
	}

}
