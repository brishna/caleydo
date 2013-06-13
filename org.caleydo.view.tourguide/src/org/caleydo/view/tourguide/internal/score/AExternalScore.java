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
package org.caleydo.view.tourguide.internal.score;

import org.caleydo.core.util.color.Color;

import org.caleydo.core.util.base.DefaultLabelProvider;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.api.score.ISerializeableScore;
import org.caleydo.view.tourguide.internal.external.AExternalScoreParseSpecification;
import org.caleydo.vis.rank.model.mapping.PiecewiseMapping;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AExternalScore extends DefaultLabelProvider implements ISerializeableScore {
	private float mappingMin;
	private float mappingMax;
	private Color bgColor;
	private Color color;


	public AExternalScore() {
		super("");
	}

	public AExternalScore(String label, AExternalScoreParseSpecification spec) {
		super(label);
		this.color = spec.getColor();
		this.bgColor = spec.getColor().brighter().brighter();
		this.mappingMin = spec.getMappingMin();
		this.mappingMax = spec.getMappingMax();
	}

	@Override
	public void onRegistered() {

	}

	@Override
	public boolean supports(EDataDomainQueryMode mode) {
		return mode == EDataDomainQueryMode.STRATIFICATIONS;
	}

	@Override
	public String getAbbreviation() {
		return "EX";
	}

	@Override
	public String getDescription() {
		return "External Score " + getLabel();
	}

	@Override
	public String getProviderName() {
		return "External";
	}

	@Override
	public PiecewiseMapping createMapping() {
		boolean tnan = Float.isNaN(mappingMax);
		PiecewiseMapping m;
		if (tnan) {
			m = new PiecewiseMapping(mappingMin, mappingMax);
		} else if (mappingMin > mappingMax) {
			m = new PiecewiseMapping(mappingMax, mappingMin);
			m.put(mappingMax, 1);
			m.put(mappingMin, 0);
		} else {
			m = new PiecewiseMapping(mappingMin, mappingMax);
		}
		return m;
	}

	@Override
	public Color getBGColor() {
		return bgColor;
	}

	@Override
	public Color getColor() {
		return color;
	}

}
