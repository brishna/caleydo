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
package org.caleydo.core.view.contextmenu;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.util.collection.Pair;

/**
 * generic implementation of a context menu item
 *
 * @author Samuel Gratzl
 *
 */
public class GroupContextMenuItem extends AContextMenuItem {
	public GroupContextMenuItem(String label) {
		setLabel(label);
	}

	public void add(AContextMenuItem item) {
		super.addSubItem(item);
	}

	public void add(String label, AEvent event) {
		add(new GenericContextMenuItem(label, event));
	}

	public void addAll(Iterable<Pair<String, ? extends AEvent>> events) {
		for (Pair<String, ? extends AEvent> event : events)
			add(event.getFirst(), event.getSecond());
	}
}
