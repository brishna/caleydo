/**
 *
 */
package org.caleydo.view.enroute.path.node.mode;

import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.util.IColorProvider;
import org.caleydo.view.enroute.SelectionColorCalculator;
import org.caleydo.view.enroute.path.APathwayPathRenderer;
import org.caleydo.view.enroute.path.node.GeneNode;

/**
 * Base class for modes of a {@link GeneNode}.
 *
 * @author Christian
 *
 */
public abstract class AGeneNodeMode extends ALayoutBasedNodeMode implements IColorProvider {

	/**
	 * Second color that is used to show a color gradient made up of combined selection colors.
	 */
	protected float[] gradientColor = null;

	SelectionColorCalculator colorCalculator;

	/**
	 * @param view
	 */
	public AGeneNodeMode(AGLView view, APathwayPathRenderer pathwayPathRenderer) {
		super(view, pathwayPathRenderer);
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		colorCalculator = new SelectionColorCalculator(new Color(DEFAULT_BACKGROUND_COLOR));
	}

	@Override
	public void render(GL2 gl, GLU glu) {
		// determineBackgroundColor(pathwayPathRenderer.getGeneSelectionManager());
		super.render(gl, glu);
	}

	@Override
	protected boolean determineHighlightColor() {
		boolean highlight = false;
		EventBasedSelectionManager selectionManager = pathwayPathRenderer.getGeneSelectionManager();
		for (Integer davidId : node.getMappedDavidIDs()) {
			List<SelectionType> selectionTypes = selectionManager.getSelectionTypes(davidId);
			if (selectionTypes.contains(SelectionType.SELECTION)) {
				highlightColor = SelectionType.SELECTION.getColor();
				return true;
			} else if (selectionTypes.contains(SelectionType.MOUSE_OVER)) {
				highlightColor = SelectionType.MOUSE_OVER.getColor();
				highlight = true;
			}

		}
		return highlight;

		// Collections.sort(allSelectionTypes);
		// Collections.reverse(allSelectionTypes);
		// colorCalculator.calculateColors(allSelectionTypes);
		// backgroundColor = colorCalculator.getPrimaryColor().getRGBA();
		// gradientColor = colorCalculator.getSecondaryColor().getRGBA();
	}

	@Override
	public float[] getColor() {
		return (gradientColor != null) ? gradientColor : backgroundColor;
	}

	@Override
	public float[] getGradientColor() {
		return (gradientColor != null) ? backgroundColor : gradientColor;
	}

	@Override
	public boolean useGradient() {
		return gradientColor != null;
	}

	@Override
	public boolean isHorizontalGradient() {
		return false;
	}

}
