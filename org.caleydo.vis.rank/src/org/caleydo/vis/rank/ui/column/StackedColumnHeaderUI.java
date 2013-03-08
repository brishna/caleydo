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
package org.caleydo.vis.rank.ui.column;

import static org.caleydo.vis.rank.ui.RenderStyle.COLUMN_SPACE;
import static org.caleydo.vis.rank.ui.RenderStyle.HIST_HEIGHT;
import static org.caleydo.vis.rank.ui.RenderStyle.LABEL_HEIGHT;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;

import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IMouseLayer.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.vis.rank.model.ACompositeRankColumnModel;
import org.caleydo.vis.rank.model.ARankColumnModel;
import org.caleydo.vis.rank.model.StackedRankColumnModel;
import org.caleydo.vis.rank.model.mixin.ICompressColumnMixin;
import org.caleydo.vis.rank.ui.SeparatorUI;
import org.caleydo.vis.rank.ui.StackedSeparatorUI;
/**
 * @author Samuel Gratzl
 *
 */
public class StackedColumnHeaderUI extends ACompositeHeaderUI implements IThickHeader {
	protected static final int SUMMARY = 0;
	public final AlignmentDragInfo align = new AlignmentDragInfo();

	protected final StackedRankColumnModel model;

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			switch (evt.getPropertyName()) {
			case StackedRankColumnModel.PROP_ALIGNMENT:
				relayout();
				break;
			case ICompressColumnMixin.PROP_COMPRESSED:
				onCompressedChanged((Boolean) evt.getNewValue());
			}
		}
	};

	public StackedColumnHeaderUI(StackedRankColumnModel model, boolean interactive) {
		super(interactive, 1);
		this.model = model;
		setLayoutData(model);
		this.add(0, new StackedSummaryHeaderUI(model, interactive));
		model.addPropertyChangeListener(ACompositeRankColumnModel.PROP_CHILDREN, childrenChanged);
		model.addPropertyChangeListener(StackedRankColumnModel.PROP_ALIGNMENT, listener);
		model.addPropertyChangeListener(ICompressColumnMixin.PROP_COMPRESSED, listener);
		init(model);
	}


	@Override
	protected SeparatorUI createSeparator(int index) {
		return new StackedSeparatorUI(this, index);
	}

	@Override
	protected GLElement wrapImpl(ARankColumnModel model) {
		GLElement g = ColumnUIs.createHeader(model, this.interactive, false);
		return g;
	}

	@Override
	protected void takeDown() {
		model.removePropertyChangeListener(StackedRankColumnModel.PROP_ALIGNMENT, listener);
		model.removePropertyChangeListener(ACompositeRankColumnModel.PROP_CHILDREN, childrenChanged);
		model.removePropertyChangeListener(ICompressColumnMixin.PROP_COMPRESSED, listener);
		super.takeDown();
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		IGLLayoutElement summary = children.get(0);

		if (model.isCompressed()) {
			summary.setBounds(0, getTopPadding(), w, h - getTopPadding());
			for (IGLLayoutElement child : children.subList(1, children.size()))
				child.hide();
			return;
		}

		summary.setBounds(0, 0, w, HIST_HEIGHT + 4);

		super.doLayout(children, w, h);

		// update the alignment infos
		if (interactive) {
			List<? extends IGLLayoutElement> separators = children.subList(numColumns + 2, children.size());
			final IGLLayoutElement sep0 = children.get(numColumns + 1);
			((StackedSeparatorUI) sep0.asElement()).setAlignment(this.model.getAlignment());
			for (IGLLayoutElement sep : separators) {
				((StackedSeparatorUI) sep.asElement()).setAlignment(this.model.getAlignment());
			}
		}
	}

	protected void onCompressedChanged(boolean isCompressed) {
		((StackedSummaryHeaderUI) get(SUMMARY)).setHasTitle(isCompressed);
		relayout();
		relayoutParent();
	}

	@Override
	protected float getTopPadding() {
		return HIST_HEIGHT + LABEL_HEIGHT;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (!model.isCompressed()) {
			g.color(model.getBgColor()).fillRect(0, HIST_HEIGHT, w, h - HIST_HEIGHT);
			// render the distributions
			float[] distributions = model.getDistributions();
			float yi = HIST_HEIGHT + 7;
			float hi = LABEL_HEIGHT - 6;
			float x = COLUMN_SPACE + 2;
			g.color(Color.GRAY).drawLine(x, yi - 4, w - 2, yi - 4);
			for (int i = 0; i < numColumns; ++i) {
				float wi = model.get(i).getPreferredWidth() + COLUMN_SPACE;
				// g.drawLine(x, yi, x, yi + hi + 2);
				g.drawText(String.format(Locale.ENGLISH, "%.2f%%", distributions[i] * 100), x, yi, wi, hi - 4,
						VAlign.CENTER);
				// g.drawLine(x, yi + hi, x + wi, yi + hi);
				x += wi;
			}
			// g.drawLine(x, yi, x, yi + hi + 2);
		}
		super.renderImpl(g, w, h);
		if (!model.isCompressed()) {
			g.lineWidth(2);
			g.incZ();
			g.color(new Color(0.85f, .85f, .85f)).renderRoundedRect(false, 0, 0, w, HIST_HEIGHT + 4, 5, 3, true, true,
					false,
					false);
			g.drawLine(0, HIST_HEIGHT + 4, 0, h);
			g.drawLine(w, HIST_HEIGHT + 4, w, h);
			g.lineWidth(1);
			g.decZ();
		}
	}

	public void setAlignment(int index) {
		model.setAlignment(index);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	}

	@Override
	public boolean canMoveHere(int index, ARankColumnModel model) {
		return this.model.isMoveAble(model, index);
	}

	@Override
	public void moveHere(int index, ARankColumnModel model) {
		assert canMoveHere(index, model);
		this.model.move(model, index);
	}

	public static class AlignmentDragInfo implements IDragInfo {

	}

	/**
	 * @param model2
	 */
	public void setAlignment(ARankColumnModel model2) {
		// TODO Auto-generated method stub

	}

}

