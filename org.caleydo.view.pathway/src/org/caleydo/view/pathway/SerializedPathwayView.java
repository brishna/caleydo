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
package org.caleydo.view.pathway;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.serialize.ASerializedSingleDataContainerBasedView;
import org.caleydo.core.view.ISingleDataContainerBasedView;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;

/**
 * Serialized form of a pathway-view.
 * 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedPathwayView extends ASerializedSingleDataContainerBasedView {

	/**
	 * id of the pathway in caleydo's pathway library, -1 for unknown pathway
	 * FIXME: this needs to be checked
	 */
	private int pathwayID;

	/**
	 * The id of the pathway data domain. The id of the underlying mapped data
	 * datadomain is stored in {@link ASerializedSingleDataContainerBasedView}
	 */
	private String pathwayDataDomainID;

	/**
	 * Default constructor with default initialization
	 */
	public SerializedPathwayView() {
	}

	public SerializedPathwayView(String pathwayDataDomainID,
			ISingleDataContainerBasedView view) {
		super(view);
		pathwayID = -1;
		this.pathwayDataDomainID = pathwayDataDomainID;
	}

	/**
	 * @return the pathwayDataDomainID, see {@link #pathwayDataDomainID}
	 */
	public String getPathwayDataDomainID() {
		return pathwayDataDomainID;
	}

	/**
	 * @param pathwayDataDomainID
	 *            setter, see {@link #pathwayDataDomainID}
	 */
	public void setPathwayDataDomainID(String pathwayDataDomainID) {
		this.pathwayDataDomainID = pathwayDataDomainID;
	}

	/**
	 * Gets the pathwayId of this SerializedPathwayView
	 * 
	 * @return pathwayId
	 */
	public int getPathwayID() {
		return pathwayID;
	}

	/**
	 * Sets the pathwayId of this SerlializedPathwayView
	 * 
	 * @param pathwayId
	 *            a valid pathwayId as in Caleydo's pathway library or -1 for an
	 *            unknown or uninitialized pathway
	 */
	public void setPathwayID(int pathwayId) {
		this.pathwayID = pathwayId;
	}

	@Override
	public ViewFrustum getViewFrustum() {
		ViewFrustum viewFrustum = new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, -4,
				4, -4, 4, -20, 20);
		return viewFrustum;
	}

	@Override
	public String getViewType() {
		return GLPathway.VIEW_TYPE;
	}

	@Override
	public String getViewClassType() {
		return GLPathway.class.getName();
	}
}
