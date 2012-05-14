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
package org.caleydo.view.kaplanmeier;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.InvalidAttributeValueException;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;

import org.caleydo.core.data.collection.dimension.DataRepresentation;
import org.caleydo.core.data.selection.ElementConnectionInformation;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.virtualarray.DimensionVirtualArray;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDType;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorManager;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.connectionline.ConnectionLineRenderer;
import org.caleydo.core.view.opengl.util.connectionline.LineCrossingRenderer;
import org.caleydo.core.view.opengl.util.connectionline.LineLabelRenderer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.view.kaplanmeier.renderstyle.KaplanMeierRenderStyle;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>
 * Kaplan Meier GL2 view.
 * </p>
 * <p>
 * TODO
 * </p>
 * 
 * @author Marc Streit
 */

public class GLKaplanMeier extends ATableBasedView {
	public static String VIEW_TYPE = "org.caleydo.view.kaplanmeier";

	public static String VIEW_NAME = "Kaplan-Meier Plot";

	protected static final int BOTTOM_LEFT_AXIS_SPACING_PIXELS = 50;
	protected static final int TOP_RIGHT_AXIS_SPACING_PIXELS = 8;

	private KaplanMeierRenderStyle renderStyle;

	private SelectionManager recordGroupSelectionManager;

	/**
	 * Constructor.
	 * 
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 */
	public GLKaplanMeier(GLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);

