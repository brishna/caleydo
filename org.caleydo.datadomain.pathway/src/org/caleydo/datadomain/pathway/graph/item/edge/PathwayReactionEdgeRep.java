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
package org.caleydo.datadomain.pathway.graph.item.edge;

import java.io.Serializable;

import org.jgrapht.graph.DefaultEdge;

/**
 * Pathway reaction edge representation belonging to the overall pathway graph.
 * Used for KEGG pathways.
 * 
 * @author Marc Streit
 */
public class PathwayReactionEdgeRep
	extends DefaultEdge
	implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final EPathwayReactionEdgeType type;

	/**
	 * Constructor.
	 * 
	 * @param reactionId
	 * @param type
	 */
	public PathwayReactionEdgeRep(final EPathwayReactionEdgeType type) {

		this.type = type;
	}

	public EPathwayReactionEdgeType getType() {

		return type;
	}
}