package org.caleydo.core.view.opengl.canvas.remote.dataflipper;

import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import gleem.linalg.open.Transform;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.command.view.opengl.CmdCreateGLEventListener;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.EVAOperation;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.IViewManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.manager.specialized.PathwayUseCase;
import org.caleydo.core.manager.specialized.TissueUseCase;
import org.caleydo.core.manager.specialized.clinical.ClinicalUseCase;
import org.caleydo.core.manager.specialized.genetic.GeneticUseCase;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.system.SystemTime;
import org.caleydo.core.util.system.Time;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteElementManager;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevelElement;
import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
import org.caleydo.core.view.opengl.util.slerp.SlerpAction;
import org.caleydo.core.view.opengl.util.slerp.SlerpMod;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.eclipse.core.runtime.Status;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;

public class GLDataFlipper
	extends AGLEventListener
	implements IGLRemoteRenderingView {

	private static final int SLERP_RANGE = 1000;
	private static final int SLERP_SPEED = 1400;

	private static final int MAX_SIDE_VIEWS = 10;

	private ArrayList<ASerializedView> newViews;

	private ArrayList<Integer> containedViewIDs;

	private RemoteLevelElement focusElement;
	private ArrayList<RemoteLevelElement> remoteLevelElementsLeft;
	private ArrayList<RemoteLevelElement> remoteLevelElementsRight;

	private GLInfoAreaManager infoAreaManager;

	private TextRenderer textRenderer;

	private TextureManager textureManager;

	private enum EOrientation {
		LEFT,
		RIGHT,
		BOTTOM,
		TOP;
	};

	private ArrayList<SlerpAction> arSlerpActions;

	private Time time;
	
	private RemoteLevelElement lastPickedRemoteLevelElement;
	private int iLastPickedViewID;

	/**
	 * Slerp factor: 0 = source; 1 = destination
	 */
	private int iSlerpFactor = 0;

	/**
	 * Constructor.
	 */
	public GLDataFlipper(GLCaleydoCanvas glCanvas, final String sLabel, final IViewFrustum viewFrustum) {

		super(glCanvas, sLabel, viewFrustum, true);

		viewType = EManagedObjectType.GL_DATA_FLIPPER;

		// // Unregister standard mouse wheel listener
		// parentGLCanvas.removeMouseWheelListener(glMouseListener);
		// // Register specialized bucket mouse wheel listener
		// parentGLCanvas.addMouseWheelListener(bucketMouseWheelListener);
		// // parentGLCanvas.addMouseListener(bucketMouseWheelListener);

		textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 24), false);
		textureManager = new TextureManager();
		arSlerpActions = new ArrayList<SlerpAction>();

		glMouseListener.addGLCanvas(this);

		newViews = new ArrayList<ASerializedView>();
		containedViewIDs = new ArrayList<Integer>();
		remoteLevelElementsRight = new ArrayList<RemoteLevelElement>();
		remoteLevelElementsLeft = new ArrayList<RemoteLevelElement>();

		// TODO: Move to render style
		Transform transform = new Transform();
		transform.setTranslation(new Vec3f(-0.2f, -0f, 4));//-1.7f, -1.5f, 4));
		transform.setScale(new Vec3f(1 / 2.3f, 1 / 2.3f, 1 / 2.3f));
