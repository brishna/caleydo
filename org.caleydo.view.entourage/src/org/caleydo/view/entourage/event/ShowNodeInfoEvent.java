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

import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.listener.AVertexRepBasedEvent;
import org.caleydo.view.entourage.GLEntourage;

/**
 * Event that shows a node info for the specified {@link PathwayVertexRep} in {@link GLEntourage}.
 *
 * @author Christian Partl
 *
 */
public class ShowNodeInfoEvent extends AVertexRepBasedEvent {

	public ShowNodeInfoEvent() {
	}

	public ShowNodeInfoEvent(PathwayVertexRep vertexRep) {
		super(vertexRep);
	}
}
