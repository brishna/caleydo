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
package org.caleydo.view.tourguide.internal.stratomex;

import static org.caleydo.view.tourguide.internal.TourGuideRenderStyle.STRATOMEX_SELECTED_ELEMENTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainOracle;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.SelectionTypeEvent;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.ITablePerspectiveBasedView;
import org.caleydo.view.stratomex.GLStratomex;
import org.caleydo.view.stratomex.event.HighlightBrickEvent;
import org.caleydo.view.stratomex.event.SelectElementsEvent;
import org.caleydo.view.stratomex.tourguide.event.ConfirmedCancelNewColumnEvent;
import org.caleydo.view.stratomex.tourguide.event.UpdatePreviewEvent;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.internal.TourGuideRenderStyle;
import org.caleydo.view.tourguide.internal.model.AScoreRow;
import org.caleydo.view.tourguide.internal.model.ITablePerspectiveScoreRow;
import org.caleydo.view.tourguide.internal.model.MaxGroupCombiner;
import org.caleydo.view.tourguide.spi.score.IScore;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * facade / adapter to {@link GLStratomex} to hide the communication details
 *
 * @author Samuel Gratzl
 *
 */
public class StratomexAdapter {
	private GLStratomex receiver;

	/**
	 * events that has to be triggered one frame later
	 */
	private final List<AEvent> delayedEvents = new ArrayList<>();

	private TablePerspective currentPreview = null;
	private Group currentPreviewGroup = null;

	private final SelectionType previewSelectionType;

	public StratomexAdapter() {
		// Create volatile selection type
		previewSelectionType = new SelectionType("Tour Guide preview selection type",
				STRATOMEX_SELECTED_ELEMENTS.getRGBA(), 1, true, 1);
		previewSelectionType.setManaged(false);

		triggerEvent(new SelectionTypeEvent(previewSelectionType));
	}

	public void sendDelayedEvents() {
		for (AEvent event : delayedEvents)
			triggerEvent(event);
		delayedEvents.clear();
	}

	public void cleanUp() {
		cleanupPreview();
		SelectionTypeEvent selectionTypeEvent = new SelectionTypeEvent(previewSelectionType);
		selectionTypeEvent.setRemove(true);
		triggerEvent(selectionTypeEvent);
	}

	private void cleanupPreview() {
		if (currentPreview != null) {
			TablePerspective bak = currentPreview;
			removePreview();
			clearHighlightRows(bak.getRecordPerspective().getIdType(), bak.getDataDomain());
		}
	}

	@ListenTo
	private void on(ConfirmedCancelNewColumnEvent event) {
		// remove all temporary stuff
		if (currentPreview != null) {
			clearHighlightRows(currentPreview.getRecordPerspective().getIdType(), currentPreview.getDataDomain());
		}
		this.currentPreview = null;
		this.currentPreviewGroup = null;
	}

	/**
	 * binds this adapter to a concrete stratomex instance
	 *
	 * @param receiver
	 * @return
	 */
	public boolean setStratomex(GLStratomex receiver) {
		if (this.receiver == receiver)
			return false;
		this.cleanupPreview();
		this.receiver = receiver;
		return true;
	}

	public void attach() {
		// TODO Auto-generated method stub

	}

	/**
	 * detach but not close from stratomex, by cleanup up temporary data but keeping them in min
	 */
	public void detach() {
		cleanupPreview();
	}

	/**
	 * whether stratomex currently showing the stratification
	 * 
	 * @param stratification
	 * @return
	 */
	public boolean contains(TablePerspective stratification) {
		if (!hasOne())
			return false;

		for (TablePerspective t : receiver.getTablePerspectives())
			if (t.equals(stratification))
				return true;
		return false;
	}

	public boolean isVisible(AScoreRow row) {
		if (!hasOne())
			return false;
		for (TablePerspective col : receiver.getTablePerspectives()) {
			if (row.is(col))
				return true;
		}
		return false;
	}

	/**
	 * central point for updating the current preview in Stratomex
	 *
	 * @param old
	 * @param new_
	 * @param visibleColumns
	 *            the currently visible scores of the new_ element
	 * @param mode
	 */
	public void updatePreview(AScoreRow old, AScoreRow new_, Collection<IScore> visibleColumns,
			EDataDomainQueryMode mode, IScore sortedBy) {
		if (!hasOne())
			return;

		switch (mode) {
		case PATHWAYS:
			// FIXME
			break;
		case STRATIFICATIONS:
			updateTableBased((ITablePerspectiveScoreRow) old, (ITablePerspectiveScoreRow) new_, visibleColumns,
					sortedBy);
			break;
		case NUMERICAL:
			// FIXME
			break;
		}
	}

