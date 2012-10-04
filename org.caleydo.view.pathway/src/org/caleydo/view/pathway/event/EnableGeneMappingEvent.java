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
package org.caleydo.view.pathway.event;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.ISingleTablePerspectiveBasedView;

/**
 * Events that signals that gene mapping within pathway views should be enabled.
 * 
 * @author Alexander Lex
 */
@XmlRootElement
@XmlType
public class EnableGeneMappingEvent extends AEvent {

	/**
	 * Flag determining whether the mapping of experimental data in the pathways
	 * should be switched on (true) or off (false). Defaults to true.
	 */
	private boolean enableGeneMapping = true;

	/**
	 * Default Constructor
	 */
	public EnableGeneMappingEvent() {
	}

	public EnableGeneMappingEvent(boolean enableGeneMapping) {
		this.enableGeneMapping = enableGeneMapping;
	}

	/**
	 * @param enableGeneMapping
	 *            setter, see {@link #enableGeneMapping}
	 */
	public void setEnableGeneMapping(boolean enableGeneMapping) {
		this.enableGeneMapping = enableGeneMapping;
	}

	/**
	 * @return the enableGeneMapping, see {@link #enableGeneMapping}
	 */
	public boolean isEnableGeneMapping() {
		return enableGeneMapping;
	}

	@Override
	public boolean checkIntegrity() {
		// nothing to check
		return true;
	}

}