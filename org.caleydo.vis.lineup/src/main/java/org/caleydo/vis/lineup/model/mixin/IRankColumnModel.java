/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.vis.lineup.model.mixin;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.vis.lineup.model.IRankColumnParent;
import org.caleydo.vis.lineup.model.RankTableModel;

/**
 * @author Samuel Gratzl
 *
 */
public interface IRankColumnModel extends ILabeled {

	/**
	 * @return
	 */
	RankTableModel getTable();

	IRankColumnParent getParent();

	IRankColumnModel clone();
}
