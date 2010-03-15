package org.caleydo.view.heatmap.heatmap;

/**
 * Every ARenderer renders from (0, 0) to (x, y). An ARenderer does not take
 * care of any spacings on the sides.
 * 
 * @author Alexander Lex
 * 
 */
public abstract class ARenderer {
	protected float x;
	protected float y;

	public void setLimits(float x, float y) {
		this.x = x;
		this.y = y;
	}

}
