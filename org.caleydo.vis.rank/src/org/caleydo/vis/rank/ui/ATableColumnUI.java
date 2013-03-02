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
package org.caleydo.vis.rank.ui;


import java.beans.IndexedPropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.vis.rank.model.ARankColumnModel;
import org.caleydo.vis.rank.model.IRow;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ATableColumnUI extends GLElementContainer implements IGLLayout, IColumModelLayout {


	public ATableColumnUI(ARankColumnModel model) {
		setLayout(this);
		setLayoutData(model);
	}

	protected void onChildrenChanged(IndexedPropertyChangeEvent evt) {
		int index = evt.getIndex();
		if (evt.getOldValue() instanceof Integer) {
			// moved
			int movedFrom = (Integer) evt.getOldValue();
			this.add(index, get(movedFrom));
		} else if (evt.getOldValue() == null) { // added
			Collection<GLElement> news = null;
			if (evt.getNewValue() instanceof ARankColumnModel) {
				news = Collections.singleton(wrap((ARankColumnModel) evt.getNewValue()));
			} else {
				news = new ArrayList<>();
				for (ARankColumnModel c : (Collection<ARankColumnModel>) evt.getNewValue())
					news.add(wrap(c));
			}
			asList().addAll(index, news);
		} else if (evt.getNewValue() == null) {// removed
			remove(index);
		} else { // replaced
			set(index, wrap((ARankColumnModel) evt.getNewValue()));
		}
		relayoutChildren();
		relayout();
		repaint();
	}

	protected void relayoutChildren() {
		for (GLElement c : this)
			c.relayout();
	}

	protected abstract GLElement wrap(ARankColumnModel model);

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		float x = RenderStyle.COLUMN_SPACE;
		for (IGLLayoutElement col : children) {
			ARankColumnModel model = col.getLayoutDataAs(ARankColumnModel.class, null);
			col.setBounds(x, 0, model.getPreferredWidth(), h);
			x += model.getPreferredWidth() + RenderStyle.COLUMN_SPACE;
		}
	}

	/**
	 * @param data
	 */
	public void setData(Collection<IRow> data) {
		for (GLElement col : this)
			((TableColumnUI) col).setData(data, this);
	}

	/**
	 *
	 */
	public void update() {
		for (GLElement g : this)
			g.relayout();
	}
}

