/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.view.contextmenu.item;

import org.caleydo.core.event.data.StatisticsFoldChangeReductionEvent;
import org.caleydo.core.view.contextmenu.AContextMenuItem;

public class StatisticsFoldChangeReductionItem
	extends AContextMenuItem {

	public StatisticsFoldChangeReductionItem(StatisticsFoldChangeReductionEvent event) {

		setLabel("Fold Change Filter");
		event.setSender(this);
		registerEvent(event);
	}
}
