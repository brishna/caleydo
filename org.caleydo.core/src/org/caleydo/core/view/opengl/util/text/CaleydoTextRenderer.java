/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.view.opengl.util.text;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;

/**
 * Wrapper for TextRenderer that provides methods to draw text with a specified minimum size (no matter what's the
 * current size of the view).
 *
 * @author Christian Partl
 * @author Alexander Lex
 */
public class CaleydoTextRenderer extends MyTextRenderer implements ITextRenderer {
	private static final String FONT_NAME = "Arial";
	static private final String REFERENCE_TEXT = "Reference Text";
	float fontScaling = GeneralRenderStyle.SMALL_FONT_SCALING_FACTOR;

	private final Rectangle2D referenceBounds;
	private final double refHeight;

	/**
	 * Constructor.
	 *
	 * @param font
	 * @param antialiased
	 * @param useFractionalMetrics
	 */
	public CaleydoTextRenderer(Font font) {
		super(font, true, true, new DefaultRenderDelegate());
		referenceBounds = super.getBounds(REFERENCE_TEXT);

		this.refHeight = super.getBounds("Sgfy").getHeight();
	}

	public CaleydoTextRenderer(int size) {
		this(size, Font.PLAIN);
	}

	public CaleydoTextRenderer(int size, int style) {
		super(new Font(FONT_NAME, style, size), true, true, new DefaultRenderDelegate());
		referenceBounds = super.getBounds(REFERENCE_TEXT);
		setUseVertexArrays(false);
		this.refHeight = super.getBounds("Sgfy").getHeight();
	}

	@Override
	public boolean isOriginTopLeft() {
		return false;
	}

	/**
	 * Convenience method to render text with a specified minimum size without having to call begin3DRendering and
	 * end3DRendering.
	 *
	 * @param gl
	 *            GL2 context.
	 * @param text
	 *            Text to render
	 * @param x
	 *            X coordinate of the text.
	 * @param y
	 *            Y coordinate of the text.
	 * @param z
	 *            Z coordinate of the text.
	 * @param scaling
	 *            Factor the text is scaled with.
	 * @param minSize
	 *            Minimum size of the text. Note that the minimum size is scaled with the specified scaling vector.
	 */
	public void renderText(GL2 gl, String text, float x, float y, float z, float scaling, int minSize) {

		scaling = calculateScaling(gl, scaling, minSize);

		begin3DRendering();
		draw3D(text, x, y, z, scaling);
		flush();
		end3DRendering();
	}

	/**
	 * Renders text with a specified minimum size. Use this only if you want to render several instances at a time. If
	 * you have only one string, use {@link #renderText(GL2, String, float, float, float, float, int)} instead.
	 *
	 * @param gl
	 *            GL2 context.
	 * @param text
	 *            Text to render
	 * @param x
	 *            X coordinate of the text.
	 * @param y
	 *            Y coordinate of the text.
	 * @param z
	 *            Z coordinate of the text.
	 * @param scaling
	 *            Factor the text is scaled with.
	 * @param minSize
	 *            Minimum size of the text. Note that the minimum size is scaled with the specified scaling vector.
	 */
	public void draw3D(GL2 gl, String text, float x, float y, float z, float scaling, int minSize) {

		scaling = calculateScaling(gl, scaling, minSize);

		draw3D(text, x, y, z, scaling);
	}

	/**
	 * Gets scaled bounds of the specified text according to the specified parameters.
	 *
	 * @param gl
	 *            GL2 context.
	 * @param text
	 *            Text to calculate the bounds for.
	 * @param scaling
	 *            Scaling of the text.
	 * @param minSize
	 *            Minimum size of the text. Note that the bound's size is therefore dependent on the size of the current
	 *            viewport.
	 * @return Scaled bounds of the specified text.
	 */
	public Rectangle2D getScaledBounds(GL2 gl, String text, float scaling, int minSize) {

		scaling = calculateScaling(gl, scaling, minSize);

		Rectangle2D rect = super.getBounds(text);
		rect.setRect(rect.getX(), rect.getY(), rect.getWidth() * scaling, rect.getHeight() * scaling);

		return rect;
	}

	/**
	 * Calculates the scaling factor taking the minimum text size into consideration.
	 *
	 * @param gl
	 *            GL2 context.
	 * @param scaling
	 *            Normal scaling of the text.
	 * @param minSize
	 *            Minimum text size.
	 * @return Scaling considering the minimum text size.
	 */
	private float calculateScaling(GL2 gl, float scaling, int minSize) {
		IntBuffer buffer = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL.GL_VIEWPORT, buffer);
		int currentWidth = buffer.get(2);

