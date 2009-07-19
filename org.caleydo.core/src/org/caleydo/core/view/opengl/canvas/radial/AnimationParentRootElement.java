package org.caleydo.core.view.opengl.canvas.radial;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.util.mapping.color.ColorMapping;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.EColorMappingType;

/**
 * This class represents the animation where the parent of the current root element becomes the new root
 * element. When the animation is finished the follow up drawing state ({@link DrawingStateFullHierarchy})
 * will become active.
 * 
 * @author Christian Partl
 */
public class AnimationParentRootElement
	extends ADrawingStateAnimation {

	public static final float DEFAULT_ANIMATION_DURATION = 0.35f;

	private MovementValue mvCurrentStartAngle;
	private MovementValue mvCurrentAngle;
	private MovementValue mvCurrentWidth;
	private MovementValue mvCurrentInnerRadius;
	private MovementValue mvCurrentSelectedColorR;
	private MovementValue mvCurrentSelectedColorG;
	private MovementValue mvCurrentSelectedColorB;
	private int iTargetDepth;

	private PDDrawingStrategyFixedColor dsFixedColor;

	/**
	 * Constructor.
	 * 
	 * @param drawingController
	 *            DrawingController that holds the drawing states.
	 * @param radialHierarchy
	 *            GLRadialHierarchy instance that is used.
	 * @param navigationHistory
	 *            NavigationHistory instance that shall be used.
	 */
	public AnimationParentRootElement(DrawingController drawingController, GLRadialHierarchy radialHierarchy,
		NavigationHistory navigationHistory) {

		super(drawingController, radialHierarchy, navigationHistory);
		fAnimationDuration = DEFAULT_ANIMATION_DURATION;
	}

	@Override
	public void draw(float fXCenter, float fYCenter, GL gl, GLU glu, double dTimePassed) {
		PartialDisc pdCurrentSelectedElement = radialHierarchy.getCurrentSelectedElement();

		if (!bAnimationStarted) {
			initAnimation(fXCenter, fYCenter, pdCurrentSelectedElement);
			bAnimationStarted = true;
			radialHierarchy.setAnimationActive(true);
		}

		moveValues(dTimePassed);

		gl.glLoadIdentity();
		gl.glTranslatef(fXCenter, fYCenter, 0);

		dsFixedColor.setFillColor(mvCurrentSelectedColorR.getMovementValue(), mvCurrentSelectedColorG
			.getMovementValue(), mvCurrentSelectedColorB.getMovementValue(), 1);

		pdCurrentSelectedElement.drawHierarchyAngular(gl, glu, mvCurrentWidth.getMovementValue(),
			iTargetDepth, mvCurrentStartAngle.getMovementValue(), mvCurrentAngle.getMovementValue(),
			mvCurrentInnerRadius.getMovementValue());

		if (haveMovementValuesReachedTargets()) {
			bAnimationStarted = false;
		}

		if (!bAnimationStarted) {
			ADrawingState dsNext =
				drawingController.getDrawingState(DrawingController.DRAWING_STATE_FULL_HIERARCHY);

			drawingController.setDrawingState(dsNext);
			radialHierarchy.setAnimationActive(false);
			PartialDisc pdNewRootElement = pdCurrentSelectedElement.getParent();
			radialHierarchy.setCurrentRootElement(pdNewRootElement);
			radialHierarchy.setCurrentMouseOverElement(pdNewRootElement);
			radialHierarchy.setCurrentSelectedElement(pdNewRootElement);

			navigationHistory.addNewHistoryEntry(dsNext, pdNewRootElement, pdNewRootElement, radialHierarchy
				.getMaxDisplayedHierarchyDepth());
			radialHierarchy.setDisplayListDirty();
		}

	}

	/**
	 * Initializes the animation, particularly initializes all movement values needed for the animation.
	 * 
	 * @param fXCenter
	 *            X coordinate of the hierarchy's center.
	 * @param fYCenter
	 *            Y coordinate of the hierarchy's center.
	 * @param pdCurrentSelectedElement
	 *            Currently selected partial disc.
	 */
	private void initAnimation(float fXCenter, float fYCenter, PartialDisc pdCurrentSelectedElement) {

		float fCurrentAngle = pdCurrentSelectedElement.getCurrentAngle();
		float fCurrentInnerRadius = pdCurrentSelectedElement.getCurrentInnerRadius();
		float fCurrentStartAngle = pdCurrentSelectedElement.getCurrentStartAngle();
		float fCurrentWidth = pdCurrentSelectedElement.getCurrentWidth();

		PartialDisc pdNewRootElement = pdCurrentSelectedElement.getParent();

		int iDisplayedHierarchyDepth =
			Math.min(radialHierarchy.getMaxDisplayedHierarchyDepth(), pdNewRootElement.getHierarchyDepth());

		float fTargetWidth =
			Math.min(fXCenter * RadialHierarchyRenderStyle.USED_SCREEN_PERCENTAGE, fYCenter
				* RadialHierarchyRenderStyle.USED_SCREEN_PERCENTAGE)
				/ (float) iDisplayedHierarchyDepth;

		pdNewRootElement.simulateDrawHierarchyFull(fTargetWidth, iDisplayedHierarchyDepth);

		iTargetDepth = pdCurrentSelectedElement.getCurrentDepth();
		float fTargetAngle = pdCurrentSelectedElement.getCurrentAngle();
		float fSimulatedStartAngle = pdCurrentSelectedElement.getCurrentStartAngle();
		float fTargetInnerRadius = pdCurrentSelectedElement.getCurrentInnerRadius();

		float fCurrentMidAngle = fCurrentStartAngle + (fCurrentAngle / 2.0f);
		while (fCurrentMidAngle > 360) {
			fCurrentMidAngle -= 360;
		}
		while (fCurrentMidAngle < 0) {
			fCurrentMidAngle += 360;
		}

		float fSimulatedMidAngle = fSimulatedStartAngle + (fTargetAngle / 2.0f);
		while (fSimulatedMidAngle > 360) {
			fSimulatedMidAngle -= 360;
		}
		while (fSimulatedMidAngle < 0) {
			fSimulatedMidAngle += 360;
		}
		float fDeltaStartAngle = fCurrentMidAngle - fSimulatedMidAngle;

		pdNewRootElement.setCurrentStartAngle(pdNewRootElement.getCurrentStartAngle() + fDeltaStartAngle);
		float fTargetStartAngle = fCurrentMidAngle - (fTargetAngle / 2.0f);

		while (fTargetStartAngle < fCurrentStartAngle) {
			fTargetStartAngle += 360;
		}

		float fArRGB[];
		if (DrawingStrategyManager.get().getDefaultDrawingStrategy().getDrawingStrategyType() == EPDDrawingStrategyType.RAINBOW_COLOR) {
			ColorMapping cmRainbow = ColorMappingManager.get().getColorMapping(EColorMappingType.RAINBOW);
			fArRGB = cmRainbow.getColor(fCurrentMidAngle / 360);
		}
		else {
			ColorMapping cmExpression =
				ColorMappingManager.get().getColorMapping(EColorMappingType.GENE_EXPRESSION);
			fArRGB = cmExpression.getColor(pdCurrentSelectedElement.getAverageExpressionValue());
		}

		alMovementValues.clear();

		mvCurrentAngle = createNewMovementValue(fCurrentAngle, fTargetAngle, fAnimationDuration);
		mvCurrentStartAngle =
			createNewMovementValue(fCurrentStartAngle, fTargetStartAngle, fAnimationDuration);
		mvCurrentInnerRadius =
			createNewMovementValue(fCurrentInnerRadius, fTargetInnerRadius, fAnimationDuration);
		mvCurrentWidth = createNewMovementValue(fCurrentWidth, fTargetWidth, fAnimationDuration);
		mvCurrentSelectedColorR =
			createNewMovementValue(RadialHierarchyRenderStyle.PARTIAL_DISC_ROOT_COLOR[0], fArRGB[0],
				fAnimationDuration);
		mvCurrentSelectedColorG =
			createNewMovementValue(RadialHierarchyRenderStyle.PARTIAL_DISC_ROOT_COLOR[1], fArRGB[1],
				fAnimationDuration);
		mvCurrentSelectedColorB =
			createNewMovementValue(RadialHierarchyRenderStyle.PARTIAL_DISC_ROOT_COLOR[2], fArRGB[2],
				fAnimationDuration);

		dsFixedColor =
			(PDDrawingStrategyFixedColor) DrawingStrategyManager.get().getDrawingStrategy(
				EPDDrawingStrategyType.FIXED_COLOR);

		pdCurrentSelectedElement.setPDDrawingStrategy(dsFixedColor);
	}
}
