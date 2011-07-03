package org.caleydo.view.heatmap.uncertainty;

import java.util.ArrayList;
import java.util.List;

import javax.management.InvalidAttributeValueException;
import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.mapping.color.ColorMapping;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.EColorMappingType;
import org.caleydo.core.view.opengl.camera.ECameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.AStorageBasedView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.view.heatmap.HeatMapRenderStyle;

/**
 * Uncertainty heat map view.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 * @author Clemens Holzhüter
 */

public class GLUncertaintyHeatMap extends AStorageBasedView implements IViewCommandHandler,
		ISelectionUpdateHandler, IGLRemoteRenderingView {

	public final static String VIEW_ID = "org.caleydo.view.heatmap.uncertainty";

	private HeatMapRenderStyle renderStyle;
	
	private UncertaintyOverviewHeatMap overviewHeatMap;
	
	private ColorMapping colorMapper = ColorMappingManager.get().getColorMapping(
			EColorMappingType.GENE_EXPRESSION);

	/**
	 * Constructor.
	 * 
	 * @param glCanvas
	 * @param sLabel
	 * @param viewFrustum
	 */
	public GLUncertaintyHeatMap(GLCaleydoCanvas glCanvas, final ViewFrustum viewFrustum) {
		super(glCanvas, viewFrustum);
		viewType = GLUncertaintyHeatMap.VIEW_ID;
	}

	@Override
	public void init(GL2 gl) {

		super.renderStyle = renderStyle;
		detailLevel = DetailLevel.HIGH;
		
		createOverviewHeatMap();
		overviewHeatMap.initRemote(gl, this, glMouseListener);
	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

		// Register keyboard listener to GL2 canvas
		glParentView.getParentGLCanvas().getParentComposite().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						glParentView.getParentGLCanvas().getParentComposite()
								.addKeyListener(glKeyListener);
					}
				});

		this.glMouseListener = glMouseListener;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);
	}

	/**
	 * Create embedded heat map
	 * 
	 * @param
	 */
	private void createOverviewHeatMap() {

		float fHeatMapHeight = viewFrustum.getHeight() * 0.3f;
		float fHeatMapWidth = viewFrustum.getWidth();
		ViewFrustum viewFrustum = new ViewFrustum(ECameraProjectionMode.ORTHOGRAPHIC, 0,
				(int) fHeatMapHeight, 0, (int) fHeatMapWidth, -20, 20);

		overviewHeatMap = new UncertaintyOverviewHeatMap(this.getParentGLCanvas(), viewFrustum);
//		glHeatMapView.setDataDomain(dataDomain);

		overviewHeatMap.setRemoteRenderingGLView(this);
//		glHeatMapView.setRemoteLevelElement(heatMapRemoteElement);

//		renderTemplate = new HierarchicalHeatMapTemplate(glHeatMapView);
//		glHeatMapView.setRenderTemplate(renderTemplate);
//		renderTemplate.setBottomSpacing(0.6f);
//		heatMapRemoteElement.setGLView(glHeatMapView);
//		glHeatMapView.setContentVAType(GLHeatMap.CONTENT_EMBEDDED_VA);
		overviewHeatMap.initialize();
//		glHeatMapView.initData();
		


	}
	
	@Override
	public void displayLocal(GL2 gl) {

		pickingManager.handlePicking(this, gl);
		display(gl);
		checkForHits(gl);
		
		if (overviewHeatMap != null)
			overviewHeatMap.processEvents();
	}

	@Override
	public void displayRemote(GL2 gl) {
		throw new IllegalStateException("This view cannot be rendered remotely!");
	}

	@Override
	public void display(GL2 gl) {

		overviewHeatMap.displayRemote(gl);
	}

	@Override
	public String getShortInfo() {

		return "LayoutTemplate Caleydo View";
	}

	@Override
	public String getDetailedInfo() {
		return "LayoutTemplate Caleydo View";

	}

	@Override
	protected void handlePickingEvents(EPickingType pickingType,
			EPickingMode pickingMode, int iExternalID, Pick pick) {

		// TODO: Implement picking processing here!
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedUncertaintyHeatMapView serializedForm = new SerializedUncertaintyHeatMapView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "TODO: ADD INFO THAT APPEARS IN THE LOG";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
			boolean scrollToSelection, String info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRedrawView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleUpdateView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleClearSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfSelections(SelectionType SelectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		ArrayList<AGLView> remoteRenderedViews = new ArrayList<AGLView>();
		remoteRenderedViews.add(overviewHeatMap);
		return remoteRenderedViews;
	}

	@Override
	public void renderContext(boolean bRenderContext) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void initLists() {

		if (bRenderOnlyContext)
			contentVAType = ISet.CONTENT_CONTEXT;
		else
			contentVAType = ISet.CONTENT;

		contentVA = dataDomain.getContentVA(contentVAType);
		storageVA = dataDomain.getStorageVA(storageVAType);

		// In case of importing group info
		// if (set.isGeneClusterInfo())
		// contentVA.setGroupList(set.getContentGroupList());
		// if (set.isExperimentClusterInfo())
		// storageVA.setGroupList(set.getStorageGroupList());

		contentSelectionManager.setVA(contentVA);
		storageSelectionManager.setVA(storageVA);

		setDisplayListDirty();
	}
	
	@Override
	protected ArrayList<SelectedElementRep> createElementRep(IDType idType, int id)
			throws InvalidAttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ColorMapping getColorMapper() {
		return colorMapper;
	}
}