		textRenderer = new CaleydoTextRenderer(24);
	}

	@Override
	public void initialize() {
		super.initialize();
		recordGroupSelectionManager = dataDomain.getRecordGroupSelectionManager().clone();
	}

	@Override
	public void init(GL2 gl) {
		displayListIndex = gl.glGenLists(1);
		renderStyle = new KaplanMeierRenderStyle(viewFrustum);

		super.renderStyle = renderStyle;
		detailLevel = EDetailLevel.HIGH;
	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

		this.glMouseListener = glMouseListener;

		init(gl);
	}

	@Override
	public void displayLocal(GL2 gl) {
		pickingManager.handlePicking(this, gl);
		display(gl);
		if (busyState != EBusyState.OFF) {
			renderBusyMode(gl);
		}
	}

	@Override
	public void displayRemote(GL2 gl) {
		display(gl);
	}

	@Override
	public void display(GL2 gl) {

		renderKaplanMeierCurve(gl);

		checkForHits(gl);
	}

	private void renderKaplanMeierCurve(final GL2 gl) {

		RecordVirtualArray recordVA = dataContainer.getRecordPerspective()
				.getVirtualArray();

		// do not fill curve if multiple curves are rendered in this plot
		boolean fillCurve = recordVA.getGroupList().size() > 1 ? false : true;

		List<Color> colors = ColorManager.get().getColorList(
				ColorManager.QUALITATIVE_COLORS);
		for (Group group : recordVA.getGroupList()) {
			List<Integer> recordIDs = recordVA.getIDsOfGroup(group.getGroupIndex());

			int colorIndex = 0;
			if (dataContainer.getRecordGroup() != null)
				colorIndex = dataContainer.getRecordGroup().getGroupIndex();
			else
				colorIndex = group.getGroupIndex();

			// We only have 10 colors in the diverging color map
			colorIndex = colorIndex % 10;

			int lineWidth = 1;
			if (recordGroupSelectionManager.getElements(SelectionType.SELECTION).size() == 1
					&& (Integer) recordGroupSelectionManager.getElements(
							SelectionType.SELECTION).toArray()[0] == group.getID()) {
				lineWidth = 2;
			}
			// else
			// fillCurve = false;

			if (detailLevel == EDetailLevel.HIGH)
				lineWidth *= 2;

			gl.glLineWidth(lineWidth);

			renderSingleKaplanMeierCurve(gl, recordIDs, colors.get(colorIndex), fillCurve);
		}

		if (detailLevel == EDetailLevel.HIGH) {
			renderAxes(gl);
		}
	}

	private void renderAxes(GL2 gl) {

		float plotHeight = viewFrustum.getHeight()
				- pixelGLConverter
						.getGLHeightForPixelHeight(BOTTOM_LEFT_AXIS_SPACING_PIXELS
								+ TOP_RIGHT_AXIS_SPACING_PIXELS);
		float plotWidth = viewFrustum.getWidth()
				- pixelGLConverter
						.getGLWidthForPixelWidth(BOTTOM_LEFT_AXIS_SPACING_PIXELS
								+ TOP_RIGHT_AXIS_SPACING_PIXELS);
		float originX = pixelGLConverter
				.getGLWidthForPixelWidth(BOTTOM_LEFT_AXIS_SPACING_PIXELS);
		float originY = pixelGLConverter
				.getGLHeightForPixelHeight(BOTTOM_LEFT_AXIS_SPACING_PIXELS);

		float axisLabelWidth = textRenderer.getRequiredTextWidthWithMax("Axis label 1",
				pixelGLConverter.getGLHeightForPixelHeight(20), viewFrustum.getWidth());

		textRenderer
				.renderTextInBounds(gl, "Time (Days)", viewFrustum.getWidth() / 2.0f
						- axisLabelWidth / 2.0f,
						pixelGLConverter.getGLHeightForPixelHeight(5), 0,
						viewFrustum.getWidth(),
						pixelGLConverter.getGLHeightForPixelHeight(20));
		
		axisLabelWidth = textRenderer.getRequiredTextWidthWithMax("Number of Patients",
				pixelGLConverter.getGLHeightForPixelHeight(20), viewFrustum.getWidth());

		textRenderer.renderRotatedTextInBounds(gl, "Number of Patients",
				pixelGLConverter.getGLHeightForPixelHeight(25), viewFrustum.getHeight()
						/ 2.0f - axisLabelWidth / 2.0f, 0, viewFrustum.getWidth(),
				pixelGLConverter.getGLHeightForPixelHeight(20), 90);

		List<Vec3f> xAxisLinePoints = new ArrayList<Vec3f>();
		xAxisLinePoints.add(new Vec3f(originX, originY, 0));
		xAxisLinePoints.add(new Vec3f(originX + plotWidth, originY, 0));
		ConnectionLineRenderer xAxis = new ConnectionLineRenderer();
		LineCrossingRenderer lineCrossingRenderer = new LineCrossingRenderer(0.5f,
				pixelGLConverter);
		LineLabelRenderer lineLabelRenderer = new LineLabelRenderer(0.5f,
				pixelGLConverter, "200", textRenderer);
		lineLabelRenderer.setLineOffsetPixels(-16);
		lineLabelRenderer.setCentered(true);
		lineCrossingRenderer.setLineWidth(2);
		xAxis.addAttributeRenderer(lineCrossingRenderer);
		xAxis.addAttributeRenderer(lineLabelRenderer);
		xAxis.setLineWidth(2);
		xAxis.renderLine(gl, xAxisLinePoints);

		List<Vec3f> yAxisLinePoints = new ArrayList<Vec3f>();
		yAxisLinePoints.add(new Vec3f(originX, originY, 0));
		yAxisLinePoints.add(new Vec3f(originX, originY + plotHeight, 0));
		ConnectionLineRenderer yAxis = new ConnectionLineRenderer();
		yAxis.setLineWidth(2);
		yAxis.renderLine(gl, yAxisLinePoints);

	}

	private void renderSingleKaplanMeierCurve(GL2 gl, List<Integer> recordIDs,
			Color color, boolean fillCurve) {

		// if (recordIDs.size() == 0)
		// return;
		DimensionVirtualArray dimensionVA = dataContainer.getDimensionPerspective()
				.getVirtualArray();

		ArrayList<Float> dataVector = new ArrayList<Float>();
		Float maxValue = Float.MIN_VALUE;

		for (int recordID = 0; recordID < recordIDs.size(); recordID++) {
			float normalizedValue = dataContainer
					.getDataDomain()
					.getTable()
					.getFloat(DataRepresentation.NORMALIZED, recordIDs.get(recordID),
							dimensionVA.get(0));
			dataVector.add(normalizedValue);

			float rawValue = dataContainer
					.getDataDomain()
					.getTable()
					.getFloat(DataRepresentation.RAW, recordIDs.get(recordID),
							dimensionVA.get(0));
			if (rawValue != Float.NaN && rawValue > maxValue)
				maxValue = rawValue;
		}
		Float[] sortedDataVector = new Float[dataVector.size()];
		dataVector.toArray(sortedDataVector);
		Arrays.sort(sortedDataVector);
		dataVector.clear();

		// move sorted data back to array list so that we can use it as a stack
		for (int index = 0; index < recordIDs.size(); index++) {
			dataVector.add(sortedDataVector[index]);
		}

		if (fillCurve) {
			// We cannot use transparency here because of artefacts. Hence, we
			// need
			// to brighten the color by multiplying it with a factor
			gl.glColor3f(color.r * 1.3f, color.g * 1.3f, color.b * 1.3f);
			drawFilledCurve(gl, maxValue, dataVector);

			dataVector.clear();
			// move sorted data back to array list so that we can use it as a
			// stack
			for (int index = 0; index < recordIDs.size(); index++) {
				dataVector.add(sortedDataVector[index]);
			}

			gl.glColor3fv(color.getRGB(), 0);
			drawCurve(gl, maxValue, dataVector);
		} else {
			gl.glColor3fv(color.getRGB(), 0);
			drawCurve(gl, maxValue, dataVector);
		}
	}

	private void drawFilledCurve(GL2 gl, float maxValue, ArrayList<Float> dataVector) {

		float plotHeight = viewFrustum.getHeight()
				- (detailLevel == EDetailLevel.HIGH ? pixelGLConverter
						.getGLHeightForPixelHeight(BOTTOM_LEFT_AXIS_SPACING_PIXELS
								+ TOP_RIGHT_AXIS_SPACING_PIXELS) : 0);
		float plotWidth = viewFrustum.getWidth()
				- (detailLevel == EDetailLevel.HIGH ? pixelGLConverter
						.getGLWidthForPixelWidth(BOTTOM_LEFT_AXIS_SPACING_PIXELS
								+ TOP_RIGHT_AXIS_SPACING_PIXELS) : 0);
		float axisSpacing = (detailLevel == EDetailLevel.HIGH ? pixelGLConverter
				.getGLWidthForPixelWidth(BOTTOM_LEFT_AXIS_SPACING_PIXELS) : 0);

		float TIME_BINS = maxValue;

		float timeBinStepSize = 1 / TIME_BINS;
		float currentTimeBin = 0;

		int remainingItemCount = dataVector.size();
		float ySingleSampleSize = plotHeight / dataVector.size();

		for (int binIndex = 0; binIndex < TIME_BINS; binIndex++) {

			while (dataVector.size() > 0 && dataVector.get(0) <= currentTimeBin) {
				dataVector.remove(0);
				remainingItemCount--;
			}

			float y = (float) remainingItemCount * ySingleSampleSize;
			gl.glBegin(GL2.GL_LINE_STRIP);
			gl.glVertex3f(axisSpacing + currentTimeBin * plotWidth, axisSpacing, 0);
			gl.glVertex3f(axisSpacing + currentTimeBin * plotWidth, axisSpacing + y, 0);
			currentTimeBin += timeBinStepSize;
			gl.glVertex3f(axisSpacing + currentTimeBin * plotWidth, axisSpacing + y, 0);
			gl.glVertex3f(axisSpacing, axisSpacing + y, 0);
			gl.glEnd();
		}

	}

	private void drawCurve(GL2 gl, float maxValue, ArrayList<Float> dataVector) {

		float plotHeight = viewFrustum.getHeight()
				- (detailLevel == EDetailLevel.HIGH ? pixelGLConverter
						.getGLHeightForPixelHeight(BOTTOM_LEFT_AXIS_SPACING_PIXELS
								+ TOP_RIGHT_AXIS_SPACING_PIXELS) : 0);
		float plotWidth = viewFrustum.getWidth()
				- (detailLevel == EDetailLevel.HIGH ? pixelGLConverter
						.getGLWidthForPixelWidth(BOTTOM_LEFT_AXIS_SPACING_PIXELS
								+ TOP_RIGHT_AXIS_SPACING_PIXELS) : 0);

		float axisSpacing = (detailLevel == EDetailLevel.HIGH ? pixelGLConverter
				.getGLWidthForPixelWidth(BOTTOM_LEFT_AXIS_SPACING_PIXELS) : 0);

		float TIME_BINS = maxValue;
		// float TIME_BINS = (float) dataContainer.getDataDomain().getTable()
		// .getRawForNormalized(1);

		float timeBinStepSize = 1 / TIME_BINS;
		float currentTimeBin = 0;

		int remainingItemCount = dataVector.size();
		float ySingleSampleSize = plotHeight / dataVector.size();

		gl.glBegin(GL2.GL_LINE_STRIP);
		gl.glVertex3f(axisSpacing, axisSpacing + plotHeight, 0);

		for (int binIndex = 0; binIndex < TIME_BINS; binIndex++) {

			while (dataVector.size() > 0 && dataVector.get(0) <= currentTimeBin) {
				dataVector.remove(0);
				remainingItemCount--;
			}

			float y = (float) remainingItemCount * ySingleSampleSize;

			gl.glVertex3f(axisSpacing + currentTimeBin * plotWidth, axisSpacing + y, 0);
			currentTimeBin += timeBinStepSize;
			gl.glVertex3f(axisSpacing + currentTimeBin * plotWidth, axisSpacing + y, 0);
		}

		gl.glEnd();
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedKaplanMeierView serializedForm = new SerializedKaplanMeierView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "TODO: ADD INFO THAT APPEARS IN THE LOG";
	}

	@Override
	public void handleRedrawView() {
		setDisplayListDirty();
	}

	@Override
	public void handleSelectionUpdate(SelectionDelta selectionDelta) {
		super.handleSelectionUpdate(selectionDelta);

		if (selectionDelta.getIDType() == recordGroupSelectionManager.getIDType()) {
			recordGroupSelectionManager.setDelta(selectionDelta);
		}
	}

	@Override
	protected ArrayList<ElementConnectionInformation> createElementConnectionInformation(
			IDType idType, int id) throws InvalidAttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinPixelHeight(EDetailLevel detailLevel) {

		switch (detailLevel) {
		case HIGH:
			return 400;
		case MEDIUM:
			return 100;
		case LOW:
			return 50;
		default:
			return 50;
		}
	}

	@Override
	public int getMinPixelWidth(EDetailLevel detailLevel) {

		switch (detailLevel) {
		case HIGH:
			return 400;
		case MEDIUM:
			return 100;
		case LOW:
			return 50;
		default:
			return 50;
		}
	}
}
