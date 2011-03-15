package org.caleydo.view.visbricks.brick;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.data.virtualarray.StorageVirtualArray;
import org.caleydo.core.util.clusterer.ClusterHelper;
import org.caleydo.core.util.mapping.color.ColorMapping;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.EColorMappingType;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;

public class OverviewHeatMapRenderer extends LayoutRenderer {

	private ColorMapping colorMapper;
	private ArrayList<float[]> heatMapValues;

	public OverviewHeatMapRenderer(ContentVirtualArray contentVA,
			StorageVirtualArray storageVA, ISet set) {
		colorMapper = ColorMappingManager.get().getColorMapping(
				EColorMappingType.GENE_EXPRESSION);
		heatMapValues = new ArrayList<float[]>();

		float[] expressionValues = new float[contentVA.size()];

		for (int storageIndex : storageVA) {

			int index = 0;
			for (int contentIndex : contentVA) {
				expressionValues[index] = set.get(storageIndex).getFloat(
						EDataRepresentation.NORMALIZED, contentIndex);
				index++;
			}

			float arithmeticMean = ClusterHelper
					.arithmeticMean(expressionValues);
			float standardDeviation = ClusterHelper.standardDeviation(
					expressionValues, arithmeticMean);

			float[] currentValues = new float[] {
					arithmeticMean - standardDeviation, arithmeticMean,
					arithmeticMean + standardDeviation };

			heatMapValues.add(currentValues);
		}

	}

	public void render(GL2 gl) {

		if (heatMapValues.size() <= 0)
			return;

		float heatMapElementWidth = x / (float) heatMapValues.size();
		float heatMapElementHeight = y / (float) heatMapValues.get(0).length;

		gl.glBegin(GL2.GL_QUADS);
		float currentPositionX = 0;
		for (float[] currentValues : heatMapValues) {
			float currentPositionY = 0;

			for (float value : currentValues) {
				float[] mappingColor = colorMapper.getColor(value);

				gl.glColor3f(mappingColor[0], mappingColor[1], mappingColor[2]);
				gl.glVertex3f(currentPositionX, currentPositionY, 0);
				gl.glVertex3f(currentPositionX + heatMapElementWidth,
						currentPositionY, 0);
				gl.glVertex3f(currentPositionX + heatMapElementWidth,
						currentPositionY + heatMapElementHeight, 0);
				gl.glVertex3f(currentPositionX, currentPositionY
						+ heatMapElementHeight, 0);

				currentPositionY += heatMapElementHeight;
			}
			currentPositionX += heatMapElementWidth;
		}
		gl.glEnd();

	}
}
