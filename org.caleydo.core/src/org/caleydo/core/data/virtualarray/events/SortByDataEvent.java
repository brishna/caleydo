/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.data.virtualarray.events;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.AEvent;
import org.caleydo.core.id.IDType;

/**
 * Event that signals that a virtual array should be sorted based on it's values
 *
 * @author Alexander Lex
 *
 */
public class SortByDataEvent extends AEvent {

	// private IDType idType;
	private String dataDomainID;
	/** The table persepctive that is the basis for the sorting */
	private String tablePerspectiveKey;

	private TablePerspective tablePerspective;

	/** The id of the perspective to be sorted */
	private IDType perspectiveIDType;

	private IDType otherIDType;
	/** The id of the row/column out of the "other" perspective that is used for determining the sorting */
	private Integer sortByID;

	/**
	 *
	 */
	public SortByDataEvent() {
	}

	public SortByDataEvent(String dataDomainID, TablePerspective tablePerspective, IDType perspectiveIDType,
			IDType otherIDType, Integer id) {
		this.dataDomainID = dataDomainID;
		this.tablePerspective = tablePerspective;
		this.perspectiveIDType = perspectiveIDType;
		this.otherIDType = otherIDType;
		this.sortByID = id;
	}

	public SortByDataEvent(String dataDomainID, String tablePerspectiveKey, IDType perspectiveIDType,
			IDType otherIDType, Integer id) {
		this.dataDomainID = dataDomainID;
		this.tablePerspectiveKey = tablePerspectiveKey;
		this.perspectiveIDType = perspectiveIDType;
		this.otherIDType = otherIDType;
		this.sortByID = id;
	}

	/**
	 * @param dataDomainID
	 *            setter, see {@link dataDomainID}
	 */
	public void setDataDomainID(String dataDomainID) {
		this.dataDomainID = dataDomainID;
	}

	/**
	 * @return the dataDomainID, see {@link #dataDomainID}
	 */
	public String getDataDomainID() {
		return dataDomainID;
	}

	/**
	 * @param perspectiveIDType
	 *            setter, see {@link perspectiveIDType}
	 */
	public void setPerspectiveIDType(IDType perspectiveIDType) {
		this.perspectiveIDType = perspectiveIDType;
	}

	/**
	 * @return the perspectiveIDType, see {@link #perspectiveIDType}
	 */
	public IDType getPerspectiveIDType() {
		return perspectiveIDType;
	}

	/**
	 * @param tablePerspective
	 *            setter, see {@link tablePerspective}
	 */
	public void setTablePerspective(TablePerspective tablePerspective) {
		this.tablePerspective = tablePerspective;
	}

	/**
	 * @param otherIDType
	 *            setter, see {@link otherIDType}
	 */
	public void setOtherIDType(IDType otherIDType) {
		this.otherIDType = otherIDType;
	}

	/**
	 * @return the otherIDType, see {@link #otherIDType}
	 */
	public IDType getSortByIDType() {
		return otherIDType;
	}

	/**
	 * @return the tablePerspective, see {@link #tablePerspective}
	 */
	public TablePerspective getTablePerspective() {
		return tablePerspective;
	}

	/**
	 * @param tablePerspectiveKey
	 *            setter, see {@link tablePerspectiveKey}
	 */
	public void setTablePerspectiveKey(String tablePerspectiveKey) {
		this.tablePerspectiveKey = tablePerspectiveKey;
	}

	/**
	 * @return the tablePerspectiveKey, see {@link #tablePerspectiveKey}
	 */
	public String getTablePerspectiveKey() {
		return tablePerspectiveKey;
	}

	/**
	 * @param sortByID
	 *            setter, see {@link sortByID}
	 */
	public void setSortByID(Integer sortByID) {
		this.sortByID = sortByID;
	}

	/**
	 * @return the sortByID, see {@link #sortByID}
	 */
	public Integer getSortByID() {
		return sortByID;
	}

	@Override
	public boolean checkIntegrity() {
		if (sortByID != null && (tablePerspectiveKey != null || tablePerspective != null) && dataDomainID != null
				&& perspectiveIDType != null)
			return true;

		return false;
	}

}
