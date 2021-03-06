/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.gui.toolbar.action;

import org.caleydo.core.event.view.UseRandomSamplingEvent;
import org.caleydo.core.gui.SimpleAction;
import org.caleydo.core.manager.GeneralManager;

public class UseRandomSamplingAction extends SimpleAction {

	public static final String LABEL = "Use random sampling";
	public static final String ICON = "resources/icons/view/tablebased/random_sampling.png";

	private boolean bFlag = true;

	public UseRandomSamplingAction() {
		super(LABEL, ICON);
		setChecked(bFlag);
	}

	@Override
	public void run() {
		super.run();
		bFlag = !bFlag;
		GeneralManager.get().getEventPublisher().triggerEvent(new UseRandomSamplingEvent(bFlag));
	}
}
