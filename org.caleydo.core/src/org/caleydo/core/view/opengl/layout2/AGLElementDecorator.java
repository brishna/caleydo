/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.view.opengl.layout2;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 *
 * @author Samuel Gratzl
 *
 */
public abstract class AGLElementDecorator extends GLElement implements IGLElementParent {
	protected GLElement content;

	public AGLElementDecorator() {
	}

	public AGLElementDecorator(GLElement content) {
		setContent(content);
	}

	/**
	 * @param content
	 *            setter, see {@link content}
	 */
	public void setContent(GLElement content) {
		if (this.content == content)
			return;
		if (this.content != null) {
			this.content.setParent(null);
			if (context != null)
				this.content.takeDown();
		}
		this.content = content;
		if (this.content != null) {
			this.content.setParent(this);
			if (context != null)
				this.content.init(context);
		}
		relayout();
	}

	/**
	 * @return the content, see {@link #content}
	 */
	public final GLElement getContent() {
		return content;
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		if (content != null) {
			content.setParent(this);
			content.init(context);
		}
	}

	@Override
	protected void takeDown() {
		if (content != null)
			content.takeDown();
		super.takeDown();
	}

	@Override
	protected boolean hasPickAbles() {
		return true;
	}

	@Override
	public void layout(int deltaTimeMs) {
		super.layout(deltaTimeMs);
		if (content != null)
			content.layout(deltaTimeMs);
	}

	@Override
	protected void layoutImpl(int deltaTimeMs) {
		if (content != null) {
			Vec2f size = getSize();
			layoutContent(content.layoutElement, size.x(), size.y(),deltaTimeMs);
		}
		super.layoutImpl(deltaTimeMs);
	}

	protected abstract void layoutContent(IGLLayoutElement content, float w, float h, int deltaTimeMs);

	@Override
	public boolean moved(GLElement child) {
		return false;
	}

	@Override
	public final <P, R> R accept(IGLElementVisitor<P, R> visitor, P para) {
		return visitor.visit(this, para);
	}
}
