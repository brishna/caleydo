/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.core.data.collection;

import gleem.linalg.Vec2f;

import java.awt.Dimension;

import org.caleydo.core.util.collection.Pair;

/**
 * @author Samuel Gratzl
 *
 */
public enum EDimension {
	RECORD, DIMENSION;

	public boolean isRecord() {
		return this == RECORD;
	}

	public boolean isDimension() {
		return !this.isRecord();
	}

	public EDimension opposite() {
		return get(this != DIMENSION);
	}

	public static EDimension get(boolean dimension) {
		return dimension ? DIMENSION : RECORD;
	}

	public boolean isHorizontal() {
		return isDimension();
	}

	public boolean isVertical() {
		return isRecord();
	}

	public boolean select(boolean dim, boolean rec) {
		return this == DIMENSION ? dim : rec;
	}

	public float select(float dim, float rec) {
		return this == DIMENSION ? dim : rec;
	}

	public int select(int dim, int rec) {
		return this == DIMENSION ? dim : rec;
	}

	public float select(Vec2f xy) {
		return this == DIMENSION ? xy.x() : xy.y();
	}

	public int select(Dimension wh) {
		return this == DIMENSION ? wh.width : wh.height;
	}

	public <T> T select(Pair<T, T> pair) {
		return this == DIMENSION ? pair.getFirst() : pair.getSecond();
	}

	public double select(double dim, double rec) {
		return this == DIMENSION ? dim : rec;
	}

	public <T> T select(T dim, T rec) {
		return this == DIMENSION ? dim : rec;
	}
}
