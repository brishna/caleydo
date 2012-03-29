package org.caleydo.view.bucket;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.serialize.ASerializedTopLevelDataView;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.view.heatmap.heatmap.SerializedHeatMapView;

/**
 * Serialized form of the remote-rendering view (bucket).
 * 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedBucketView extends ASerializedTopLevelDataView {

	/** @see org.caleydo.core.org.caleydo.core.view.opengl.canvas.AGLViewBrowser.pathwayTexturesEnabled */
	private boolean pathwayTexturesEnabled;

	/** @see org.caleydo.core.org.caleydo.core.view.opengl.canvas.AGLViewBrowser.geneMappingEnabled */
	private boolean geneMappingEnabled;

	/** @see org.caleydo.core.org.caleydo.core.view.opengl.canvas.AGLViewBrowser.neighborhoodEnabled */
	private boolean neighborhoodEnabled;

	/** @see org.caleydo.core.org.caleydo.core.view.opengl.canvas.AGLViewBrowser.connectionLinesEnabled */
	private boolean connectionLinesEnabled;

	/** list of view-ids contained in the focus-level */
	private List<ASerializedView> focusViews;

	/** list of view-ids contained in the stack-level */
	private List<ASerializedView> stackViews;

	/**
	 * No-Arg Constructor to create a serialized bucket-view with default
	 * parameters.
	 */
	public SerializedBucketView() {
	}

	public SerializedBucketView(String dataDomainType) {
		super(dataDomainType);
		init();
	}

	@Override
	public void setDataDomainID(String dataDomainType) {
		super.setDataDomainID(dataDomainType);
		init();

	}

	private void init() {
		setPathwayTexturesEnabled(true);
		setNeighborhoodEnabled(true);
		setGeneMappingEnabled(true);
		setConnectionLinesEnabled(true);

		ArrayList<ASerializedView> remoteViews = new ArrayList<ASerializedView>();

		SerializedHeatMapView heatMap = new SerializedHeatMapView(dataDomainID);
		remoteViews.add(heatMap);

		ArrayList<ASerializedView> focusLevel = new ArrayList<ASerializedView>();
		if (remoteViews.size() > 0) {
			focusLevel.add(remoteViews.remove(0));
		}
		setFocusViews(focusLevel);
		setStackViews(remoteViews);
	}

	public boolean isPathwayTexturesEnabled() {
		return pathwayTexturesEnabled;
	}

	public void setPathwayTexturesEnabled(boolean pathwayTexturesEnabled) {
		this.pathwayTexturesEnabled = pathwayTexturesEnabled;
	}

	public boolean isGeneMappingEnabled() {
		return geneMappingEnabled;
	}

	public void setGeneMappingEnabled(boolean geneMappingEnabled) {
		this.geneMappingEnabled = geneMappingEnabled;
	}

	public boolean isNeighborhoodEnabled() {
		return neighborhoodEnabled;
	}

	public void setNeighborhoodEnabled(boolean neighborhoodEnabled) {
		this.neighborhoodEnabled = neighborhoodEnabled;
	}

	public boolean isConnectionLinesEnabled() {
		return connectionLinesEnabled;
	}

	public void setConnectionLinesEnabled(boolean connectionLinesEnabled) {
		this.connectionLinesEnabled = connectionLinesEnabled;
	}

	@XmlElementWrapper
	public List<ASerializedView> getFocusViews() {
		return focusViews;
	}

	public void setFocusViews(List<ASerializedView> focusViews) {
		this.focusViews = focusViews;
	}

	@XmlElementWrapper
	public List<ASerializedView> getStackViews() {
		return stackViews;
	}

	public void setStackViews(List<ASerializedView> stackViews) {
		this.stackViews = stackViews;
	}

	@Override
	public String getViewType() {
		return GLBucket.VIEW_TYPE;
	}

	@Override
	public ViewFrustum getViewFrustum() {
		return new ViewFrustum(CameraProjectionMode.PERSPECTIVE, -1f, 1f, -1f, 1f, 1.9f,
				100);
	}
}
