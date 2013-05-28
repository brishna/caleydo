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
/**
 *
 */
package org.caleydo.core.data.perspective.table;

/**
 * Container for average values and related information such as standard deviations
 *
 * @author Alexander Lex
 */
public class Average {

	/** The average value (mean) of the record */
	double arithmeticMean;
	/** The standard deviation from the {@link #arithmeticMean} */
	double standardDeviation;

	/**
	 * @return the arithmeticMean, see {@link #arithmeticMean}
	 */
	public double getArithmeticMean() {
		return arithmeticMean;
	}

	/**
	 * @return the standardDeviation, see {@link #standardDeviation}
	 */
	public double getStandardDeviation() {
		return standardDeviation;
	}

	/**
	 * @param arithmeticMean
	 *            setter, see {@link arithmeticMean}
	 */
	public void setArithmeticMean(double arithmeticMean) {
		this.arithmeticMean = arithmeticMean;
	}

	/**
	 * @param standardDeviation
	 *            setter, see {@link standardDeviation}
	 */
	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

}
