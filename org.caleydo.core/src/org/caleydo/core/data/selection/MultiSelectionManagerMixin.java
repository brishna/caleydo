/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.data.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.selection.delta.DeltaConverter;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.SelectionCommandEvent;
import org.caleydo.core.event.data.SelectionUpdateEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;

import com.google.common.collect.Iterators;

/**
 * a mixin container class for handling selection in a single class
 *
 * important: annotate the field of this element with the {@link DeepScan} annotation to ensure that the listener will
 * be created
 *
 * @author Samuel Gratzl
 *
 */
public class MultiSelectionManagerMixin implements Iterable<SelectionManager> {
	protected final List<SelectionManager> selectionManagers = new ArrayList<>(2);
	protected final ISelectionMixinCallback callback;

	public MultiSelectionManagerMixin(ISelectionMixinCallback callback) {
		this.callback = callback;
	}

	public final void add(SelectionManager manager) {
		this.selectionManagers.add(manager);
	}

	public final SelectionManager get(int index) {
		return selectionManagers.get(index);
	}

	@ListenTo
	private void onSelectionUpdate(SelectionUpdateEvent event) {
		if (event.getSender() == this) // ignore event sent by myself
			return;
		SelectionDelta selectionDelta = event.getSelectionDelta();
		final IDType idType = selectionDelta.getIDType();

		for (SelectionManager selectionManager : selectionManagers) {
			final IDType sidType = selectionManager.getIDType();
			if (idType.resolvesTo(sidType)) {
				// Check for type that can be handled
				selectionDelta = convert(selectionDelta, sidType);
				selectionManager.setDelta(selectionDelta);
				callback.onSelectionUpdate(selectionManager);
				break;
			}
		}
	}

	private SelectionDelta convert(SelectionDelta selectionDelta, IDType sidType) {
		if (selectionDelta.getIDType().equals(sidType))
			return selectionDelta;
		return DeltaConverter.convertDelta(IDMappingManagerRegistry.get().getIDMappingManager(sidType),
				sidType, selectionDelta);
	}

	@ListenTo
	private void onSelectionCommand(SelectionCommandEvent event) {
		if (event.getSender() == this) // ignore event sent by myself
			return;
		IDCategory idCategory = event.getIdCategory();
		SelectionCommand cmd = event.getSelectionCommand();

		for (SelectionManager selectionManager : selectionManagers) {
			final IDType sidType = selectionManager.getIDType();
			if (idCategory == null || idCategory.isOfCategory(sidType)) {
				selectionManager.executeSelectionCommand(cmd);
				callback.onSelectionUpdate(selectionManager);
				if (idCategory != null)
					break;
			}
		}
	}

	public final void fireSelectionDelta(IDType type) {
		for (SelectionManager m : selectionManagers) {
			if (m.getIDType().equals(type))
				fireSelectionDelta(m);
		}
	}

	public final SelectionManager getSelectionManager(IDType type) {
		for (SelectionManager m : selectionManagers) {
			if (m.getIDType().equals(type))
				return m;
		}
		return null;
	}

	@Override
	public final Iterator<SelectionManager> iterator() {
		return Iterators.unmodifiableIterator(selectionManagers.iterator());
	}

	public final void fireSelectionDelta(SelectionManager manager) {
		SelectionDelta selectionDelta = manager.getDelta();
		SelectionUpdateEvent event = createEvent();
		event.setSelectionDelta(selectionDelta);
		EventPublisher.trigger(event);
	}

	protected SelectionUpdateEvent createEvent() {
		SelectionUpdateEvent event = new SelectionUpdateEvent();
		event.setSender(this);
		return event;
	}

	public interface ISelectionMixinCallback {
		void onSelectionUpdate(SelectionManager manager);
	}
}