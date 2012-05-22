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
package org.caleydo.view.info.selection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.serialize.ASerializedTopLevelDataView;

/**
 * Serialized form of a parallel-coordinates-view.
 * 
 * @author Werner Puff
 * @deprecated This class should extend ASerializedView!
 */
@Deprecated
@XmlRootElement
@XmlType
public class SerializedSelectionInfoView extends ASerializedTopLevelDataView {

	/**
	 * Default constructor with default initialization
	 */
	public SerializedSelectionInfoView() {
	}

	public SerializedSelectionInfoView(String dataDomainType) {
		super(dataDomainType);
	}

	@Override
	public String getViewType() {
		return RcpSelectionInfoView.VIEW_TYPE;
	}

	@Override
	public String getViewClassType() {
		return RcpSelectionInfoView.class.getName();
	}
}