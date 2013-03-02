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
package org.caleydo.vis.rank.layout;

import java.util.Arrays;

import org.caleydo.vis.rank.model.MaxCompositeRankColumnModel;

/**
 * row height layouts determine on the one hand the row heights of items and will also be used for layouting the rows of
 * a {@link MaxCompositeRankColumnModel}
 *
 * @author Samuel Gratzl
 *
 */
public class RowHeightLayouts {
	public interface IRowHeightLayout {
		/**
		 * returns a computed list of row heights, when it is smaller than the number of rows, the remaining rows will
		 * be hidden
		 *
		 * @param numRows
		 *            the number of visible rows
		 * @param selectedRowIndex
		 *            the index of the selected one
		 * @param h
		 *            the available height
		 * @return
		 */
		float[] compute(int numRows, int selectedRowIndex, float h);
	}

	public static final IRowHeightLayout UNIFORM = new IRowHeightLayout() {
		@Override
		public float[] compute(int numRows, int selectedRowIndex, float h) {
			int visibleRows = (int) Math.round(Math.floor(h / 22));
			float[] r = new float[Math.min(numRows, visibleRows)];
			Arrays.fill(r, 20);
			return r;
		}
	};

	public static final IRowHeightLayout LINEAR = new IRowHeightLayout() {
		@Override
		public float[] compute(int numRows, int selectedRowIndex, float h) {
			final float firstHeight = 22;
			final float selectedHeight = 35;
			final float minHeight = 3;
			final float minDelta = 0.25f;
			final float maxDelta = 0.75f;

			int numDeltas = (numRows - 1) * (numRows) / 2;
			if ((firstHeight * numRows - numDeltas * minDelta) < h) {
				// TODO
				System.out.println();
			}

			float[] r = new float[numRows];
			float act = firstHeight;

			float delta = -0.25f;
			float acc = 0;
			for (int i = 0; i < numRows; ++i) {
				r[i] = act;
				act += delta;
				if (act <= minHeight)
					delta = 0;
				if (i == selectedRowIndex) {
					r[i] = selectedHeight;
				}
				acc += r[i];
				if (acc >= (h - act)) {
					r = Arrays.copyOf(r, i + 1);
					break;
				}
			}
			return r;
		}
	};

	public static final IRowHeightLayout JUST_SELECTED = new IRowHeightLayout() {
		@Override
		public float[] compute(int numRows, int selectedRowIndex, float h) {
			if (selectedRowIndex < 0)
				return new float[0];
			float[] r = new float[selectedRowIndex + 1];
			Arrays.fill(r, 0);
			r[selectedRowIndex] = h;
			return r;
		}
	};

	public static final IRowHeightLayout HINTS = new IRowHeightLayout() {
		@Override
		public float[] compute(int numRows, int selectedRowIndex, float h) {
			if (h <= (numRows + 2) || selectedRowIndex < 0)
				return JUST_SELECTED.compute(numRows, selectedRowIndex, h);

			float[] r = new float[numRows];
			float delta = h / (numRows + 2);
			Arrays.fill(r, delta);
			r[selectedRowIndex] = delta * 3;
			return r;
		}
	};
}
