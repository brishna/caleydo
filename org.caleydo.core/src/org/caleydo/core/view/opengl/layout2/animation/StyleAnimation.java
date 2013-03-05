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
package org.caleydo.core.view.opengl.layout2.animation;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.Durations.IDuration;
import org.caleydo.core.view.opengl.layout2.animation.StyleAnimations.IStyleAnimation;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author Samuel Gratzl
 *
 */
public class StyleAnimation extends AElementAnimation {
	private final IStyleAnimation anim;

	public StyleAnimation(int startIn, IDuration duration, IGLLayoutElement animated, IStyleAnimation anim) {
		super(startIn, duration, animated);
		this.anim = anim;
	}

	/**
	 * performs the animation
	 *
	 * @param delta
	 *            between last call in ms
	 * @return whether this animation ended
	 */
	public boolean apply(GLGraphics g, int delta) {
		if (startIn >= 0) {
			startIn -= delta;
			if (startIn <= 0) {
				delta = -startIn;
				startIn = -1;
				animate(g, 0.0f);
			} else
				return false;
		}
		if (delta < 1)
			return false;
		remaining -= delta;
		float alpha = 0;
		if (remaining <= 0) { // last one
			animate(g, 1.f);
		} else {
			alpha = 1 - (remaining / (float) durationValue);
			animate(g, alpha);
		}
		return remaining <= 0;
	}

	public void animate(GLGraphics g, float alpha) {
		GLElement elem = getAnimatedElement();
		if (!isRunning())
			elem.render(g);
		else
			anim.render(elem, g, alpha);
	}

	@Override
	public EAnimationType getType() {
		return EAnimationType.STYLE;
	}

}
