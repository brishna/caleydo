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
package org.caleydo.view.entourage.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.AVertexRepBasedEventFactory;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.listener.ShowNodeContextEvent;
import org.caleydo.view.entourage.GLEntourage;

/**
 * @author Christian
 *
 */
public class ShowNodeContextEventFactory extends AVertexRepBasedEventFactory {

	private GLEntourage view;

	/**
	 * @param eventSpace
	 */
	public ShowNodeContextEventFactory(String eventSpace, GLEntourage view) {
		super(eventSpace);
		this.view = view;
	}

	@Override
	public AEvent create(PathwayVertexRep vertexRep) {
		if (view.isControlKeyPressed()) {
			ShowNodeContextEvent event = new ShowNodeContextEvent(vertexRep);
			event.setEventSpace(eventSpace);
			return event;
		}
		return null;
	}

}
