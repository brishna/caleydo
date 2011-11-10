package org.caleydo.view.histogram;

import java.util.ArrayList;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.util.format.Formatter;
import org.caleydo.core.util.mapping.color.ChooseColorMappingDialog;
import org.caleydo.core.util.mapping.color.ColorMarkerPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class RcpGLColorMapperHistogramView extends RcpGLHistogramView {

	private CLabel colorMappingPreview;

	private ArrayList<CLabel> labels;

	@Override
	public void redrawView() {

		colorMappingPreview = new CLabel(histoComposite, SWT.SHADOW_IN);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 10;
		gridData.grabExcessHorizontalSpace = true;
		colorMappingPreview.setLayoutData(gridData);
		colorMappingPreview.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ChooseColorMappingDialog dialog = new ChooseColorMappingDialog(new Shell(
						SWT.APPLICATION_MODAL), dataDomain);
				// dialog.setPossibleDataDomains(availableDomains);
				dialog.setBlockOnOpen(true);
				dialog.open();

				updateColorMappingPreview();
			}
		});

		FillLayout fillLayout = new FillLayout();
		Composite labelComposite = new Composite(histoComposite, SWT.NULL);
		labelComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelComposite.setLayout(fillLayout);

		labels = new ArrayList<CLabel>(3);

		int numberOfMarkerPoints = dataDomain.getColorMapper().getMarkerPoints().size();

		for (int count = 0; count < numberOfMarkerPoints; count++) {
			CLabel label = new CLabel(labelComposite, SWT.NONE);
			labels.add(label);
			if (count == numberOfMarkerPoints - 1) {
				label.setAlignment(SWT.RIGHT);
			} else if (count > 0) {
				label.setAlignment(SWT.CENTER);
			}
		}

		updateColorMappingPreview();
	}

	@Override
	public void handleRedrawView() {
		colorMappingPreview.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateColorMappingPreview();
			}
		});
	}

	private void updateColorMappingPreview() {

		ArrayList<ColorMarkerPoint> markerPoints = dataDomain.getColorMapper()
				.getMarkerPoints();

		Color[] alColor = new Color[markerPoints.size()];
		int[] colorMarkerPoints = new int[markerPoints.size() - 1];
		for (int iCount = 1; iCount <= markerPoints.size(); iCount++) {

			float normalizedValue = markerPoints.get(iCount - 1).getMappingValue();

			double correspondingValue = ((ATableBasedDataDomain) dataDomain).getTable()
					.getRawForNormalized(normalizedValue);

			if (labels != null)
				labels.get(iCount - 1)
						.setText(Formatter.formatNumber(correspondingValue));

			int colorMarkerPoint = (int) (100 * normalizedValue);

			// Gradient label does not need the 0 point
			if (colorMarkerPoint != 0) {
				colorMarkerPoints[iCount - 2] = colorMarkerPoint;
			}

			int[] color = markerPoints.get(iCount - 1).getIntColor();

			alColor[iCount - 1] = new Color(PlatformUI.getWorkbench().getDisplay(),
					color[0], color[1], color[2]);
		}

		colorMappingPreview.setBackground(alColor, colorMarkerPoints);
		colorMappingPreview.update();
	}

}