		float referenceWidth = minSize / (float) referenceBounds.getHeight() * 500.0f;

		if (referenceWidth > currentWidth)
			scaling = scaling * referenceWidth / currentWidth;

		return scaling;
	}

	Rectangle2D getReferenceBounds() {
		return referenceBounds;
	}

	public float getReferenceHeight() {
		return (float) referenceBounds.getHeight();
	}

	@Override
	public void renderTextInBounds(GL2 gl, String text, float x, float y, float z, float w, float h) {
		renderTextInBounds(gl, text, x, y, z, w, h, true);
	}

	@Override
	public void renderTextInBounds(GL2 gl, String text, float x, float y, float z, float w, float h, boolean alignRight) {

		// we use the height of a standard string so we don't have varying
		// height
		double scaling = h / refHeight;

		Rectangle2D boundsForWidth = super.getBounds(text);
		double requiredWidth = boundsForWidth.getWidth() * scaling;
		if (requiredWidth > w) {
			double truncateFactor = w / requiredWidth;

			if (truncateFactor < 0)
				truncateFactor = 0;

			int length = Math.min((int) (text.length() * truncateFactor), text.length());
			if (length >= 0)
				text = text.substring(0, length);

			Rectangle2D checkedBounds = super.getBounds(text);
			if (w < checkedBounds.getWidth() * scaling && text.length() > 0) {
				text = text.substring(0, length - 1);
			}
		}

		begin3DRendering();
		if (!alignRight) {
			boundsForWidth = super.getBounds(text);
			x -= boundsForWidth.getWidth() * scaling;
		}
		draw3D(text, x, y, z, (float) scaling);
		flush();
		end3DRendering();
	}

	/**
	 * Render the text at the position specified (lower left corner) with the specified rotation within the bounding
	 * box. The height is scaled to fit, the string is truncated to fit the width.
	 *
	 * @param gl
	 * @param text
	 * @param xPosition
	 *            x of lower left corner
	 * @param yPosition
	 *            y of lower left corner
	 * @param zPositon
	 * @param width
	 *            width fo the bounding box
	 * @param height
	 *            height of the bounding box
	 * @param rotationAngle
	 *            rotation angle in degrees
	 */
	public void renderRotatedTextInBounds(GL2 gl, String text, float xPosition, float yPosition, float zPositon,
			float width, float height, float rotationAngle) {

		// we use the height of a standard string so we don't have varying
		// height
		double scaling = height / refHeight;

		Rectangle2D boundsForWidth = super.getBounds(text);
		double requiredWidth = boundsForWidth.getWidth() * scaling;
		if (requiredWidth > width) {
			double truncateFactor = width / requiredWidth;
			int length = (int) (text.length() * truncateFactor);
			if (length >= 0)
				text = text.substring(0, length);

			Rectangle2D checkedBounds = super.getBounds(text);
			if (width < checkedBounds.getWidth() * scaling && text.length() > 0)
				text = text.substring(0, length - 1);
		}

		gl.glPushMatrix();
		gl.glTranslatef(xPosition, yPosition, zPositon);
		gl.glRotatef(rotationAngle, 0, 0, 1);

		begin3DRendering();
		draw3D(text, 0, 0, 0, (float) scaling);
		flush();
		end3DRendering();

		gl.glPopMatrix();
	}

	/**
	 * Calculates the required width of a text with a specified height.
	 *
	 * @param text
	 * @param height
	 * @return Required width of the text
	 */
	public float getRequiredTextWidth(String text, float height) {

		// we use the height of a standard string so we don't have varying
		// height
		double scaling = height / refHeight;

		Rectangle2D boundsForWidth = super.getBounds(text);
		// double requiredWidth = boundsForWidth.getWidth() * scaling;

		// Rectangle2D bounds = super.getBounds(text);
		//
		// double scaling = height / bounds.getHeight();

		return (float) (boundsForWidth.getWidth() * scaling);
	}

	/**
	 * Same as {@link #getRequiredTextWidth(String, float)}, but returns the specified maximum width if the required
	 * text width exceeds this maximum.
	 *
	 * @param text
	 * @param height
	 * @param maxWidth
	 * @return
	 */
	public float getRequiredTextWidthWithMax(String text, float height, float maxWidth) {

		float requiredWidth = getRequiredTextWidth(text, height);
		return (requiredWidth > maxWidth) ? maxWidth : requiredWidth;
	}

	@Override
	public float getTextWidth(String text, float height) {
		return getRequiredTextWidth(text, height);
	}

	/**
	 * Set the color of the text
	 *
	 * @param color
	 */
	@Override
	public void setColor(Color color) {
		setColor(color.r, color.g, color.b, color.a);
	}
}