	private void updateTableBased(ITablePerspectiveScoreRow old, ITablePerspectiveScoreRow new_,
			Collection<IScore> visibleColumns, IScore sortedBy) {
		TablePerspective strat = new_ == null ? null : new_.asTablePerspective();
		Group group = new_ == null ? null : MaxGroupCombiner.getMax(old, sortedBy);

		// handle stratification changes
		if (currentPreview != null && strat != null) { // update
			if (currentPreview.equals(strat)) {
				if (!Objects.equal(currentPreviewGroup, group)) {
					unhighlightBrick(currentPreview, currentPreviewGroup);
					hightlightBrick(currentPreview, group, true);
					currentPreviewGroup = group;
				}
			} else { // not same stratification
				if (contains(strat)) { // part of stratomex
					unhighlightBrick(currentPreview, currentPreviewGroup);
					hightlightBrick(strat, group, true);
				} else {
					updatePreview(strat, group);
				}
			}
		} else if (currentPreview != null) { // last
			removePreview();
		} else if (strat != null) { // first
			updatePreview(strat, group);
		}

		// highlight connection band
		if (strat != null)
			hightlightRows(new_, visibleColumns, group);
		else if (old != null) {
			clearHighlightRows(old.getIdType(), old.getDataDomain());
		}
	}

	private void updatePreview(TablePerspective strat, Group group) {
		this.currentPreview = strat;
		UpdatePreviewEvent event = new UpdatePreviewEvent(strat);
		event.to(receiver.getTourguide());
		triggerEvent(event);

		if (group != null) {
			hightlightBrick(strat, group, false);
		}
		currentPreviewGroup = group;
	}

	private void removePreview() {
		this.currentPreview = null;
		this.currentPreviewGroup = null;
	}

	private void clearHighlightRows(IDType idType, IDataDomain dataDomain) {
		AEvent event = new SelectElementsEvent(Collections.<Integer> emptyList(), idType, this.previewSelectionType)
				.to(receiver);
		event.setEventSpace(dataDomain.getDataDomainID());
		triggerEvent(event);
	}

	private void hightlightRows(ITablePerspectiveScoreRow new_, Collection<IScore> visibleColumns, Group new_g) {
		Pair<Collection<Integer>, IDType> intersection = new_.getIntersection(visibleColumns, new_g);
		AEvent event = new SelectElementsEvent(intersection.getFirst(), intersection.getSecond(),
				this.previewSelectionType).to(receiver);
		event.setEventSpace(new_.getDataDomain().getDataDomainID());
		triggerEvent(event);
	}

	private void unhighlightBrick(TablePerspective strat, Group g) {
		if (g == null)
			return;
		triggerEvent(new HighlightBrickEvent(strat, g, receiver, this, null));
	}

	private void hightlightBrick(TablePerspective strat, Group g, boolean now) {
		if (g == null)
			return;
		AEvent event = new HighlightBrickEvent(strat, g, receiver, this, TourGuideRenderStyle.STRATOMEX_FOUND_GROUP);
		if (now)
			triggerEvent(event);
		else
			triggerDelayedEvent(event);
	}

	/**
	 * converts the given clinicial Variable using the underlying {@link TablePerspective}
	 *
	 * @param underlying
	 * @param clinicalVariable
	 * @return
	 */
	private static TablePerspective asPerspective(TablePerspective underlying, Integer clinicalVariable) {
		ATableBasedDataDomain dataDomain = DataDomainOracle.getClinicalDataDomain();

		Perspective dim = null;
		for (String id : dataDomain.getDimensionPerspectiveIDs()) {
			Perspective d = dataDomain.getTable().getDimensionPerspective(id);
			VirtualArray va = d.getVirtualArray();
			if (va.size() == 1 && va.get(0) == clinicalVariable) {
				dim = d;
				break;
			}
		}
		if (dim == null) { // not yet existing create a new one
			dim = new Perspective(dataDomain, dataDomain.getDimensionIDType());
			PerspectiveInitializationData data = new PerspectiveInitializationData();
			data.setData(Lists.newArrayList(clinicalVariable));
			dim.init(data);
			dim.setLabel(dataDomain.getDimensionLabel(clinicalVariable), false);

			dataDomain.getTable().registerDimensionPerspective(dim);
		}

		Perspective rec = null;
		Perspective underlyingRP = underlying.getRecordPerspective();

		for (String id : dataDomain.getRecordPerspectiveIDs()) {
			Perspective r = dataDomain.getTable().getRecordPerspective(id);
			if (r.getDataDomain().equals(underlying.getDataDomain())
					&& r.isLabelDefault() == underlyingRP.isLabelDefault()
					&& r.getLabel().equals(underlyingRP.getLabel())) {
				rec = r;
				break;
			}
		}
		if (rec == null) { // not found create a new one
			rec = dataDomain.convertForeignPerspective(underlyingRP);
			dataDomain.getTable().registerRecordPerspective(rec);
		}
		return dataDomain.getTablePerspective(rec.getPerspectiveID(), dim.getPerspectiveID(), false);
	}

	private void triggerEvent(AEvent event) {
		if (event == null)
			return;
		event.setSender(this);
		EventPublisher.trigger(event);
	}

	private void triggerDelayedEvent(AEvent event) {
		if (event == null)
			return;
		delayedEvents.add(event);
	}

	/**
	 * @return whether this adapter is bound to a real stratomex
	 */
	public boolean hasOne() {
		return this.receiver != null;
	}

	/**
	 * @return checks if the given receiver is the currently bound stratomex
	 */
	public boolean is(ITablePerspectiveBasedView receiver) {
		return this.receiver == receiver && this.receiver != null;
	}

	/**
	 * @return checks if the given receiver is the currently bound stratomex
	 */
	public boolean is(Integer receiverID) {
		return this.receiver != null && this.receiver.getID() == receiverID;
	}
}
