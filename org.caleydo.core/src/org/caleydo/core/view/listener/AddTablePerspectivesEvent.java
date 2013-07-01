/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.view.listener;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.ITablePerspectiveBasedView;

/**
 * Event that triggers adding a list of {@link TablePerspective}s to a specific
 * view.
 * 
 * @author Alexander Lex
 * 
 */
public class AddTablePerspectivesEvent extends AEvent {

	/** The data containers that are to be added to the view */
	private List<TablePerspective> tablePerspectives;

	/** The view which is the receiver of the data containers */
	private ITablePerspectiveBasedView receiver;

	/**
	 * Default constructor.
	 */
	public AddTablePerspectivesEvent() {
	}

	/**
	 * Constructor initializing the event with a single data container.
	 * 
	 * @param tablePerspective
	 *            added to a new instance of {@link #tablePerspectives}
	 */
	public AddTablePerspectivesEvent(TablePerspective tablePerspective) {
		tablePerspectives = new ArrayList<TablePerspective>();
		this.tablePerspectives.add(tablePerspective);
	}

	/**
	 * Constructor initializing the event with multiple data containers.
	 * 
	 * @param tablePerspectives
	 *            set to {@link #tablePerspectives}
	 */
	public AddTablePerspectivesEvent(List<TablePerspective> tablePerspectives) {
		this.tablePerspectives = tablePerspectives;
	}

	/**
	 * @param receiver
	 *            setter, see {@link #receiver}
	 */
	public void setReceiver(ITablePerspectiveBasedView receiver) {
		this.receiver = receiver;
	}

	/**
	 * @return the receiver, see {@link #receiver}
	 */
	public ITablePerspectiveBasedView getReceiver() {
		return receiver;
	}

	public void addTablePerspective(TablePerspective tablePerspective) {
		if (tablePerspectives == null)
			tablePerspectives = new ArrayList<TablePerspective>(1);
		tablePerspectives.add(tablePerspective);

	}

	/**
	 * @param tablePerspectives
	 *            setter, see {@link #tablePerspectives}
	 */
	public void setTablePerspectives(List<TablePerspective> tablePerspectives) {
		this.tablePerspectives = tablePerspectives;
	}

	/**
	 * @return the tablePerspectives, see {@link #tablePerspectives}
	 */
	public List<TablePerspective> getTablePerspectives() {
		return tablePerspectives;
	}

	@Override
	public boolean checkIntegrity() {
		if (tablePerspectives == null)
			return false;

		return true;
	}

}
