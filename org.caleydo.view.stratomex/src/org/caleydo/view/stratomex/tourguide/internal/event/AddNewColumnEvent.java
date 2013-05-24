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
package org.caleydo.view.stratomex.tourguide.internal.event;

import org.caleydo.core.event.ADirectedEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class AddNewColumnEvent extends ADirectedEvent {

	private int objectId;
	private boolean attached;

	/**
	 * @param objectID
	 */
	public AddNewColumnEvent(int objectID) {
		this(objectID, false);
	}

	/**
	 * @param objectID2
	 * @param b
	 */
	public AddNewColumnEvent(int objectID, boolean attached) {
		this.objectId = objectID;
		this.attached = attached;
	}

	/**
	 * @return the dependentOne, see {@link #dependentOne}
	 */
	public boolean isIndependentOne() {
		return attached && objectId % 2 == 1;
	}

	public boolean isDependentOne() {
		return attached && objectId % 2 == 0;
	}

	/**
	 * @return the objectId, see {@link #objectId}
	 */
	public int getObjectId() {
		return attached ? objectId / 2 : objectId;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}