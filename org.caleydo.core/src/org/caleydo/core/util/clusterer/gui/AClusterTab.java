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
package org.caleydo.core.util.clusterer.gui;

import org.caleydo.core.util.clusterer.initialization.AClusterConfiguration;
import org.caleydo.core.util.clusterer.initialization.EDistanceMeasure;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Base class for a tab containing clustering algorithm-specific configurations.
 * It is expected that a new {@link TabItem} is registered with the
 * <code>TabFolder</code> when the constructor is called. The algorithm-specific
 * instance of {@link AClusterConfiguration} is expected to be returned with all
 * algorithm-specific parameters set when {@link #getClusterConfiguration()} is
 * called.
 * 
 * @author Alexander Lex
 * 
 */
public abstract class AClusterTab {
	protected TabItem clusterTab;
	protected TabFolder tabFolder;

	/**
	 *  
	 */
	public AClusterTab(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}

	/** Returns an algorithm-specific cluster configuration */
	public abstract AClusterConfiguration getClusterConfiguration();

	/**
	 * Returns the distance measures supported by this clustering algorithm. By
	 * default returns all measures, must be overriden to reduce the set of
	 * measures
	 */
	public String[] getSupportedDistanceMeasures() {
		return EDistanceMeasure.getNames();
	}

}