//		transform.setTranslation(new Vec3f(-1.95f, -1.4f, 0));
//		transform.setScale(new Vec3f(1 / 1.15f, 1 / 1.15f, 1 / 1.1f));
		
		focusElement = new RemoteLevelElement(null);
		focusElement.setTransform(transform);
		RemoteElementManager.get().registerItem(focusElement);

		for (int iSideViewsIndex = 1; iSideViewsIndex <= MAX_SIDE_VIEWS; iSideViewsIndex++) {
			RemoteLevelElement newElement = new RemoteLevelElement(null);
			transform = new Transform();
			transform.setTranslation(new Vec3f(-2.2f - iSideViewsIndex / 2f +1.5f, -1.5f+1.5f, 4f));
			transform.setScale(new Vec3f(1 / 2.3f, 1 / 2.3f, 1 / 2.3f));
			transform.setRotation(new Rotf(new Vec3f(0, 1, 0), Vec3f.convertGrad2Radiant(100)));
			newElement.setTransform(transform);
			remoteLevelElementsLeft.add(newElement);
			RemoteElementManager.get().registerItem(newElement);

			newElement = new RemoteLevelElement(null);
			transform = new Transform();
			transform.setTranslation(new Vec3f(3.6f + iSideViewsIndex / 2f+1.5f, -1.79f+1.5f, -1f));
			transform.setScale(new Vec3f(1 / 1.9f, 1 / 1.9f, 1 / 1.9f));
			transform.setRotation(new Rotf(new Vec3f(0, -1, 0), Vec3f.convertGrad2Radiant(100)));
			newElement.setTransform(transform);
			remoteLevelElementsRight.add(newElement);
			RemoteElementManager.get().registerItem(newElement);
		}
	}

	@Override
	public void initLocal(final GL gl) {
		// iGLDisplayList = gl.glGenLists(1);
		init(gl);
	}

	@Override
	public void initRemote(final GL gl, final AGLEventListener glParentView,
		final GLMouseListener glMouseListener, GLInfoAreaManager infoAreaManager) {

		throw new IllegalStateException("Not implemented to be rendered remote");
	}

	@Override
	public void init(final GL gl) {
		gl.glClearColor(0.5f, 0.5f, 0.5f, 1f);

		time = new SystemTime();
		((SystemTime) time).rebase();

		infoAreaManager = new GLInfoAreaManager();
		infoAreaManager.initInfoInPlace(viewFrustum);

	}

	@Override
	public void displayLocal(final GL gl) {

		pickingManager.handlePicking(this, gl);

		display(gl);

		if (eBusyModeState != EBusyModeState.OFF) {
			renderBusyMode(gl);
		}

		checkForHits(gl);

		// gl.glCallList(iGLDisplayListIndexLocal);
	}

	@Override
	public void displayRemote(final GL gl) {
		display(gl);
	}

	@Override
	public void display(final GL gl) {

		// for (int iSideViewsIndex = 1; iSideViewsIndex <= 2; iSideViewsIndex++) {
		// RemoteLevelElement newElement = remoteLevelElementsRight.get(iSideViewsIndex-1);
		// Transform transform = new Transform();
		// transform.setTranslation(new Vec3f(3.6f + iSideViewsIndex / 2f, -1.9f, -1f));
		// transform.setScale(new Vec3f(1 / 1.9f, 1 / 1.9f, 1f));
		// transform.setRotation(new Rotf(new Vec3f(0, -1, 0), Vec3f.convertGrad2Radiant(100)));
		// newElement.setTransform(transform);
		// remoteLevelElementsRight.add(newElement);
		// }

		time.update();
		processEvents();

		// gl.glCallList(iGLDisplayList);

		doSlerpActions(gl);
		initNewView(gl);

		renderHandles(gl);
		renderDataViewIcons(gl, GeneralManager.get().getUseCase(EDataDomain.CLINICAL_DATA));
		renderDataViewIcons(gl, new TissueUseCase());
		renderDataViewIcons(gl, useCase);
		renderDataViewIcons(gl, new PathwayUseCase());

		// renderHandles(gl);
		renderRemoteLevelElement(gl, focusElement);

		for (RemoteLevelElement element : remoteLevelElementsLeft) {
			renderRemoteLevelElement(gl, element);
		}

		for (RemoteLevelElement element : remoteLevelElementsRight) {
			renderRemoteLevelElement(gl, element);
		}

		// gl.glBegin(GL.GL_POLYGON);
		// gl.glVertex3f(0, 2, 0);
		// gl.glVertex3f(6, 4, 8);
		// gl.glVertex3f(2, 3, 4);
		// gl.glVertex3f(1, 6, 3);
		// gl.glEnd();
		// GLHelperFunctions.drawViewFrustum(gl, viewFrustum);

		float fZTranslation = 0;
		fZTranslation = 4f;

		gl.glTranslatef(0, 0, fZTranslation);
		contextMenu.render(gl, this);
		gl.glTranslatef(0, 0, -fZTranslation);
	}

	private void renderRemoteLevelElement(final GL gl, RemoteLevelElement element) {

		if (element.getContainedElementID() == -1)
			return;

		int iViewID = element.getContainedElementID();

		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.REMOTE_LEVEL_ELEMENT, element
			.getID()));
		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.VIEW_SELECTION, iViewID));

		AGLEventListener glEventListener =
			generalManager.getViewGLCanvasManager().getGLEventListener(iViewID);

		if (glEventListener == null) {
			generalManager.getLogger().log(
				new Status(Status.WARNING, GeneralManager.PLUGIN_ID,
					"Remote level element is null and cannot be rendered!"));
			return;
		}

		gl.glPushMatrix();

		Transform transform = element.getTransform();
		Vec3f translation = transform.getTranslation();
		Rotf rot = transform.getRotation();
		Vec3f scale = transform.getScale();
		Vec3f axis = new Vec3f();
		float fAngle = rot.get(axis);

		gl.glTranslatef(translation.x()-1.5f, translation.y()-1.5f, translation.z());
		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z());
		gl.glScalef(scale.x(), scale.y(), scale.z());

		renderBucketWall(gl, true);
		glEventListener.displayRemote(gl);

		gl.glPopMatrix();

		gl.glPopName();
		gl.glPopName();
	}

	/**
	 * Adds new remote-rendered-views that have been queued for displaying to this view. Only one view is
	 * taken from the list and added for remote rendering per call to this method.
	 * 
	 * @param GL
	 */
	private void initNewView(GL gl) {

		// if(arSlerpActions.isEmpty())
		// {
		if (!newViews.isEmpty()) {
			ASerializedView serView = newViews.remove(0);
			AGLEventListener view = createView(gl, serView);

			// addSlerpActionForView(gl, view);

			// TODO: remove when activating slerp
			view.initRemote(gl, this, glMouseListener, infoAreaManager);
			view.setDetailLevel(EDetailLevel.HIGH);
			// view.getViewFrustum().considerAspectRatio(true);

			containedViewIDs.add(view.getID());

			if (focusElement.isFree()) {
				focusElement.setContainedElementID(view.getID());
			}
			else {

				if (newViews.size() % 2 == 0) {

					Iterator<RemoteLevelElement> iter = remoteLevelElementsLeft.iterator();
					while (iter.hasNext()) {
						RemoteLevelElement element = iter.next();
						if (element.isFree()) {
							element.setContainedElementID(view.getID());
							break;
						}
					}
				}
				else {
					Iterator<RemoteLevelElement> iter = remoteLevelElementsRight.iterator();
					while (iter.hasNext()) {
						RemoteLevelElement element = iter.next();
						if (element.isFree()) {
							element.setContainedElementID(view.getID());
							break;
						}
					}
				}
			}

			if (newViews.isEmpty()) {
				// triggerToolBarUpdate();
				enableUserInteraction();
			}
		}
	}

	@Override
	public void initFromSerializableRepresentation(ASerializedView ser) {
		// resetView(false);

		SerializedDataFlipperView serializedView = (SerializedDataFlipperView) ser;
		newViews.addAll(serializedView.getInitialContainedViews());

		setDisplayListDirty();
	}

	/**
	 * Creates and initializes a new view based on its serialized form. The view is already added to the list
	 * of event receivers and senders.
	 * 
	 * @param gl
	 * @param serView
	 *            serialized form of the view to create
	 * @return the created view ready to be used within the application
	 */
	private AGLEventListener createView(GL gl, ASerializedView serView) {

		ICommandManager commandManager = generalManager.getCommandManager();
		ECommandType cmdType = serView.getCreationCommandType();
		CmdCreateGLEventListener cmdView =
			(CmdCreateGLEventListener) commandManager.createCommandByType(cmdType);
		cmdView.setAttributesFromSerializedForm(serView);
		// cmdView.setSet(set);
		cmdView.doCommand();

		AGLEventListener glView = cmdView.getCreatedObject();
		glView.setUseCase(useCase);
		glView.setRemoteRenderingGLView(this);
		glView.setSet(set);

		// if (glView instanceof GLPathway) {
		// initializePathwayView((GLPathway) glView);
		// }

		// triggerMostRecentDelta();

		return glView;
	}

	/**
	 * Disables picking and enables busy mode
	 */
	public void disableUserInteraction() {
		IViewManager canvasManager = generalManager.getViewGLCanvasManager();
		canvasManager.getPickingManager().enablePicking(false);
		canvasManager.requestBusyMode(this);
	}

	/**
	 * Enables picking and disables busy mode
	 */
	public void enableUserInteraction() {
		IViewManager canvasManager = generalManager.getViewGLCanvasManager();
		canvasManager.getPickingManager().enablePicking(true);
		canvasManager.releaseBusyMode(this);
	}

	private void doSlerpActions(final GL gl) {
		if (arSlerpActions.isEmpty())
			return;

		// SlerpAction tmpSlerpAction = arSlerpActions.get(0);

		if (iSlerpFactor == 0) {

			for (SlerpAction tmpSlerpAction : arSlerpActions) {
				tmpSlerpAction.start();
			}
		}

		if (iSlerpFactor < SLERP_RANGE) {
			// Makes animation rendering CPU speed independent
			iSlerpFactor += SLERP_SPEED * time.deltaT();

			if (iSlerpFactor > SLERP_RANGE) {
				iSlerpFactor = SLERP_RANGE;
			}
		}

		for (SlerpAction tmpSlerpAction : arSlerpActions) {
			slerpView(gl, tmpSlerpAction);
		}

		// Check if slerp action is finished
		if (iSlerpFactor >= SLERP_RANGE) {

//			// Finish in reverse order - otherwise the target ID would overwrite the next
//			for (int iSlerpIndex = arSlerpActions.size() - 1; iSlerpIndex >= 0; iSlerpIndex--) {
//				arSlerpActions.get(iSlerpIndex).finished();
//			}
			
			for (SlerpAction tmpSlerpAction : arSlerpActions) {
				tmpSlerpAction.finished();
			}

			arSlerpActions.clear();
			iSlerpFactor = 0;
			
			// Trigger chain move when selected view has not reached the focus position
			if (iLastPickedViewID != focusElement.getContainedElementID())
				chainMove(lastPickedRemoteLevelElement);
		}
	}

	private void slerpView(final GL gl, SlerpAction slerpAction) {
		int iViewID = slerpAction.getElementId();

		if (iViewID == -1)
			return;

		SlerpMod slerpMod = new SlerpMod();

		if (iSlerpFactor == 0) {
			slerpMod.playSlerpSound();
		}

		Transform transform =
			slerpMod.interpolate(slerpAction.getOriginRemoteLevelElement().getTransform(), slerpAction
				.getDestinationRemoteLevelElement().getTransform(), (float) iSlerpFactor / SLERP_RANGE);

		gl.glPushMatrix();

		slerpMod.applySlerp(gl, transform, true);

		renderBucketWall(gl, true);
		generalManager.getViewGLCanvasManager().getGLEventListener(iViewID).displayRemote(gl);

		gl.glPopMatrix();

		// // Check if slerp action is finished
		// if (iSlerpFactor >= SLERP_RANGE) {
		// // arSlerpActions.remove(slerpAction);
		// arSlerpActions.removeAll();
		//
		// iSlerpFactor = 0;
		//			
		// slerpAction.finished();
		//
		// // RemoteLevelElement destinationElement = slerpAction.getDestinationRemoteLevelElement();
		//
		// // updateViewDetailLevels(destinationElement);
		// // bUpdateOffScreenTextures = true;
		// }

		// // After last slerp action is done the line connections are turned on
		// // again
		// if (arSlerpActions.isEmpty()) {
		// if (glConnectionLineRenderer != null) {
		// glConnectionLineRenderer.enableRendering(true);
		// }
		//
		// generalManager.getViewGLCanvasManager().getInfoAreaManager().enable(!bEnableNavigationOverlay);
		// generalManager.getViewGLCanvasManager().getConnectedElementRepresentationManager().clearTransformedConnections();
		// }
	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllSelections() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDetailedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfSelections(ESelectionType eSelectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getShortInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void handlePickingEvents(EPickingType pickingType, EPickingMode pickingMode, int iExternalID, Pick pick) {

		switch (pickingType) {

			case REMOTE_LEVEL_ELEMENT:
				switch (pickingMode) {
					case CLICKED:
						// Check if other slerp action is currently running
						if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE) {
							break;
						}

						// glConnectionLineRenderer.enableRendering(true);

						arSlerpActions.clear();
						lastPickedRemoteLevelElement = RemoteElementManager.get().getItem(iExternalID);
						iLastPickedViewID = lastPickedRemoteLevelElement.getContainedElementID();
						chainMove(lastPickedRemoteLevelElement);

						break;
					case MOUSE_OVER:

						break;
				}
				break;

			case BUCKET_DRAG_ICON_SELECTION:

				switch (pickingMode) {
					case CLICKED:

						break;
				}
		}
	}

	private void chainMove(RemoteLevelElement selectedElement) {
		// Chain slerping to the right
		if (remoteLevelElementsLeft.contains(selectedElement)) {

			for (int iElementIndex = remoteLevelElementsLeft.size(); iElementIndex >= 0; iElementIndex--) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					arSlerpActions.add(new SlerpAction(remoteLevelElementsLeft
						.get(iElementIndex + 1), remoteLevelElementsLeft.get(iElementIndex)));
				}

				if (iElementIndex == 0) {
					arSlerpActions.add(new SlerpAction(remoteLevelElementsLeft
						.get(iElementIndex), focusElement));
				}
			}

			arSlerpActions
				.add(new SlerpAction(focusElement, remoteLevelElementsRight.get(0)));

			for (int iElementIndex = 0; iElementIndex < remoteLevelElementsRight.size(); iElementIndex++) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					// if (!remoteLevelElementsRight.get(iElementIndex + 1).isFree()) {
					arSlerpActions
						.add(new SlerpAction(remoteLevelElementsRight.get(iElementIndex),
							remoteLevelElementsRight.get(iElementIndex + 1)));
					// }
				}
			}
		}
		// Chain slerping to the left
		else if (remoteLevelElementsRight.contains(selectedElement)) {

			for (int iElementIndex = 0; iElementIndex < remoteLevelElementsRight.size(); iElementIndex++) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					arSlerpActions
						.add(new SlerpAction(remoteLevelElementsRight.get(iElementIndex + 1),
							remoteLevelElementsRight.get(iElementIndex)));
				}

				if (iElementIndex == 0) {
					arSlerpActions.add(new SlerpAction(remoteLevelElementsRight
						.get(iElementIndex), focusElement));
				}
			}

			arSlerpActions.add(new SlerpAction(focusElement, remoteLevelElementsLeft.get(0)));

			for (int iElementIndex = 0; iElementIndex < remoteLevelElementsLeft.size(); iElementIndex++) {

				if (iElementIndex < (MAX_SIDE_VIEWS - 1)) {
					// if (!remoteLevelElementsLeft.get(iElementIndex + 1).isFree()) {
					arSlerpActions.add(new SlerpAction(remoteLevelElementsLeft
						.get(iElementIndex), remoteLevelElementsLeft.get(iElementIndex + 1)));
					// }
				}
			}
		}
	}
	
	@Override
	public ASerializedView getSerializableRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		super.reshape(drawable, x, y, width, height);

		AGLEventListener glView =
			generalManager.getViewGLCanvasManager().getGLEventListener(focusElement.getContainedElementID());

		if (glView == null)
			return;

		// IViewFrustum frustum = glView.getViewFrustum();
		// frustum.setTop(8*fAspectRatio);
		// glView.reshape(drawable, x, y, width, height);
	}

	private void renderDataViewIcons(final GL gl, IUseCase dataDomain) {

		EIconTextures dataIcon = null;
		float fXPos = 0.6f;
		
		if (dataDomain instanceof ClinicalUseCase) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_PATIENT;
			fXPos += -2.4f;
		}
		else if (dataDomain instanceof TissueUseCase) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_TISSUE;
			fXPos += -1.4f;
		}
		else if (dataDomain instanceof GeneticUseCase) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_GENE_EXPRESSION;
			fXPos += -0.4f;
		}
		else if (dataDomain instanceof PathwayUseCase) {
			dataIcon = EIconTextures.DATA_FLIPPER_DATA_ICON_PATHWAY;
			fXPos += 0.6f;
		}

		gl.glTranslatef(fXPos, -2.6f, 3);

		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_DATA_ICON_BACKGROUND, new Vec3f(0, 0, 0),
			new Vec3f(0.63f, 0, 0), new Vec3f(0.63f, 0.46f, 0), new Vec3f(0, 0.46f, 0), 1, 1, 1, 1);

		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_ROUNDED, new Vec3f(
			0.15f, 0.47f, 0), new Vec3f(0, 0.47f, 0), new Vec3f(0, 0.62f, 0), new Vec3f(0.15f, 0.62f, 0), 1,
			1, 1, 1);

		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_SQUARE, new Vec3f(
			0.16f, 0.47f, 0), new Vec3f(0.31f, 0.47f, 0), new Vec3f(0.31f, 0.62f, 0), new Vec3f(0.16f, 0.62f,
			0), 1, 1, 1, 1);

		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_SQUARE, new Vec3f(
			0.32f, 0.47f, 0), new Vec3f(0.47f, 0.47f, 0), new Vec3f(0.47f, 0.62f, 0), new Vec3f(0.32f, 0.62f,
			0), 1, 1, 1, 1);

		textureManager.renderTexture(gl, EIconTextures.DATA_FLIPPER_VIEW_ICON_BACKGROUND_ROUNDED, new Vec3f(
			0.48f, 0.47f, 0), new Vec3f(0.63f, 0.47f, 0), new Vec3f(0.63f, 0.62f, 0), new Vec3f(0.48f, 0.62f,
			0), 1, 1, 1, 1);
		
		textureManager.renderTexture(gl, dataIcon, new Vec3f(0f,
			0f, 0.01f), new Vec3f(0.63f, 0.0f, 0.01f), new Vec3f(0.63f, 0.47f, 0.01f), new Vec3f(0.0f,
			0.47f, 0.01f), 1, 1, 1, 1);

		gl.glTranslatef(-fXPos, 2.6f, -3);
	}

	// FIXME: method copied from bucket
	private void renderHandles(final GL gl) {

		// Bucket center (focus)
		RemoteLevelElement element = focusElement;
		if (element.getContainedElementID() != -1) {

			Transform transform;
			Vec3f translation;
			Vec3f scale;

			float fYCorrection = 0f;

			transform = element.getTransform();
			translation = transform.getTranslation();
			scale = transform.getScale();

			gl.glTranslatef(translation.x()-1.5f, translation.y() - 0.075f + fYCorrection,
				translation.z() + 0.001f);

//			gl.glScalef(scale.x() * 4, scale.y() * 4, 1);
			renderNavigationHandleBar(gl, element, 3.48f, 0.075f, false, 2);
//			gl.glScalef(1 / (scale.x() * 4), 1 / (scale.y() * 4), 1);

			gl.glTranslatef(-translation.x()+1.5f, -translation.y() + 0.075f - fYCorrection,
				-translation.z() - 0.001f);
		}

		// Left first
		element = remoteLevelElementsLeft.get(0);
		if (element.getContainedElementID() != -1) {

			gl.glTranslatef(-0.68f, -1.495f, 4.02f);
			gl.glRotatef(90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.47f, 0.075f, false, 2);
			gl.glRotatef(-90, 0, 0, 1);
			gl.glTranslatef(0.68f, 1.495f, -4.02f);
		}

		// Left second
		element = remoteLevelElementsLeft.get(1);
		if (element.getContainedElementID() != -1) {

			gl.glTranslatef(-1.17f, -1.495f, 4.02f);
			gl.glRotatef(90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.47f, 0.075f, false, 2);
			gl.glRotatef(-90, 0, 0, 1);
			gl.glTranslatef(1.17f, 1.495f, -4.02f);
		}

		// Right first
		element = remoteLevelElementsRight.get(0);
		if (element.getContainedElementID() != -1) {
			gl.glTranslatef(0.765f, 2, 4.02f);
			gl.glRotatef(-90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.47f, 0.075f, false, 2);
			gl.glRotatef(90, 0, 0, 1);
			gl.glTranslatef(-0.765f, -2, -4.02f);
		}

		// Right second
		element = remoteLevelElementsRight.get(1);
		if (element.getContainedElementID() != -1) {
			gl.glTranslatef(1.165f, 2, 4.02f);
			gl.glRotatef(-90, 0, 0, 1);
			renderNavigationHandleBar(gl, element, 3.47f, 0.075f, false, 2);
			gl.glRotatef(90, 0, 0, 1);
			gl.glTranslatef(-1.165f, -2, -4.02f);
		}
	}

	// FIXME: method copied from bucket
	// private void renderViewTitleBar(final GL gl, RemoteLevelElement element, float fHandleWidth,
	// float fHandleHeight, boolean bUpsideDown, float fScalingFactor, EOrientation eOrientation) {
	//
	// // if (eOrientation == EOrientation.LEFT) {
	// // gl.glBegin(GL.GL_POLYGON);
	// // gl.glVertex3f(-fHandleHeight, fHandleHeight, 0);
	// // gl.glVertex3f(0, fHandleHeight, 0);
	// // gl.glVertex3f(0, fHandleWidth - fHandleHeight, 0);
	// // gl.glVertex3f(-fHandleHeight, fHandleWidth - fHandleHeight, 0);
	// // gl.glEnd();
	// //
	// // // Render icons
	// // gl.glTranslatef(-fHandleHeight, 0, 0);
	// // renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_DRAG_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight, eOrientation);
	// // gl.glTranslatef(0, fHandleWidth - fHandleWidth, 0);
	// //// if (bUpsideDown) {
	// //// gl.glRotatef(180, 1, 0, 0);
	// //// gl.glTranslatef(0, fHandleHeight, 0);
	// //// }
	// //// renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_LOCK_ICON_SELECTION,
	// //// EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight);
	// //// if (bUpsideDown) {
	// //// gl.glTranslatef(0, -fHandleHeight, 0);
	// //// gl.glRotatef(-180, 1, 0, 0);
	// //// }
	// //// gl.glTranslatef(0, -fHandleWidth, 0);
	// //// renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_REMOVE_ICON_SELECTION,
	// //// EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight);
	// // gl.glTranslatef(fHandleHeight, -fHandleWidth + fHandleWidth, 0);
	// //
	// // }
	//
	// if (eOrientation == EOrientation.LEFT)
	// gl.glRotatef(90, 0, 0, 1);
	//		
	// // Render icons
	// // gl.glTranslatef(0, fHandleWidth + fHandleHeight, 0);
	// // renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_DRAG_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight, eOrientation);
	// // gl.glTranslatef(0, -fHandleWidth - fHandleHeight, 0);
	// // gl.glTranslatef(fHandleWidth - 2 * fHandleHeight, 0, 0);
	//		
	// // if (bUpsideDown) {
	// // gl.glRotatef(180, 1, 0, 0);
	// // gl.glTranslatef(0, fHandleHeight, 0);
	// // }
	// // renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_LOCK_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight, eOrientation);
	// // if (bUpsideDown) {
	// // gl.glTranslatef(0, -fHandleHeight, 0);
	// // gl.glRotatef(-180, 1, 0, 0);
	// // }
	// // gl.glTranslatef(fHandleHeight, 0, 0);
	// // renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_REMOVE_ICON_SELECTION,
	// // EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight, eOrientation);
	// // gl.glTranslatef(-fHandleWidth + fHandleHeight, -fHandleWidth - fHandleHeight, 0);
	// //
	// // // Render background (also draggable)
	// //
	// // gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.BUCKET_DRAG_ICON_SELECTION,
	// element
	// // .getID()));
	// gl.glColor3f(0.25f, 0.25f, 0.25f);
	// //
	// //// if (eOrientation == EOrientation.TOP) {
	// gl.glBegin(GL.GL_POLYGON);
	// gl.glVertex3f(0 + fHandleHeight, fHandleWidth + fHandleHeight, 0);
	// gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, fHandleWidth + fHandleHeight, 0);
	// gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, fHandleWidth, 0);
	// gl.glVertex3f(0 + fHandleHeight, fHandleWidth, 0);
	// gl.glEnd();
	// //// }
	// // gl.glPopName();
	// //
	// if (eOrientation == EOrientation.LEFT)
	// gl.glRotatef(-90, 0, 0, 1);
	//
	// //
	// // // Render view information
	// // String sText =
	// // generalManager.getViewGLCanvasManager().getGLEventListener(element.getContainedElementID())
	// // .getShortInfo();
	// //
	// // int iMaxChars = 50;
	// // if (sText.length() > iMaxChars) {
	// // sText = sText.subSequence(0, iMaxChars - 3) + "...";
	// // }
	// //
	// // float fTextScalingFactor = 0.0027f;
	// //
	// // if (bUpsideDown) {
	// // gl.glRotatef(180, 1, 0, 0);
	// // gl.glTranslatef(0, -4 - fHandleHeight, 0);
	// // }
	// //
	// // textRenderer.setColor(0.7f, 0.7f, 0.7f, 1);
	// // textRenderer.begin3DRendering();
	// // textRenderer.draw3D(sText, fHandleWidth / fScalingFactor
	// // - (float) textRenderer.getBounds(sText).getWidth() / 2f * fTextScalingFactor, fHandleWidth + .02f,
	// // 0f, fTextScalingFactor);
	// // textRenderer.end3DRendering();
	// //
	// // if (bUpsideDown) {
	// // gl.glTranslatef(0, 4 + fHandleHeight, 0);
	// // gl.glRotatef(-180, 1, 0, 0);
	// // }
	// }

	private void renderNavigationHandleBar(final GL gl, RemoteLevelElement element, float fHandleWidth,
		float fHandleHeight, boolean bUpsideDown, float fScalingFactor) {

		// Render icons
		gl.glTranslatef(0, 2 + fHandleHeight, 0);
		renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_DRAG_ICON_SELECTION,
			EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight);
		gl.glTranslatef(fHandleWidth - 2 * fHandleHeight, 0, 0);
		if (bUpsideDown) {
			gl.glRotatef(180, 1, 0, 0);
			gl.glTranslatef(0, fHandleHeight, 0);
		}
		renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_LOCK_ICON_SELECTION,
			EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight);
		if (bUpsideDown) {
			gl.glTranslatef(0, -fHandleHeight, 0);
			gl.glRotatef(-180, 1, 0, 0);
		}
		gl.glTranslatef(fHandleHeight, 0, 0);
		renderSingleHandle(gl, element.getID(), EPickingType.BUCKET_REMOVE_ICON_SELECTION,
			EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight);
		gl.glTranslatef(-fHandleWidth + fHandleHeight, -2 - fHandleHeight, 0);

		// Render background (also draggable)
		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.BUCKET_DRAG_ICON_SELECTION, element
			.getID()));
		gl.glColor3f(0.25f, 0.25f, 0.25f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0 + fHandleHeight, 2 + fHandleHeight, 0);
		gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, 2 + fHandleHeight, 0);
		gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, 2, 0);
		gl.glVertex3f(0 + fHandleHeight, 2, 0);
		gl.glEnd();

		gl.glPopName();

		// Render view information
		String sText =
			generalManager.getViewGLCanvasManager().getGLEventListener(element.getContainedElementID())
				.getShortInfo();

		int iMaxChars = 50;
		if (sText.length() > iMaxChars) {
			sText = sText.subSequence(0, iMaxChars - 3) + "...";
		}

		float fTextScalingFactor = 0.0027f;

		if (bUpsideDown) {
			gl.glRotatef(180, 1, 0, 0);
			gl.glTranslatef(0, -4 - fHandleHeight, 0);
		}

		textRenderer.setColor(0.7f, 0.7f, 0.7f, 1);
		textRenderer.begin3DRendering();
		textRenderer.draw3D(sText, fHandleWidth / fScalingFactor
			- (float) textRenderer.getBounds(sText).getWidth() / 2f * fTextScalingFactor, 2.02f, 0f,
			fTextScalingFactor);
		textRenderer.end3DRendering();

		if (bUpsideDown) {
			gl.glTranslatef(0, 4 + fHandleHeight, 0);
			gl.glRotatef(-180, 1, 0, 0);
		}
	}

	// FIXME: method copied from bucket
	private void renderSingleHandle(final GL gl, int iRemoteLevelElementID, EPickingType ePickingType,
		EIconTextures eIconTexture, float fWidth, float fHeight) {

		gl.glPushName(pickingManager.getPickingID(iUniqueID, ePickingType, iRemoteLevelElementID));

		Texture tempTexture = textureManager.getIconTexture(gl, eIconTexture);
		tempTexture.enable();
		tempTexture.bind();

		TextureCoords texCoords = tempTexture.getImageTexCoords();
		gl.glColor3f(1, 1, 1);
		gl.glBegin(GL.GL_POLYGON);

		// if (eOrientation == EOrientation.TOP) {
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(0, -fHeight, 0f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(0, 0, 0f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(fWidth, 0, 0f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(fWidth, -fHeight, 0f);
		gl.glEnd();
		// }
		// else if (eOrientation == EOrientation.LEFT) {
		// gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		// gl.glVertex3f(0, -fHeight, 0f);
		// gl.glTexCoord2f(texCoords.left(), texCoords.top());
		// gl.glVertex3f(0, 0, 0f);
		// gl.glTexCoord2f(texCoords.right(), texCoords.top());
		// gl.glVertex3f(fWidth, 0, 0f);
		// gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		// gl.glVertex3f(fWidth, -fHeight, 0f);
		// gl.glEnd();
		// }

		tempTexture.disable();

		gl.glPopName();
	}

	// FIXME: method copied from bucket
	public void renderBucketWall(final GL gl, boolean bRenderBorder) {

		gl.glLineWidth(2);

		// Highlight potential view drop destination
		// if (dragAndDrop.isDragActionRunning() && element.getID() == iMouseOverObjectID) {
		// gl.glLineWidth(5);
		// }
		// gl.glColor4f(0.2f, 0.2f, 0.2f, 1);
		// gl.glBegin(GL.GL_LINE_LOOP);
		// gl.glVertex3f(0, 0, 0.01f);
		// gl.glVertex3f(0, 8, 0.01f);
		// gl.glVertex3f(8, 8, 0.01f);
		// gl.glVertex3f(8, 0, 0.01f);
		// gl.glEnd();
		// }

		// if (arSlerpActions.isEmpty()) {
		gl.glColor4f(1f, 1f, 1f, 1.0f); // normal mode
		// }
		// else {
		// gl.glColor4f(1f, 1f, 1f, 0.3f);
		// }

		if (!newViews.isEmpty()) {
			gl.glColor4f(1f, 1f, 1f, 0.3f);
		}

		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0, 0, -0.01f);
		gl.glVertex3f(0, 8, -0.01f);
		gl.glVertex3f(8, 8, -0.01f);
		gl.glVertex3f(8, 0, -0.01f);
		gl.glEnd();

		if (!bRenderBorder)
			return;

		gl.glColor4f(0.4f, 0.4f, 0.4f, 1f);
		gl.glLineWidth(1f);
	}
}
