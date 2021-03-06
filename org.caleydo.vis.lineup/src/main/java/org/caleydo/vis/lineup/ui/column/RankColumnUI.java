/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.vis.lineup.ui.column;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.vis.lineup.model.ARankColumnModel;
import org.caleydo.vis.lineup.model.IRow;
import org.caleydo.vis.lineup.ui.detail.ColoredValueElement;

public class RankColumnUI extends ColumnUI {

	public RankColumnUI(ARankColumnModel model) {
		super(model);
	}

	@Override
	public void layout(int deltaTimeMs) {
		super.layout(deltaTimeMs);
		OrderColumnUI ranker = getColumnParent().getRanker(model);
		if (ranker.haveRankDeltas()) {
			for (GLElement elem : this) {
				IRow row = elem.getLayoutDataAs(IRow.class, null);
				if (row == null)
					continue;
				int delta = ranker.getRankDelta(row);
				if (delta == 0)
					continue;
				((ColoredValueElement) elem).setRankDelta(delta);
			}
		}
	}
}
