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
package org.caleydo.vis.rank.config;

/**
 * @author Samuel Gratzl
 *
 */
public class RankTableUIConfigs {

	public static final IRankTableUIConfig DEFAULT = new RankTableUIConfigBase(true, true, true);

	public static IRankTableUIConfig nonInteractive(IRankTableUIConfig config) {
		return new WrappedRankTableUIConfig(config, Boolean.FALSE, null, null);
	}

	private static class WrappedRankTableUIConfig implements IRankTableUIConfig {
		private final IRankTableUIConfig wrappee;
		private final Boolean isInteractive;
		private final Boolean isMoveAble;
		private final Boolean canChangeWeights;

		public WrappedRankTableUIConfig(IRankTableUIConfig wrappee, Boolean isInteractive, Boolean isMoveAble,
				Boolean canChangeWeights) {
			this.wrappee = wrappee;
			this.isInteractive = isInteractive;
			this.isMoveAble = isMoveAble;
			this.canChangeWeights = canChangeWeights;
		}

		@Override
		public boolean isInteractive() {
			if (isInteractive != null)
				return isInteractive.booleanValue();
			return wrappee.isInteractive();
		}

		@Override
		public boolean isMoveAble() {
			if (isMoveAble != null)
				return isMoveAble.booleanValue();
			return wrappee.isMoveAble();
		}

		@Override
		public boolean canChangeWeights() {
			if (canChangeWeights != null)
				return canChangeWeights.booleanValue();
			return wrappee.canChangeWeights();
		}

	}

}
