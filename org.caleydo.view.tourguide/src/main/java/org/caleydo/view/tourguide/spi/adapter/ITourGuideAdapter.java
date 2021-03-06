/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.tourguide.spi.adapter;

import java.net.URL;
import java.util.Collection;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.vis.ITourGuideView;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.caleydo.vis.lineup.model.RankTableModel;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Samuel Gratzl
 *
 */
public interface ITourGuideAdapter extends ILabeled {
	/**
	 * @param table
	 * @param glTourGuideView
	 */
	void addDefaultColumns(RankTableModel table);

	/**
	 * return the view part icon, should be composition of LineUp and the adapted view
	 *
	 * @return
	 */
	URL getIcon();

	/**
	 * first attach version
	 */
	void setup(ITourGuideView vis, GLElementContainer lineUp);

	void cleanup();

	/**
	 * is the given row currently previewed?
	 *
	 * @param row
	 * @return
	 */
	boolean isPreviewing(AScoreRow row);

	/**
	 * @param row
	 * @return
	 */

	boolean isVisible(AScoreRow row);

	/**
	 * @param old
	 * @param new_
	 * @param visibleScores
	 * @param mode
	 * @param sortedByScore
	 */
	void update(AScoreRow old, AScoreRow new_, Collection<IScore> visibleScores, IScore sortedByScore);

	/**
	 *
	 */
	void preDisplay();

	/**
	 * @return
	 */
	boolean canShowPreviews();


	/**
	 * @param table
	 * @param pickingMode
	 * @param row
	 * @param isSelected
	 */
	void onRowClick(RankTableModel table, PickingMode pickingMode, AScoreRow row, boolean isSelected,
			IGLElementContext context);

	/**
	 * @return
	 */
	String getPartName();

	/**
	 * @return
	 */
	String getSecondaryID();

	/**
	 * @return
	 */
	ITourGuideDataMode asMode();

	/**
	 * predicate filter method for filtering {@link ITourGuideDataMode} defined data domains in a view specific way
	 *
	 * @param query
	 * @return
	 */
	boolean filterBoundView(ADataDomainQuery query);

	/**
	 * @param part
	 * @param isBoundTo
	 * @return
	 */
	boolean isRepresenting(IWorkbenchPart part, boolean isBoundTo);

	/**
	 * bind to the specific instance
	 *
	 * @param part
	 */
	void bindTo(IViewPart part);

	/**
	 * whether to ignore the current active part change, e.g. dvi
	 *
	 * @param part
	 * @return
	 */
	boolean ignoreActive(IViewPart part);

	/**
	 * @return whether the current adapter is bound to a specific view instance
	 */
	boolean isBound2View();
}
