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
package org.caleydo.vis.rank.model;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.vis.rank.internal.ui.TextRenderer;
import org.caleydo.vis.rank.model.mixin.ICollapseableColumnMixin;
import org.caleydo.vis.rank.model.mixin.IExplodeableColumnMixin;
import org.caleydo.vis.rank.model.mixin.IHideableColumnMixin;
import org.caleydo.vis.rank.model.mixin.IRankableColumnMixin;
import org.caleydo.vis.rank.ui.GLPropertyChangeListeners;
import org.caleydo.vis.rank.ui.detail.MultiScoreBarElement;
import org.caleydo.vis.rank.ui.detail.StackedScoreSummary;

/**
 * @author Samuel Gratzl
 *
 */
public class MaxCompositeRankColumnModel extends AMultiRankColumnModel implements IRankableColumnMixin,
		IHideableColumnMixin, IExplodeableColumnMixin, ICollapseableColumnMixin {

	public MaxCompositeRankColumnModel() {
		super(Color.GRAY, new Color(0.95f, 0.95f, 0.95f));
		setHeaderRenderer(new TextRenderer("MAX", this));
	}

	public MaxCompositeRankColumnModel(MaxCompositeRankColumnModel copy) {
		super(copy);
		setHeaderRenderer(new TextRenderer("MAX", this));
		cloneInitChildren();
	}

	@Override
	public MaxCompositeRankColumnModel clone() {
		return new MaxCompositeRankColumnModel(this);
	}


	@Override
	public GLElement createSummary(boolean interactive) {
		return new StackedScoreSummary(this, interactive);
	}

	@Override
	public GLElement createValue() {
		return new RepaintingGLElement();
	}

	@Override
	public boolean canAdd(ARankColumnModel model) {
		return model instanceof IRankableColumnMixin && !(model instanceof ACompositeRankColumnModel)
				&& super.canAdd(model);
	}

	@Override
	public void explode() {
		parent.explode(this);
	}

	@Override
	public MultiFloat getSplittedValue(IRow row) {
		if (children.isEmpty())
			return new MultiFloat(-1);
		float max = Float.NEGATIVE_INFINITY;
		int maxIndex = -1;
		float[] vs = new float[size()];
		int i = 0;
		for (ARankColumnModel col : this) {
			float v = ((IRankableColumnMixin) col).applyPrimitive(row);
			vs[i++] = v;
			if (v > max) {
				maxIndex = i - 1;
				max = v;
			}
		}
		return new MultiFloat(maxIndex, vs);
	}

	@Override
	public float applyPrimitive(IRow row) {
		if (children.isEmpty())
			return 0;
		float max = Float.NEGATIVE_INFINITY;
		for (ARankColumnModel col : this) {
			float v = ((IRankableColumnMixin) col).applyPrimitive(row);
			max = Math.max(max, v);
		}
		if (max > 1) {
			System.err.println();
		}
		return max;
	}

	@Override
	public boolean isValueInferred(IRow row) {
		int repr = getSplittedValue(row).repr;
		if (repr < 0)
			return false;
		return (((IRankableColumnMixin) get(repr)).isValueInferred(row));
	}

	/**
	 * @param a
	 * @return
	 */
	public static boolean canBeChild(ARankColumnModel model) {
		return model instanceof IRankableColumnMixin;
	}

	@Override
	public boolean isFlatAdding(ACompositeRankColumnModel model) {
		return model instanceof MaxCompositeRankColumnModel;
	}

	private class RepaintingGLElement extends MultiScoreBarElement {
		private final PropertyChangeListener l = GLPropertyChangeListeners.repaintOnEvent(this);

		public RepaintingGLElement() {
			super(MaxCompositeRankColumnModel.this);
		}
		@Override
		protected void init(IGLElementContext context) {
			super.init(context);
			addPropertyChangeListener(PROP_CHILDREN, l);
		}

		@Override
		protected void takeDown() {
			removePropertyChangeListener(PROP_CHILDREN, l);
			super.takeDown();
		}
	}
}
