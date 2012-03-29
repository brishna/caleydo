package org.caleydo.view.heatmap.hierarchical;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.caleydo.core.view.ARcpGLViewPart;
import org.eclipse.swt.widgets.Composite;

public class RcpGLHierarchicalHeatMapView extends ARcpGLViewPart {

	/**
	 * Constructor.
	 */
	public RcpGLHierarchicalHeatMapView() {
		super();

		try {
			viewContext = JAXBContext
					.newInstance(SerializedHierarchicalHeatMapView.class);
		} catch (JAXBException ex) {
			throw new RuntimeException("Could not create JAXBContext", ex);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		createGLCanvas();

		view = new GLHierarchicalHeatMap(glCanvas, parentComposite,
				serializedView.getViewFrustum());

		initializeViewWithData();
		view.initFromSerializableRepresentation(serializedView);
		view.initialize();

		createPartControlGL();
	}

	@Override
	public void createDefaultSerializedView() {

		serializedView = new SerializedHierarchicalHeatMapView();
		determineDataConfiguration(serializedView);
	}

	@Override
	public String getViewGUIID() {
		return GLHierarchicalHeatMap.VIEW_TYPE;
	}

}