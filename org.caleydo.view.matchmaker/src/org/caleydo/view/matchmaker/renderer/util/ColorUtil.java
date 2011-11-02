package org.caleydo.view.matchmaker.renderer.util;

public class ColorUtil {

	public static float[] getColor(int iColorNr) {

		float fBrighness = 0.9f;
		int iWhiteness = 20;

		int color = (iColorNr) % 6;

		float[] fArMappingColor = new float[] { 0F, 0F, 0F, 0F };

		// 152, 78, 163; 255, 127, 0; 255, 255, 51; 166, 86, 40; 247, 129, 191; 153, 153, 153;

		switch (color) {
			case 0:
				fArMappingColor = new float[] { 228, 26, 28, 1 };
				break;
			case 1:
				fArMappingColor = new float[] { 77, 175, 74, 1 };
				break;
			case 2:
				fArMappingColor = new float[] { 152, 78, 163, 1 };
				break;
			case 3:
				fArMappingColor = new float[] { 255, 127, 0, 1 };
				break;
			case 4:
				fArMappingColor = new float[] { 255, 255, 51, 1 };
				break;
			case 5:
				fArMappingColor = new float[] { 166, 86, 40, 1 };
				break;
			case 6:
				fArMappingColor = new float[] { 247, 129, 191, 1 };
				break;
			

			default:
				fArMappingColor = new float[] { 0, 0, 0, 1 };
		}

		for (int i = 0; i < 3; i++) {
			fArMappingColor[i] = fBrighness * ((fArMappingColor[i] - iWhiteness) / 255f);
			if (fArMappingColor[i] > 1)
				fArMappingColor[i] = 1;
			if (fArMappingColor[i] < 0)
				fArMappingColor[i] = 0;
		}
		return fArMappingColor;

	}
}