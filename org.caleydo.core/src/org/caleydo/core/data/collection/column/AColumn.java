/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander Lex, Christian Partl, Johannes Kepler
 * University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.core.data.collection.column;

import java.util.HashMap;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.column.container.FloatContainer;
import org.caleydo.core.data.collection.column.container.IContainer;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.io.DataDescription;

/**
 * <p>
 * Base implementation for columns of data stored in a {@link Table}. A column is a container that holds various
 * representations of a raw data column as read from a source file. Most importantly it contains the raw data as well as
 * a normalized version of the raw data, but other representations are supported as well. It uses {@link IContainer}
 * implementations to store this data.
 * </p>
 * <p>
 * Only the raw data and some metadata can be specified manually, the rest is computed on on demand.
 * </p>
 * <p>
 * This class provides only the base functionality. Data type specific implementations such as {@link NumericalColumn},
 * {@link CategoricalColumn} and {@link GenericColum} exist.
 *
 * @author Alexander Lex
 */

public abstract class AColumn<RawContainerType extends IContainer<RawType>, RawType> {

	/** The class of data stored in this column */
	private EDataClass dataClass;

	/** The data type of the raw data of this column */
	private EDataType rawDataType;

	/** The default transformation of this column */
	private String defaultDataTransformation = Table.Transformation.NONE;


	/** The id of this column, corresponds to the index of the column in the table */
	private int id;

	protected RawContainerType rawContainer;

	protected HashMap<String, FloatContainer> dataRepToContainerMap;

	/**
	 * Constructor Initializes objects
	 */
	public AColumn(DataDescription dataDescription) {
		dataRepToContainerMap = new HashMap<>();
		this.dataClass = dataDescription.getDataClass();
		this.defaultDataTransformation = dataDescription.getDefaultDataTransformation();
		this.rawDataType = dataDescription.getRawDataType();
	}

	/**
	 * @param id
	 *            setter, see {@link id}
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public int getID() {
		return id;
	}

	/**
	 * Returns the data type of the raw data
	 *
	 * @return a value of ERawDataType
	 */
	public EDataType getRawDataType() {
		return rawContainer.getDataType();
	}

	/**
	 * Set the raw data with data type float
	 *
	 * @param rawData
	 *            a float array containing the raw data
	 */
	public void setRawData(RawContainerType rawContainer) {
		if (this.rawContainer != null)
			throw new IllegalStateException("Raw data was already set in column " + id + " , tried to set again.");
		this.rawContainer = rawContainer;
	}

	// public boolean containsDataRepresentation(DataRepresentation dataRepresentation) {
	// return dataRepToContainerMap.containsKey(dataRepresentation);
	// }

	/**
	 * Returns the normalized float value from the index in this column.
	 *
	 * @param dataTransformation
	 *            the transformation that should be used
	 * @param index
	 *            The index of the requested Element
	 * @return The associated value
	 */
	public float getNormalizedValue(String dataTransformation, int index) {
		return dataRepToContainerMap.get(dataTransformation).getPrimitive(index);
	}

	public RawType getRaw(int index) {
		return rawContainer.get(index);
	}

	public String getRawAsString(int index) {
		return rawContainer.get(index).toString();
	}

	/**
	 * Returns the number of raw data elements
	 *
	 * @return the number of raw data elements
	 */
	public int size() {
		return rawContainer.size();
	}

	@Override
	public String toString() {
		return "Dimension for " + getRawDataType() + ", size: " + size();
	}

	/**
	 * Brings any dataset into a format between 0 and 1. This is used for drawing. Works for nominal and numerical data.
	 * Operates with the raw data as basis by default, however when a logarithmized representation is in the dimension
	 * this is used (only applies to numerical data). For nominal data the first value is 0, the last value is 1
	 */
	public void normalize() {
		dataRepToContainerMap.put(Table.Transformation.NONE, rawContainer.normalize());
	}

	public <DataClassSpecificDescriptionType> DataClassSpecificDescriptionType getDataClassSpecificDescription() {
		return null;
	}

	/**
	 * @return the dataClass, see {@link #dataClass}
	 */
	public EDataClass getDataClass() {
		return dataClass;
	}
}
