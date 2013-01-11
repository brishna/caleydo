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
package org.caleydo.view.tourguide.impl;

import java.util.Set;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainOracle;
import org.caleydo.core.data.perspective.variable.ARecordPerspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.DefaultLabelProvider;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.contextmenu.ContextMenuCreator;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.contextmenu.GroupContextMenuItem;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.api.query.ESorting;
import org.caleydo.view.tourguide.api.query.ScoringElement;
import org.caleydo.view.tourguide.api.score.DefaultComputedGroupScore;
import org.caleydo.view.tourguide.api.score.EScoreType;
import org.caleydo.view.tourguide.impl.algorithm.LogRank;
import org.caleydo.view.tourguide.internal.event.AddScoreColumnEvent;
import org.caleydo.view.tourguide.spi.IMetricFactory;
import org.caleydo.view.tourguide.spi.algorithm.IGroupAlgorithm;
import org.caleydo.view.tourguide.spi.score.IRegisteredScore;
import org.caleydo.view.tourguide.spi.score.IScore;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class LogRankMetricFactory implements IMetricFactory {
	@Override
	public void addCreateMetricItems(ContextMenuCreator creator, Set<IScore> visible, Object receiver) {
		Iterable<LogRankMetric> logRankScores = Iterables.filter(Iterables.transform(
				DataDomainOracle.getClinicalVariables(), new Function<Pair<Integer, String>, LogRankMetric>() {
					@Override
					public LogRankMetric apply(Pair<Integer, String> p) {
						return new LogRankMetric(p.getSecond(), p.getFirst());
					}
				}), Predicates.not(Predicates.in(visible)));

		GroupContextMenuItem logRanks = new GroupContextMenuItem("Create LogRank of");
		boolean hasOne = false;
		for (LogRankMetric score : logRankScores) {
			hasOne = true;
			IScore logRankPValue = new LogRankPValue(score.getLabel() + " (P-V)", score);
			logRanks.add(new GenericContextMenuItem(score.getLabel(), new AddScoreColumnEvent(score, logRankPValue)
					.to(receiver)));
		}
		if (hasOne)
			creator.addContextMenuItem(logRanks);
	}

	@Override
	public boolean supports(EDataDomainQueryMode mode) {
		return mode == EDataDomainQueryMode.TABLE_BASED;
	}

	public static class LogRankMetric extends DefaultComputedGroupScore {
		private final Integer clinicalVariable;

		public LogRankMetric(String label, final Integer clinicalVariable) {
			super(label, new IGroupAlgorithm() {
				final ATableBasedDataDomain clinical = DataDomainOracle.getClinicalDataDomain();
				final IGroupAlgorithm underlying = LogRank.get(clinicalVariable, clinical);

				@Override
				public IDType getTargetType(ARecordPerspective a, ARecordPerspective b) {
					return underlying.getTargetType(a, b);
				}

				@Override
				public String getAbbreviation() {
					return underlying.getAbbreviation();
				}

				@Override
				public float compute(Set<Integer> a, Set<Integer> b) {
					// me versus the rest
					return underlying.compute(a, Sets.difference(b, a));
				}
			}, null);
			this.clinicalVariable = clinicalVariable;
		}

		public Integer getClinicalVariable() {
			return clinicalVariable;
		}
	}

	public static class LogRankPValue extends DefaultLabelProvider implements IRegisteredScore {
		private final LogRankMetric logRankScore;

		public LogRankPValue(String label, LogRankMetric logRankScore) {
			super(label);
			this.logRankScore = logRankScore;
		}

		@Override
		public void onRegistered() {

		}

		@Override
		public ESorting getDefaultSorting() {
			return ESorting.ASC;
		}

		@Override
		public String getAbbreviation() {
			return "LR-P";
		}

		@Override
		public boolean supports(EDataDomainQueryMode mode) {
			return mode == EDataDomainQueryMode.TABLE_BASED;
		}

		@Override
		public EScoreType getScoreType() {
			return logRankScore.getScoreType();
		}

		@Override
		public float getScore(ScoringElement elem) {
			return LogRank.getPValue(logRankScore.getScore(elem));
		}

		public Integer getClinicalVariable() {
			return logRankScore.getClinicalVariable();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((logRankScore == null) ? 0 : logRankScore.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LogRankPValue other = (LogRankPValue) obj;
			if (logRankScore == null) {
				if (other.logRankScore != null)
					return false;
			} else if (!logRankScore.equals(other.logRankScore))
				return false;
			return true;
		}
	}
}

