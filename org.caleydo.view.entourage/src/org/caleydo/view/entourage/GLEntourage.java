package org.caleydo.view.entourage;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.view.MinSizeUpdateEvent;
import org.caleydo.core.id.IDType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.contextmenu.AContextMenuItem;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.GLMouseAdapter;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.layout.util.multiform.DefaultVisInfo;
import org.caleydo.core.view.opengl.layout.util.multiform.IMultiFormChangeListener;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.core.view.opengl.layout2.AGLElementGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.GLElementAdapter;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.GLSizeRestrictiveFlowLayout;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.util.GLElementWindow;
import org.caleydo.core.view.opengl.layout2.util.GLElementWindow.ICloseWindowListener;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.data.loader.ResourceLoader;
import org.caleydo.data.loader.ResourceLocators.IResourceLocator;
import org.caleydo.datadomain.genetic.EGeneIDTypes;
import org.caleydo.datadomain.pathway.IPathwayRepresentation;
import org.caleydo.datadomain.pathway.IVertexRepSelectionListener;
import org.caleydo.datadomain.pathway.VertexRepBasedContextMenuItem;
import org.caleydo.datadomain.pathway.graph.PathSegment;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.PathwayPath;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.listener.ESampleMappingMode;
import org.caleydo.datadomain.pathway.listener.EnablePathSelectionEvent;
import org.caleydo.datadomain.pathway.listener.LoadPathwaysByGeneEvent;
import org.caleydo.datadomain.pathway.listener.LoadPathwaysEvent;
import org.caleydo.datadomain.pathway.listener.PathwayMappingEvent;
import org.caleydo.datadomain.pathway.listener.PathwayPathSelectionEvent;
import org.caleydo.datadomain.pathway.listener.SampleMappingModeEvent;
import org.caleydo.datadomain.pathway.listener.ShowNodeContextEvent;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.datadomain.pathway.toolbar.SelectPathAction;
import org.caleydo.view.enroute.GLEnRoutePathway;
import org.caleydo.view.enroute.event.FitToViewWidthEvent;
import org.caleydo.view.entourage.MultiLevelSlideInElement.IWindowState;
import org.caleydo.view.entourage.SlideInElement.ESlideInElementPosition;
import org.caleydo.view.entourage.datamapping.DataMappers;
import org.caleydo.view.entourage.datamapping.DataMappingState;
import org.caleydo.view.entourage.datamapping.DataMappingWizard;
import org.caleydo.view.entourage.event.AddPathwayEvent;
import org.caleydo.view.entourage.event.AddPathwayEventFactory;
import org.caleydo.view.entourage.event.ClearWorkspaceEvent;
import org.caleydo.view.entourage.event.SelectPathwayEventFactory;
import org.caleydo.view.entourage.event.ShowCommonNodesPathwaysEvent;
import org.caleydo.view.entourage.event.ShowNodeContextEventFactory;
import org.caleydo.view.entourage.event.ShowPortalsEvent;
import org.caleydo.view.entourage.pathway.PathwayViews;
import org.caleydo.view.entourage.ranking.PathwayFilters;
import org.caleydo.view.entourage.ranking.PathwayRankings;
import org.caleydo.view.entourage.ranking.RankingElement;
import org.caleydo.view.entourage.toolbar.ShowPortalsAction;
import org.eclipse.swt.widgets.Display;

public class GLEntourage extends AGLElementGLView implements IMultiTablePerspectiveBasedView, IGLRemoteRenderingView,
		IMultiFormChangeListener, IEventBasedSelectionManagerUser {

	public static String VIEW_TYPE = "org.caleydo.view.entourage";

	public static String VIEW_NAME = "Entourage";

	// private List<TablePerspective> tablePerspectives = new ArrayList<>();

	// private Set<String> remoteRenderedPathwayMultiformViewIDs;

	private final String pathEventSpace = GeneralManager.get().getEventPublisher().createUniqueEventSpace();

	private AnimatedGLElementContainer baseContainer = new AnimatedGLElementContainer(new GLSizeRestrictiveFlowLayout(
			true, 10, GLPadding.ZERO));
	private GLElementContainer root = new GLElementContainer(GLLayouts.LAYERS);
	private AnimatedGLElementContainer pathwayRow = new PathwayRowElement(this);
	// private AnimatedGLElementContainer pathwayRow = new AnimatedGLElementContainer();
	private GLEntourageAugmentation augmentation = new GLEntourageAugmentation(this);
	private AnimatedGLElementContainer nodeInfoContainer = new AnimatedGLElementContainer(
			new GLSizeRestrictiveFlowLayout(true, 10, GLPadding.ZERO));

	private GLEntourageWindow activeWindow = null;
	private GLPathwayWindow portalFocusWindow = null;

	private ShowPortalsAction showPortalsButton;
	// private HighlightAllPortalsAction highlightAllPortalsButton;

	protected GLEntourageWindow rankingWindow;

	// private List<IPathwayRepresentation> pathwayRepresentations = new ArrayList<>();

	private PathEventSpaceHandler pathEventSpaceHandler = new PathEventSpaceHandler();

	/**
	 * All segments of the currently selected path.
	 */
	private PathwayPath path = new PathwayPath();

	/**
	 * Info for the {@link MultiFormRenderer} of the selected path.
	 */
	private MultiFormInfo pathInfo;

	private DataMappingWizard dataMappingWizard;

	private boolean wasContextChanged = false;

	// /**
	// * All portals currently present.
	// */
	// protected Set<PathwayVertexRep> portals = new HashSet<>();

	/**
	 * List of infos for all pathways.
	 */
	protected List<PathwayMultiFormInfo> pathwayInfos = new ArrayList<>();

	protected static int currentPathwayAge = Integer.MAX_VALUE;

	protected MultiFormRenderer lastUsedRenderer;

	protected MultiFormRenderer lastUsedLevel1Renderer;

	protected GLPathwayGridLayout3 pathwayLayout = new GLPathwayGridLayout3(this, GLPadding.ZERO, 10);

	// protected GLExperimentalDataMapping experimentalDataMappingElement;

	/**
	 * Determines whether path selection mode is currently active.
	 */
	protected boolean isPathSelectionMode = false;

	/**
	 * Determines whether a new pathway was recently added. This information is needed to send events when dependent
	 * views need to be initialized in the first display cycle.
	 */
	protected boolean wasPathwayAdded = false;

	/**
	 * Determines whether the path window is maximized.
	 */
	protected boolean isPathWindowMaximized = false;

	/**
	 * Determines whether portal highlighting is currently enabled.
	 */
	protected boolean isShowPortals = true;

	private final DragAndDropController dndController = new DragAndDropController(this);

	/**
	 * The element that shows the ranked pathway list
	 */
	protected RankingElement rankingElement;

	/**
	 * The portal that is currently mouse-overed
	 */
	protected PathwayVertexRep currentPortalVertexRep;

	/**
	 * The vertex rep that is used for context path determination.
	 */
	private PathwayVertexRep currentContextVertexRep;

	private EventBasedSelectionManager vertexSelectionManager;

	// private EventBasedSelectionManager pathwaySelectionManager;

	private Map<Integer, PathwayVertexRep> allVertexReps = new HashMap<>();

	private List<Pair<PathwayVertexRep, PathwayVertexRep>> selectedPortalLinks = new ArrayList<>();

	// private GLWindow dataMappingWindow;

	/**
	 * Items that need to be added, but come from another thread.
	 */
	private List<AContextMenuItem> contextMenuItemsToShow = new ArrayList<>();

	protected Set<GLPathwayWindow> pinnedWindows = new HashSet<>();

	private GLEntourageWindow windowToSetActive;

	private ColoredConnectionBandRenderer connectionBandRenderer = null;

	private boolean isControlKeyPressed = false;
	private boolean isAltKeyPressed = false;
	private boolean isShiftKeyPressed = false;

	private PathwayVertex fromVertex;
	private PathwayVertex toVertex;

	public boolean showSrcWindowLinks = false;

	/**
	 * Reflects current state of data mapping.
	 */
	protected final DataMappingState dataMappingState;

	private boolean isEnRouteFirstTimeVisible = true;

	private SelectPathAction selectPathAction;

	private IWindowState pathOnlyState;
	private IWindowState smallEnrouteState;

	private MultiLevelSlideInElement enrouteSlideInElement;

	private ESampleMappingMode sampleMappingMode = ESampleMappingMode.ALL;

	private GLButton useCenterLineButton = new GLButton(EButtonMode.CHECKBOX);
	private GLButton fitEnrouteToViewWidthButton = new GLButton(EButtonMode.CHECKBOX);
	private GLButton useColorMappingButton = new GLButton(EButtonMode.CHECKBOX);

	/**
	 * Constructor.
	 *
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 */
	public GLEntourage(IGLCanvas glCanvas, ViewFrustum viewFrustum) {
		super(glCanvas, viewFrustum, VIEW_TYPE, VIEW_NAME);
		dataMappingState = new DataMappingState(this);
		// experimentalDataMappingElement = new GLExperimentalDataMapping(this);

		AnimatedGLElementContainer column = new AnimatedGLElementContainer(new GLSizeRestrictiveFlowLayout(false, 10,
				GLPadding.ZERO));
		column.add(baseContainer);
		nodeInfoContainer.setSize(Float.NaN, 0);
		// dataMappingWindow = new SideWindow("Data Mapping", this, SideWindow.SLIDE_BOTTOM_OUT);
		// dataMappingWindow.setDefaultInTransition(new InOutTransitions.InOutTransitionBase(InOutInitializers.TOP,
		// MoveTransitions.MOVE_LINEAR));
		// dataMappingWindow.setDefaultMoveTransition(MoveTransitions.GROW_LINEAR);
		// dataMappingWindow.setSize(Float.NaN, 80);
		// dataMappingWindow.setContent(experimentalDataMappingElement);
		// dataMappingWindow.setShowCloseButton(false);
		// SlideInElement slideInElement = new SlideInElement(dataMappingWindow, ESlideInElementPosition.TOP);
		// dataMappingWindow.addSlideInElement(slideInElement);

		vertexSelectionManager = new EventBasedSelectionManager(this, IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX_REP
				.name()));
		vertexSelectionManager.registerEventListeners();

		// column.add(dataMappingWindow);
		// column.add(nodeInfoContainer);
		rankingWindow = new SideWindow("Pathways", this, SideWindow.SLIDE_LEFT_OUT);

		rankingElement = new RankingElement(this);
		rankingWindow.setContent(rankingElement);
		rankingWindow.setSize(rankingElement.getRequiredWidth(), Float.NaN);
		SlideInElement slideInElement = new SlideInElement(rankingWindow, ESlideInElementPosition.RIGHT);
		slideInElement.setCallBack(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				AnimatedGLElementContainer anim = (AnimatedGLElementContainer) rankingWindow.getParent();
				if (selected) {
					anim.resizeChild(rankingWindow, rankingElement.getRequiredWidth(), Float.NaN);

				} else {
					anim.resizeChild(rankingWindow, 1, Float.NaN);
				}

			}
		});
		rankingWindow.addSlideInElement(slideInElement);
		rankingWindow.setShowCloseButton(false);
		rankingElement.setWindow(rankingWindow);
		baseContainer.add(rankingWindow);
		// pathwayRow.setLayout(new GLMultiFormPathwayLayout(10, GLPadding.ZERO, this, pathwayRow));
		pathwayRow.setLayout(pathwayLayout);

		// pathwayRow.setDefaultDuration(Durations.fix(600));
		// pathwayRow
		// .setDefaultInTransition(new InOutTransitionBase(InOutInitializers.RIGHT, MoveTransitions.MOVE_LINEAR));
		//
		baseContainer.add(pathwayRow);

		root.add(column);
		root.add(augmentation);
		//
		// PathwayDataDomain pathwayDataDomain = (PathwayDataDomain) DataDomainManager.get().getDataDomainByType(
		// "org.caleydo.datadomain.pathway");

		// pathwaySelectionManager = new EventBasedSelectionManager(t

		connectionBandRenderer = new ColoredConnectionBandRenderer();

	}

	protected HashSet<Pair<PathwayMultiFormInfo, PathwayMultiFormInfo>> windowStubs = new HashSet<Pair<PathwayMultiFormInfo, PathwayMultiFormInfo>>();
	protected HashSet<Pair<PathwayMultiFormInfo, PathwayMultiFormInfo>> windowStubsRightSide = new HashSet<Pair<PathwayMultiFormInfo, PathwayMultiFormInfo>>();

	protected void clearWindowStubSets() {
		windowStubs.clear();
		windowStubsRightSide.clear();
	}

	public boolean containsWindowsStub(Pair<PathwayMultiFormInfo, PathwayMultiFormInfo> windowPair) {
		if (!windowStubs.contains(windowPair)) {
			windowStubs.add(windowPair);
			return false;
		}
		return true;
	}

	public boolean containsWindowsStubRightSide(Pair<PathwayMultiFormInfo, PathwayMultiFormInfo> windowPair) {
		if (!windowStubsRightSide.contains(windowPair)) {
			windowStubsRightSide.add(windowPair);
			return false;
		}
		return true;
	}

	@Override
	public void init(GL2 gl) {
		super.init(gl);
		pathInfo = new MultiFormInfo();
		createSelectedPathMultiformRenderer(new ArrayList<>(dataMappingState.getTablePerspectives()),
				EnumSet.of(EEmbeddingID.PATH_LEVEL2, EEmbeddingID.PATH_LEVEL1), baseContainer, 0.3f, pathInfo);
		enrouteSlideInElement = new MultiLevelSlideInElement(pathInfo.window, ESlideInElementPosition.LEFT);
		enrouteSlideInElement.addWindowState(new IWindowState() {

			@Override
			public void apply() {
				rankingWindow.setVisibility(EVisibility.VISIBLE);
				pathwayRow.setVisibility(EVisibility.VISIBLE);
				rankingWindow.setVisibility(EVisibility.VISIBLE);
				pathInfo.window.setLayoutData(Float.NaN);
				AnimatedGLElementContainer anim = ((AnimatedGLElementContainer) pathInfo.window.getParent());
				anim.resizeChild(pathInfo.window, 1, Float.NaN);
				// pathInfo.window.background.setVisibility(EVisibility.NONE);
				// pathInfo.window.baseContainer.setVisibility(EVisibility.NONE);
				isPathWindowMaximized = false;
			}
		});
		pathOnlyState = new IWindowState() {

			@Override
			public void apply() {
				if (isPathWindowMaximized) {
					baseContainer.remove(0);
				}
				rankingWindow.setVisibility(EVisibility.VISIBLE);
				pathwayRow.setVisibility(EVisibility.VISIBLE);
				AnimatedGLElementContainer anim = (AnimatedGLElementContainer) pathInfo.window.getParent();
				pathInfo.window.setLayoutData(Float.NaN);
				anim.resizeChild(pathInfo.window, 150, Float.NaN);

				// pathInfo.window.background.setVisibility(EVisibility.PICKABLE);
				// pathInfo.window.baseContainer.setVisibility(EVisibility.VISIBLE);
				isPathWindowMaximized = false;
				pathInfo.multiFormRenderer.setActive(pathInfo.embeddingIDToRendererIDs.get(EEmbeddingID.PATH_LEVEL2)
						.get(0));
				augmentation.enable();
				isLayoutDirty = true;
			}
		};
		enrouteSlideInElement.addWindowState(pathOnlyState);
		smallEnrouteState = new IWindowState() {

			@Override
			public void apply() {
				if (isPathWindowMaximized) {
					baseContainer.remove(0);
				}
				rankingWindow.setVisibility(EVisibility.VISIBLE);
				pathwayRow.setVisibility(EVisibility.VISIBLE);
				// pathInfo.window.background.setVisibility(EVisibility.PICKABLE);
				// pathInfo.window.baseContainer.setVisibility(EVisibility.VISIBLE);
				isPathWindowMaximized = false;
				AnimatedGLElementContainer anim = (AnimatedGLElementContainer) pathInfo.window.getParent();
				anim.resizeChild(pathInfo.window, Float.NaN, Float.NaN);
				pathInfo.window.setLayoutData(0.3f);
				pathInfo.multiFormRenderer.setActive(pathInfo.embeddingIDToRendererIDs.get(EEmbeddingID.PATH_LEVEL1)
						.get(0));
				augmentation.enable();
				isLayoutDirty = true;
			}
		};

		enrouteSlideInElement.addWindowState(smallEnrouteState);
		enrouteSlideInElement.addWindowState(new IWindowState() {

			@Override
			public void apply() {
				if (isPathWindowMaximized) {
					baseContainer.remove(0);
				}
				rankingWindow.setVisibility(EVisibility.VISIBLE);
				pathwayRow.setVisibility(EVisibility.VISIBLE);
				// pathInfo.window.background.setVisibility(EVisibility.PICKABLE);
				// pathInfo.window.baseContainer.setVisibility(EVisibility.VISIBLE);
				isPathWindowMaximized = false;
				AnimatedGLElementContainer anim = (AnimatedGLElementContainer) pathInfo.window.getParent();
				anim.resizeChild(pathInfo.window, Float.NaN, Float.NaN);
				pathInfo.window.setLayoutData(0.5f);
				pathInfo.multiFormRenderer.setActive(pathInfo.embeddingIDToRendererIDs.get(EEmbeddingID.PATH_LEVEL1)
						.get(0));
				augmentation.enable();
				isLayoutDirty = true;
			}
		});
		enrouteSlideInElement.addWindowState(new IWindowState() {

			@Override
			public void apply() {
				rankingWindow.setVisibility(EVisibility.NONE);
				// Adding an element to get the gap is not so nice...
				GLElement element = new GLElement();
				element.setSize(0, Float.NaN);
				baseContainer.add(0, element);
				pathwayRow.setVisibility(EVisibility.NONE);
				pathInfo.window.setLayoutData(Float.NaN);
				AnimatedGLElementContainer anim = ((AnimatedGLElementContainer) pathInfo.window.getParent());
				anim.resizeChild(pathInfo.window, Float.NaN, Float.NaN);
				// pathInfo.window.background.setVisibility(EVisibility.PICKABLE);
				// pathInfo.window.baseContainer.setVisibility(EVisibility.VISIBLE);
				isPathWindowMaximized = true;
				augmentation.disable();
				updateAugmentation();
			}
		});
		enrouteSlideInElement.setCurrentWindowState(pathOnlyState);
		pathOnlyState.apply();

		pathInfo.window.addSlideInElement(enrouteSlideInElement);
		pathInfo.window.setShowCloseButton(false);
		pathInfo.window.viewSwitchingBar.setButtonToolTip("Show selected path only", pathInfo.embeddingIDToRendererIDs
				.get(EEmbeddingID.PATH_LEVEL2).get(0));
		pathInfo.window.viewSwitchingBar.setButtonToolTip("Show expanded enRoute with detailed experimental data",
				pathInfo.embeddingIDToRendererIDs.get(EEmbeddingID.PATH_LEVEL1).get(0));
		// pathInfo.window.setShowViewSwitchingBar(false);
		dataMappingWizard = new DataMappingWizard(this);
		pathInfo.window.addContentLayer(dataMappingWizard);
		// This assumes that a path level 2 view exists
		// if (pathInfo.multiFormRenderer.getActiveRendererID() != rendererID) {
		// pathInfo.multiFormRenderer.setActive(rendererID);
		// } else {
		setPathLevel(EEmbeddingID.PATH_LEVEL2);

		createEnRouteSpecificButtons();
		// }
		augmentation.init(gl);
		connectionBandRenderer.init(gl);
		registerListeners();
	}

	private void createEnRouteSpecificButtons() {

		int rendererID = pathInfo.embeddingIDToRendererIDs.get(EEmbeddingID.PATH_LEVEL1).get(0);
		useCenterLineButton.setSize(16, 16);
		// This is a bit hacky...
		final GLEnRoutePathway enRoute = (GLEnRoutePathway) pathInfo.multiFormRenderer.getView(rendererID);
		final IResourceLocator enrouteResourceLocator = org.caleydo.view.enroute.Activator.getResourceLocator();
		useCenterLineButton.setRenderer(GLRenderers.fillImage(new ResourceLoader(enrouteResourceLocator)
				.getTexture("resources/icons/center_data_line.png")));
		useCenterLineButton.setSelectedRenderer(GLRenderers.pushedImage(new ResourceLoader(enrouteResourceLocator)
				.getTexture("resources/icons/center_data_line.png")));
		useCenterLineButton.setSelected(enRoute.isShowCenteredDataLineInRowCenter());

		useCenterLineButton.onPick(new APickingListener() {
			@Override
			protected void clicked(Pick pick) {
				boolean showInCenter = enRoute.isShowCenteredDataLineInRowCenter();
				useCenterLineButton.setSelected(!showInCenter);
				enRoute.setShowCenteredDataLineInRowCenter(!showInCenter);
				enRoute.setLayoutDirty();
			}
		});
		useCenterLineButton.setTooltip("Toggle center line alignment for centered data.");

		fitEnrouteToViewWidthButton.setSize(16, 16);
		fitEnrouteToViewWidthButton.setRenderer(GLRenderers.fillImage(new ResourceLoader(enrouteResourceLocator)
				.getTexture("resources/icons/fit_to_width.png")));
		fitEnrouteToViewWidthButton.setSelectedRenderer(GLRenderers.pushedImage(new ResourceLoader(
				enrouteResourceLocator).getTexture("resources/icons/fit_to_width.png")));
		fitEnrouteToViewWidthButton.setSelected(enRoute.isFitWidthToScreen());

		fitEnrouteToViewWidthButton.onPick(new APickingListener() {
			@Override
			protected void clicked(Pick pick) {
				boolean fit = enRoute.isFitWidthToScreen();
				fitEnrouteToViewWidthButton.setSelected(!fit);
				EventPublisher.trigger(new FitToViewWidthEvent(!fit));
			}
		});
		fitEnrouteToViewWidthButton.setTooltip("Toggle fit to view width.");

		useColorMappingButton.setSize(16, 16);
		useColorMappingButton.setRenderer(GLRenderers.fillImage(new ResourceLoader(enrouteResourceLocator)
				.getTexture("resources/icons/toggle_color.png")));
		useColorMappingButton.setSelectedRenderer(GLRenderers.pushedImage(new ResourceLoader(enrouteResourceLocator)
				.getTexture("resources/icons/toggle_color.png")));
		useColorMappingButton.setSelected(enRoute.isUseColorMapping());

		useColorMappingButton.onPick(new APickingListener() {
			@Override
			protected void clicked(Pick pick) {
				boolean useColorMapping = enRoute.isUseColorMapping();
				useColorMappingButton.setSelected(!useColorMapping);
				enRoute.setUseColorMapping(!useColorMapping);
				enRoute.setLayoutDirty();
			}
		});
		useColorMappingButton.setTooltip("Toggle color mapping for numerical bars.");
	}

	/**
	 * @return the dndController, see {@link #dndController}
	 */
	public DragAndDropController getDndController() {
		return dndController;
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedEntourageView serializedForm = new SerializedEntourageView();
		return serializedForm;
	}

	protected void registerListeners() {

		parentGLCanvas.addMouseListener(new GLMouseAdapter() {

			@Override
			public void mousePressed(IMouseEvent mouseEvent) {
				if (mouseEvent.getButton() == 3 && activeWindow != null) {
					for (PathwayMultiFormInfo info : pathwayInfos) {
						if (info.window == activeWindow) {
							ShowCommonNodesPathwaysEvent event = new ShowCommonNodesPathwaysEvent(info.pathway);
							event.setEventSpace(pathEventSpace);
							// getContextMenuCreator().add(
							// new GenericContextMenuItem(
							// "Show Related Pathways with Common Nodes", event));
							contextMenuItemsToShow.add(new GenericContextMenuItem(
									"Show Related Pathways with Common Nodes", event));
						}
					}
				}

			}

			@Override
			public void mouseMoved(IMouseEvent mouseEvent) {
				Vec2f mousePosition = mouseEvent.getPoint();
				if (pathwayRow.getVisibility() != EVisibility.NONE) {
					for (PathwayMultiFormInfo info : pathwayInfos) {
						if (setWindowActive(mousePosition, info.window))
							return;
					}
				}
				if (pathInfo != null && pathInfo.window != null && setWindowActive(mousePosition, pathInfo.window))
					return;
				if (setWindowActive(mousePosition, rankingWindow))
					return;
				// if (setWindowActive(mousePosition, dataMappingWindow))
				// return;
			}

			private boolean setWindowActive(Vec2f mousePosition, GLEntourageWindow window) {
				Vec2f location = window.getAbsoluteLocation();
				Vec2f size = window.getSize();
				if ((mousePosition.x() >= location.x() && mousePosition.x() <= location.x() + size.x())
						&& (mousePosition.y() >= location.y() && mousePosition.y() <= location.y() + size.y())) {
					windowToSetActive = window;
					return true;
				}
				return false;
			}

			@Override
			public void mouseExited(IMouseEvent mouseEvent) {
			}

			@Override
			public void mouseEntered(IMouseEvent mouseEvent) {
			}

			@Override
			public void mouseDragged(IMouseEvent mouseEvent) {
			}

			@Override
			public void mouseClicked(IMouseEvent mouseEvent) {

			}
		});
		parentGLCanvas.addKeyListener(new IGLKeyListener() {
			@Override
			public void keyPressed(IKeyEvent e) {
				update(e);
				isControlKeyPressed = e.isControlDown();
				isAltKeyPressed = e.isAltDown();
				isShiftKeyPressed = e.isShiftDown();
			}

			@Override
			public void keyReleased(IKeyEvent e) {
				isControlKeyPressed = e.isControlDown();
				isAltKeyPressed = e.isAltDown();
				isShiftKeyPressed = e.isShiftDown();
				// update(e);
			}

			private void update(IKeyEvent e) {
				boolean isPPressed = e.isKeyDown('p');
				// augmentation.showPortals(isPPressed);
				if (isPPressed) {
					// boolean isOPressed = e.isKeyDown('o');
					ShowPortalsEvent event = new ShowPortalsEvent(!showPortalsButton.isChecked());
					showPortalsButton.setChecked(!showPortalsButton.isChecked());
					event.setEventSpace(pathEventSpace);
					EventPublisher.INSTANCE.triggerEvent(event);
				}
				// highlightAllPortalsButton.setChecked(isOPressed);
				boolean iswPressed = e.isKeyDown('w');
				// augmentation.showPortals(isPPressed);
				if (iswPressed) {
					if (showSrcWindowLinks)
						showSrcWindowLinks = false;
					else
						showSrcWindowLinks = true;
				}

				if (e.isKey('b')) { // ctrl +o
					if (selectPathAction != null) {

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								boolean enable = !selectPathAction.isChecked();
								EnablePathSelectionEvent event = new EnablePathSelectionEvent(enable);
								event.setEventSpace(pathEventSpace);
								GeneralManager.get().getEventPublisher().triggerEvent(event);
								selectPathAction.setChecked(enable);
							}
						});
					}
				}
			}
		});
	}

	@Override
	public String toString() {
		return "Subgraph view";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		eventListeners.register(pathEventSpaceHandler, pathEventSpace);
	}

	public EventListenerManager getEventListenerManager() {
		return eventListeners;
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();
		vertexSelectionManager.unregisterEventListeners();
		// pathwaySelectionManager.unregisterEventListeners();
	}

	@Override
	protected void destroyViewSpecificContent(GL2 gl) {
		gl.glDeleteLists(displayListIndex, 1);
	}

	public void addPathway(PathwayGraph pathway, EEmbeddingID level) {

		PathwayMultiFormInfo info = new PathwayMultiFormInfo();
		info.pathway = pathway;

		info.age = currentPathwayAge--;
		createPathwayMultiFormRenderer(pathway, EnumSet.of(EEmbeddingID.PATHWAY_LEVEL1, EEmbeddingID.PATHWAY_LEVEL2,
				EEmbeddingID.PATHWAY_LEVEL3, EEmbeddingID.PATHWAY_LEVEL4), pathwayRow, Float.NaN, info);
		pathwayLayout.addColumn((GLPathwayWindow) info.window);
		for (PathwayVertexRep vertexRep : pathway.vertexSet()) {
			allVertexReps.put(vertexRep.getID(), vertexRep);
		}
		int rendererID = info.embeddingIDToRendererIDs.get(level).get(0);
		if (info.multiFormRenderer.getActiveRendererID() != rendererID) {
			info.multiFormRenderer.setActive(rendererID);
		}
		if (level == EEmbeddingID.PATHWAY_LEVEL1) {
			lastUsedLevel1Renderer = info.multiFormRenderer;
		}
		lastUsedRenderer = info.multiFormRenderer;

		pathwayInfos.add(info);

		wasPathwayAdded = true;
		isLayoutDirty = true;
	}

	private void createPathwayMultiFormRenderer(PathwayGraph pathway, EnumSet<EEmbeddingID> embeddingIDs,
			final AnimatedGLElementContainer parent, Object layoutData, PathwayMultiFormInfo info) {
		MultiFormRenderer renderer = new MultiFormRenderer(this, false);
		renderer.addChangeListener(this);
		// renderer.setUseScreenCoordinateViewFrustum(true);
		info.multiFormRenderer = renderer;

		for (EEmbeddingID embedding : embeddingIDs) {
			IPathwayRepresentation pathwayRepresentation = PathwayViews.getPathwayRepresenation(this, pathway,
					dataMappingState.getTablePerspectives(), dataMappingState.getPathwayMappedTablePerspective(),
					pathEventSpace, embedding);
			if (pathwayRepresentation == null)
				continue;
			String iconPath = PathwayViews.getPathwayIconPath(this, embedding);

			AGLView view = pathwayRepresentation.asAGLView();
			ALayoutRenderer layoutRenderer = pathwayRepresentation.asLayoutRenderer();
			int rendererID = -1;
			if (view != null) {
				rendererID = renderer.addView(view, iconPath, new DefaultVisInfo(), false, false);
			} else if (layoutRenderer != null) {
				rendererID = renderer.addLayoutRenderer(layoutRenderer, iconPath, new DefaultVisInfo(), false);
			}

			List<Integer> rendererIDList = info.embeddingIDToRendererIDs.get(embedding);
			if (rendererIDList == null) {
				rendererIDList = new ArrayList<>();
				info.embeddingIDToRendererIDs.put(embedding, rendererIDList);
			}
			rendererIDList.add(rendererID);
			initPathwayRepresentation(pathwayRepresentation);
			info.pathwayRepresentations.put(rendererID, pathwayRepresentation);
		}

		final GLPathwayWindow pathwayWindow = new GLPathwayWindow(pathway.getTitle(), this, info, false);
		pathwayWindow.onClose(new ICloseWindowListener() {
			@Override
			public void onWindowClosed(GLElementWindow w) {
				removePathwayWindow(pathwayWindow);
			}
		});

		info.window = pathwayWindow;
		parent.add(pathwayWindow);
	}

	private void createSelectedPathMultiformRenderer(List<TablePerspective> tablePerspectives,
			EnumSet<EEmbeddingID> embeddingIDs, final AnimatedGLElementContainer parent, Object layoutData,
			MultiFormInfo info) {

		// GLElementContainer backgroundContainer = new GLElementContainer(GLLayouts.LAYERS);
		// backgroundContainer.setLayoutData(layoutData);

		// Different renderers should receive path updates from the beginning on, therefore no lazy creation.
		MultiFormRenderer renderer = new MultiFormRenderer(this, false);
		renderer.addChangeListener(this);
		// renderer.setUseScreenCoordinateViewFrustum(true);
		info.multiFormRenderer = renderer;

		for (EEmbeddingID embedding : embeddingIDs) {
			String embeddingID = embedding.id();
			Set<String> ids = ViewManager.get().getRemotePlugInViewIDs(VIEW_TYPE, embeddingID);

			for (String viewID : ids) {

				List<Integer> rendererIDList = info.embeddingIDToRendererIDs.get(embedding);
				if (rendererIDList == null) {
					rendererIDList = new ArrayList<>();
					info.embeddingIDToRendererIDs.put(embedding, rendererIDList);
				}

				int rendererID = renderer.addPluginVisualization(viewID, getViewType(), embeddingID, tablePerspectives,
						pathEventSpace);
				rendererIDList.add(rendererID);
			}
		}
		GLMultiFormWindow window = new GLMultiFormWindow("enRoute - experimental data for paths", this, info, true);

		info.window = window;
		parent.add(window);
	}

	private void initPathwayRepresentation(IPathwayRepresentation pathwayRepresentation) {

		// PathwayGraph pathway = pathwayRepresentation.getPathway();
		// pathwayRepresentation.addVertexRepBasedContextMenuItem(new VertexRepBasedContextMenuItem(
		// "Show Node Info", ShowNodeInfoEvent.class, pathEventSpace));
		// pathwayRepresentation.addVertexRepBasedContextMenuItem(new ShowCommonNodeItem(
		// ShowCommonNodePathwaysEvent.class, pathEventSpace));
		// pathwayRepresentation.addVertexRepBasedContextMenuItem(new VertexRepBasedContextMenuItem("Show Context",
		// ShowNodeContextEvent.class, pathEventSpace));

		pathwayRepresentation.addVertexRepSelectionListener(new IVertexRepSelectionListener() {

			@Override
			public void onSelect(PathwayVertexRep vertexRep, Pick pick) {
				switch (pick.getPickingMode()) {
				case CLICKED:
					new ShowNodeContextEventFactory(pathEventSpace, GLEntourage.this).triggerEvent(vertexRep);
					// Temporary
					if (isAltKeyPressed) {
						if (fromVertex == null || toVertex != null) {
							fromVertex = vertexRep.getPathwayVertices().get(0);
							toVertex = null;
						} else {
							toVertex = vertexRep.getPathwayVertices().get(0);
						}
						if (fromVertex != null && toVertex != null) {
							System.out.println("From: " + fromVertex.getHumanReadableName() + ", To: "
									+ toVertex.getHumanReadableName());
							PathwayManager.get().getShortestPaths(fromVertex, toVertex);
						}
					}

					break;
				case MOUSE_OVER:
					new SelectPathwayEventFactory(pathEventSpace).triggerEvent(vertexRep);
					break;
				case DOUBLE_CLICKED:
					new AddPathwayEventFactory(pathEventSpace).triggerEvent(vertexRep);
					break;
				case RIGHT_CLICKED:
					VertexRepBasedContextMenuItem item = new VertexRepBasedContextMenuItem("Show Context",
							ShowNodeContextEvent.class, pathEventSpace);
					item.setVertexRep(vertexRep);
					getContextMenuCreator().add(item);
					// contextMenuItemsToShow.add(item);
					break;
				default:
					break;
				}

			}
		});

		// pathwayRepresentation.addVertexRepBasedSelectionEvent(
		// new ShowNodeContextEventFactory(pathEventSpace, this), PickingMode.CLICKED);
		// pathwayRepresentation.addVertexRepBasedSelectionEvent(new AddPathwayEventFactory(pathEventSpace),
		// PickingMode.DOUBLE_CLICKED);
		// pathwayRepresentation.addVertexRepBasedSelectionEvent(new SelectPathwayEventFactory(pathEventSpace),
		// PickingMode.MOUSE_OVER);

	}

	private void removePathwayWindow(GLPathwayWindow pathwayWindow) {
		pathwayLayout.removeWindow(pathwayWindow);
		((AnimatedGLElementContainer) pathwayWindow.getParent()).remove(pathwayWindow);
		pathwayInfos.remove(pathwayWindow.info);
		for (PathwayVertexRep vertexRep : ((PathwayMultiFormInfo) (pathwayWindow.info)).pathway.vertexSet()) {
			allVertexReps.remove(vertexRep.getID());
		}
		if (activeWindow == pathwayWindow) {
			activeWindow = null;
			portalFocusWindow = null;
		}
		List<PathwayMultiFormInfo> lv1Infos = getPathwayInfosWithLevel(EEmbeddingID.PATHWAY_LEVEL1);
		if (lv1Infos.isEmpty()) {
			List<PathwayMultiFormInfo> infos = new ArrayList<>(pathwayInfos);
			Collections.sort(infos, new Comparator<PathwayMultiFormInfo>() {
				@Override
				public int compare(PathwayMultiFormInfo info1, PathwayMultiFormInfo info2) {
					return info1.age - info2.age;
				}
			});
			for (PathwayMultiFormInfo info : infos) {
				if (!pinnedWindows.contains(info.window)) {
					int rendererID = info.embeddingIDToRendererIDs.get(EEmbeddingID.PATHWAY_LEVEL1).get(0);
					if (info.multiFormRenderer.getActiveRendererID() != rendererID) {
						info.multiFormRenderer.setActive(rendererID);
					}

					lastUsedLevel1Renderer = info.multiFormRenderer;
					lastUsedRenderer = info.multiFormRenderer;
					break;
				}
			}
		}
		updateAugmentation();
		isLayoutDirty = true;
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		// TODO: implement
		return null;
	}

	@Override
	public boolean isDataView() {
		return true;
	}

	@Override
	protected GLElement createRoot() {

		return root;
	}

	protected Rect getAbsoluteVertexLocation(IPathwayRepresentation pathwayRepresentation, PathwayVertexRep vertexRep,
			GLElement element) {

		Vec2f elementPosition = element.getAbsoluteLocation();
		Rect location = pathwayRepresentation.getVertexRepBounds(vertexRep);
		if (location != null) {
			return new Rect(location.x() + elementPosition.x(), location.y() + elementPosition.y(), location.width(),
					location.height());
			// return new Rectangle2D.Float((elementPosition.x()), (elementPosition.y()), (float) location.getWidth(),
			// (float) location.getHeight());

		}
		return null;
	}

	public void updateAugmentation() {
		clearWindowStubSets();
		updatePathwayPortals();
	}

	public void setLayoutDirty() {
		isLayoutDirty = true;
	}

	@Override
	public void display(GL2 gl) {

		clearWindowStubSets();
		if (windowToSetActive != null) {
			windowToSetActive.setActive(true);
			windowToSetActive = null;
		}
		boolean updateAugmentation = false;
		if (isLayoutDirty)
			updateAugmentation = true;

		final boolean isPickingRun = GLGraphics.isPickingPass(gl);
		if (!isPickingRun) {
			if (!contextMenuItemsToShow.isEmpty()) {
				for (AContextMenuItem item : contextMenuItemsToShow) {
					getContextMenuCreator().add(item);
				}
				contextMenuItemsToShow.clear();
			}
		}

		super.display(gl);

		if (wasContextChanged) {
			// We need to remove and add it again, otherwise there is no selection delta, as the currentContextVertexRep
			// is already selected
			vertexSelectionManager.clearSelection(SelectionType.SELECTION);
			// vertexSelectionManager.removeFromType(SelectionType.SELECTION, currentContextVertexRep.getID());
			vertexSelectionManager.addToType(SelectionType.SELECTION, currentContextVertexRep.getID());
			vertexSelectionManager.triggerSelectionUpdateEvent();
			wasContextChanged = false;
		}

		// for (AContextMenuItem item : contextMenuItemsToShow) {
		// getContextMenuCreator().add(item);
		// }
		if (wasPathwayAdded) {
			EnablePathSelectionEvent event = new EnablePathSelectionEvent(isPathSelectionMode);
			event.setEventSpace(pathEventSpace);
			eventPublisher.triggerEvent(event);

			PathwayMappingEvent pathwayMappingEvent = new PathwayMappingEvent(
					dataMappingState.getPathwayMappedTablePerspective());
			event.setEventSpace(pathEventSpace);
			EventPublisher.trigger(pathwayMappingEvent);
			EventPublisher.trigger(new SampleMappingModeEvent(sampleMappingMode));

			if (currentContextVertexRep != null) {
				ShowNodeContextEvent e = new ShowNodeContextEvent(currentContextVertexRep);
				e.setEventSpace(pathEventSpace);
				e.setSender(this);
				eventPublisher.triggerEvent(e);
			}
			wasPathwayAdded = false;
		}
		// The augmentation has to be updated after the layout was updated in super; updating on relayout would be too
		// early, as the layout is not adapted at that time.
		if (updateAugmentation) {
			updateAugmentation();
		}
		// }

		// call after all other rendering because it calls the onDrag
		// methods
		// which need alpha blending...
		dndController.handleDragging(gl, glMouseListener);

		// wasContextChanged = false;
	}

	@Override
	public void activeRendererChanged(MultiFormRenderer multiFormRenderer, int rendererID, int previousRendererID,
			boolean wasTriggeredByUser) {

		if (wasTriggeredByUser && rendererID != previousRendererID) {
			for (PathwayMultiFormInfo info : pathwayInfos) {
				if (info.multiFormRenderer == multiFormRenderer) {
					if (info.getEmbeddingIDFromRendererID(rendererID) == EEmbeddingID.PATHWAY_LEVEL1) {
						pathwayLayout.setLevel1((GLPathwayWindow) info.window);
						lastUsedLevel1Renderer = info.multiFormRenderer;
						info.age = currentPathwayAge--;
					}
					lastUsedRenderer = info.multiFormRenderer;
					break;
				}
			}

			pathwayRow.relayout();
		}

		if (multiFormRenderer == pathInfo.multiFormRenderer) {
			EEmbeddingID level = pathInfo.getEmbeddingIDFromRendererID(rendererID);
			if (dataMappingWizard != null)
				dataMappingWizard.onPathLevelChanged();
			if (wasTriggeredByUser) {
				setPathLevel(level);
			}
			if (pathInfo.window != null) {
				if (level == EEmbeddingID.PATH_LEVEL1) {
					if (isEnRouteFirstTimeVisible) {
						DataMappers.getDataMapper().show();
						isEnRouteFirstTimeVisible = false;
					}
					pathInfo.window.getTitleBar().add(pathInfo.window.getTitleBar().size() - 1,
							fitEnrouteToViewWidthButton);
					pathInfo.window.getTitleBar().add(pathInfo.window.getTitleBar().size() - 1, useColorMappingButton);
					pathInfo.window.getTitleBar().add(pathInfo.window.getTitleBar().size() - 1, useCenterLineButton);
				} else {
					pathInfo.window.getTitleBar().remove(fitEnrouteToViewWidthButton);
					pathInfo.window.getTitleBar().remove(useColorMappingButton);
					pathInfo.window.getTitleBar().remove(useCenterLineButton);
				}
			}
		}
	}

	public void setPathLevel(EEmbeddingID embeddingID) {
		if (embeddingID == null)
			return;
		if (embeddingID == EEmbeddingID.PATH_LEVEL1) {
			enrouteSlideInElement.setCurrentWindowState(smallEnrouteState);
			smallEnrouteState.apply();
		} else if (embeddingID == EEmbeddingID.PATH_LEVEL2) {
			enrouteSlideInElement.setCurrentWindowState(pathOnlyState);
			pathOnlyState.apply();
		}
		if (dataMappingWizard != null)
			dataMappingWizard.onPathLevelChanged();
	}

	@Override
	public void rendererAdded(MultiFormRenderer multiFormRenderer, int rendererID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rendererRemoved(MultiFormRenderer multiFormRenderer, int rendererID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroyed(MultiFormRenderer multiFormRenderer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the currentActiveBackground, see {@link #activeWindow}
	 */
	public GLElementWindow getActiveWindow() {
		return activeWindow;
	}

	/**
	 * @param currentActiveBackground
	 *            setter, see {@link currentActiveBackground}
	 */
	public void setActiveWindow(GLEntourageWindow activeWindow) {
		if (activeWindow != null && this.activeWindow != null && activeWindow != this.activeWindow) {
			this.activeWindow.setActive(false);
		}
		if (activeWindow instanceof GLPathwayWindow) {
			portalFocusWindow = (GLPathwayWindow) activeWindow;
		}

		this.activeWindow = activeWindow;
		isLayoutDirty = true;
		// clearSelectedPortalLinks();
		// updatePathwayPortals();

	}

	/**
	 * @return the pathEventSpace, see {@link #pathEventSpace}
	 */
	public String getPathEventSpace() {
		return pathEventSpace;
	}

	/**
	 * Info about container, embeddings, etc. for each {@link MultiFormRenderer} in this view.
	 *
	 * @author Christian
	 *
	 */
	protected class MultiFormInfo {
		/**
		 * The multiform renderer.
		 */
		protected MultiFormRenderer multiFormRenderer;
		/**
		 * The element that represents the "window" of the multiform renderer, which includes a title bar. This element
		 * should be used for resizing.
		 */
		protected GLMultiFormWindow window;
		/**
		 * The parent {@link GLElementAdapter} of this container.
		 */
		protected GLElementAdapter container;
		protected Map<EEmbeddingID, List<Integer>> embeddingIDToRendererIDs = new HashMap<>();

		protected boolean isInitialized() {
			return multiFormRenderer != null && window != null && container != null && embeddingIDToRendererIDs != null;
		}

		protected EEmbeddingID getEmbeddingIDFromRendererID(int rendererID) {
			for (Entry<EEmbeddingID, List<Integer>> entry : embeddingIDToRendererIDs.entrySet()) {
				List<Integer> rendererIDs = entry.getValue();
				if (rendererIDs.contains(rendererID)) {
					return entry.getKey();
				}
			}
			return null;
		}

		protected EEmbeddingID getCurrentEmbeddingID() {
			return getEmbeddingIDFromRendererID(multiFormRenderer.getActiveRendererID());
		}
	}

	/**
	 * Same as {@link MultiFormInfo}, but especially for MultiFormRenderers of pathways.
	 *
	 * @author Christian
	 *
	 */
	protected class PathwayMultiFormInfo extends MultiFormInfo {
		protected PathwayGraph pathway;
		protected Map<Integer, IPathwayRepresentation> pathwayRepresentations = new HashMap<>();
		protected int age;

		@Override
		protected boolean isInitialized() {
			return super.isInitialized() && pathway != null && pathwayRepresentations != null;
		}

		public IPathwayRepresentation getCurrentPathwayRepresentation() {
			return pathwayRepresentations.get(multiFormRenderer.getActiveRendererID());
		}
	}

	protected ArrayList<Rectangle2D> portalRects = new ArrayList<>();

	@ListenTo(sendToMe = true)
	public void onClearWorkspace(ClearWorkspaceEvent event) {
		for (PathwayMultiFormInfo info : new ArrayList<PathwayMultiFormInfo>(pathwayInfos)) {
			removePathwayWindow((GLPathwayWindow) info.window);
		}
	}

	@ListenTo
	public void onShowPathwaysWithGene(LoadPathwaysByGeneEvent event) {
		Set<PathwayGraph> pathways = PathwayManager.get()
				.getPathwayGraphsByGeneID(event.getIdType(), event.getGeneID());
		if (pathways == null)
			pathways = new HashSet<>();
		rankingElement.setFilter(new PathwayFilters.PathwaySetFilter(pathways));

	}

	@ListenTo
	public void onShowPathwaysWithGene(LoadPathwaysEvent event) {
		Set<PathwayGraph> pathways = event.getPathways();
		if (pathways == null)
			pathways = new HashSet<>();
		rankingElement.setFilter(new PathwayFilters.PathwaySetFilter(pathways));

	}

	@ListenTo
	public void onSampleMappingModeChanged(SampleMappingModeEvent event) {
		this.sampleMappingMode = event.getSampleMappingMode();
	}

	private class PathEventSpaceHandler {

		// @ListenTo(restrictExclusiveToEventSpace = true)
		// public void onShowPortalNodes(ShowPortalNodesEvent event) {
		// currentPortalVertexRep = event.getVertexRep();
		//
		// }

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onPathSelection(PathwayPathSelectionEvent event) {
			path = event.getPath();
			if (path.size() > 0) {
				PathSegment segment = path.getLast();
				PathwayMultiFormInfo info = getPathwayMultiFormInfo(segment.getPathway());
				if (info != null) {
					lastUsedRenderer = info.multiFormRenderer;
				}
			}
			updatePathwayPortals();
		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onMinSizeUpdate(MinSizeUpdateEvent event) {
			pathwayRow.relayout();
			isLayoutDirty = true;
		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onShowPathwaysWithVertex(ShowCommonNodesPathwaysEvent event) {
			rankingElement.setFilter(new PathwayFilters.CommonVerticesFilter(event.getPathway(), false));
			rankingElement.setRanking(new PathwayRankings.CommonVerticesRanking(event.getPathway()));
			isLayoutDirty = true;
		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onEnablePathSelection(EnablePathSelectionEvent event) {
			isPathSelectionMode = event.isPathSelectionMode();
		}

		// @ListenTo(restrictExclusiveToEventSpace = true)
		// public void onHighlightAllPortals(HighlightAllPortalsEvent event) {
		//
		//
		// }

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onShowPortalLinks(ShowPortalsEvent event) {
			// augmentation.showPortals(event.isShowPortals());
			isShowPortals = event.isShowPortals();
			updatePathwayPortals();
		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onShowNodeContext(ShowNodeContextEvent event) {
			currentContextVertexRep = event.getVertexRep();
			if (event.getSender() == GLEntourage.this)
				return;

			// Try to promote views that have context
			for (PathwayMultiFormInfo info : pathwayInfos) {
				if (hasPathwayCurrentContext(info.pathway)
						&& info.getCurrentEmbeddingID() != EEmbeddingID.PATHWAY_LEVEL1
						&& info.multiFormRenderer != lastUsedRenderer) {
					info.multiFormRenderer.setActive(info.embeddingIDToRendererIDs.get(EEmbeddingID.PATHWAY_LEVEL2)
							.get(0));
					// info.age = currentPathwayAge--;
				}
			}
			rankingElement.setFilter(new PathwayFilters.CommonVertexFilter(currentContextVertexRep, false));
			rankingElement.setRanking(new PathwayRankings.CommonVerticesRanking(currentContextVertexRep.getPathway()));
			wasContextChanged = true;
			updatePathwayPortals();
		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onAddPathway(AddPathwayEvent event) {
			PathwayGraph pathway = event.getPathway();
			if (pathway != null && !hasPathway(pathway)) {
				addPathway(pathway, event.getPathwayLevel());
			}
		}

		// @ListenTo(restrictExclusiveToEventSpace = true)
		// public void onPathwayTextureSelection(PathwayTextureSelectionEvent event) {
		// for (IPathwayTextureSelectionListener listener : textureSelectionListeners) {
		// listener.onPathwayTextureSelected(event.getPathway());
		// }
		// }
	}

	public boolean hasPathway(PathwayGraph pathway) {
		return getPathwayMultiFormInfo(pathway) != null;
	}

	public PathwayMultiFormInfo getPathwayMultiFormInfo(PathwayGraph pathway) {
		for (PathwayMultiFormInfo info : pathwayInfos) {
			if (info.pathway == pathway)
				return info;
		}
		return null;
	}

	private PathwayMultiFormInfo getInfo(PathwayVertexRep vertexRep) {
		for (PathwayMultiFormInfo info : pathwayInfos) {
			if (info.pathway == vertexRep.getPathway())
				return info;
		}
		return null;
	}

	private List<PathwayMultiFormInfo> getPathwayInfosWithLevel(EEmbeddingID level) {
		List<PathwayMultiFormInfo> infos = new ArrayList<>();
		for (PathwayMultiFormInfo info : pathwayInfos) {
			if (info.getCurrentEmbeddingID() == level) {
				infos.add(info);
			}
		}
		return infos;
	}

	protected void updatePathwayPortals() {
		PathwayMultiFormInfo info = null;
		if (portalFocusWindow != null) {
			if (portalFocusWindow.info instanceof PathwayMultiFormInfo) {
				info = (PathwayMultiFormInfo) portalFocusWindow.info;

			}
		}

		// for (PathwayMultiFormInfo i : pathwayInfos) {
		// i.window.setTitleBarColor(GLTitleBar.DEFAULT_COLOR);
		// }

		augmentation.clear();
		clearWindowStubSets();
		if (info == null)
			return;
		// textureSelectionListeners.clear();
		PathwayVertexRep lastNodeOfPrevSegment = null;

		for (PathSegment segment : path) {
			if (!segment.isEmpty()) {
				if (lastNodeOfPrevSegment != null) {
					PathwayMultiFormInfo info1 = getInfo(lastNodeOfPrevSegment);
					PathwayMultiFormInfo info2 = getInfo(segment.get(0));
					if (pathwayRow.getVisibility() == EVisibility.NONE || info1 == null || info2 == null)
						continue;
					Rect loc1 = getAbsoluteVertexLocation(info1.getCurrentPathwayRepresentation(),
							lastNodeOfPrevSegment, info1.container);
					Rect loc2 = getAbsoluteVertexLocation(info2.getCurrentPathwayRepresentation(), segment.get(0),
							info2.container);
					augmentation.add(new LinkRenderer(this, true, loc1, loc2, info1, info2, 1, false, false, false,
							true, lastNodeOfPrevSegment, segment.get(0), connectionBandRenderer));
				}

				lastNodeOfPrevSegment = segment.get(segment.size() - 1);
			}
		}
		if (pathwayRow.getVisibility() != EVisibility.NONE) {
			for (PathwayMultiFormInfo i : pathwayInfos) {
				if (i.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL1) {
					highlightPathwayNodePortals(i);
				}
			}
		}

		// Set<GLPathwayWindow> windowsToHighlight = new HashSet<>();

		for (PathwayVertexRep vertexRep : info.pathway.vertexSet()) {
			// if (info.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL1
			// && vertexRep.getType() == EPathwayVertexType.map) {
			// // addPortalHighlightRenderer(vertexRep, info);
			// continue;
			// }
			Pair<Rect, Boolean> sourcePair = getPortalLocation(vertexRep, info);
			for (PathwayMultiFormInfo i : pathwayInfos) {
				if (info != i) {
					// boolean wasLinkAdded = false;
					addLinkRenderers(vertexRep, info, i, sourcePair);
					// boolean highlightAdded = highlightPathwayNodePortals(info, i);
					// wasLinkAdded = wasLinkAdded || highlightAdded;
					// if (wasLinkAdded) {
					// windowsToHighlight.add((GLPathwayWindow) i.window);
					// }
				} else {
					// TODO: implement
				}

			}
		}
		// if (isShowPortals) {
		// for (PathwayMultiFormInfo i : pathwayInfos) {
		// if (windowsToHighlight.contains(i.window)) {
		// i.window.setTitleBarColor(PortalRenderStyle.DEFAULT_PORTAL_COLOR);
		// } else {
		// i.window.setTitleBarColor(GLTitleBar.DEFAULT_COLOR);
		// }
		// }
		// }

		// clearSelectedPortalLinks();
		// System.out.println("update");
	}

	private void highlightPathwayNodePortals(PathwayMultiFormInfo info) {
		if (info.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL1) {
			for (PathwayVertexRep vertexRep : info.pathway.vertexSet()) {
				if (vertexRep.getType() == EPathwayVertexType.map) {
					PathwayGraph pathway = PathwayManager.get().getPathwayByTitle(vertexRep.getName(),
							EPathwayDatabaseType.KEGG);
					PathwayMultiFormInfo target = getPathwayMultiFormInfo(pathway);
					if (target != null) {
						PortalHighlightRenderer renderer = new PortalHighlightRenderer(info, getPortalLocation(
								vertexRep, info).getFirst(), (GLPathwayWindow) target.window);
						// textureSelectionListeners.add(renderer);
						augmentation.add(renderer);
					}
				}
			}
		}

		// boolean wasHighlighted = false;
		// if (targetInfo.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL1) {
		// for (PathwayVertexRep vertexRep : targetInfo.pathway.vertexSet()) {
		// if (vertexRep.getType() == EPathwayVertexType.map) {
		// PathwayGraph pathway = PathwayManager.get().getPathwayByTitle(vertexRep.getName(),
		// EPathwayDatabaseType.KEGG);
		// if (pathway == sourceInfo.pathway) {
		// PortalHighlightRenderer renderer = new PortalHighlightRenderer(getPortalLocation(vertexRep,
		// targetInfo).getFirst(), (GLPathwayWindow) sourceInfo.window);
		// // textureSelectionListeners.add(renderer);
		// augmentation.add(renderer);
		// wasHighlighted = true;
		// }
		// }
		// }
		// }
		// return wasHighlighted;
	}

	private boolean addPortalHighlightRenderer(PathwayVertexRep vertexRep, PathwayMultiFormInfo info) {
		PathwayGraph pathway = PathwayManager.get().getPathwayByTitle(vertexRep.getName(), EPathwayDatabaseType.KEGG);
		boolean wasHighlighted = false;
		if (pathway != null) {
			PathwayMultiFormInfo windowInfo = getPathwayMultiFormInfo(pathway);
			if (windowInfo != null) {
				PortalHighlightRenderer renderer = new PortalHighlightRenderer(info, getPortalLocation(vertexRep, info)
						.getFirst(), (GLPathwayWindow) windowInfo.window);
				// textureSelectionListeners.add(renderer);
				augmentation.add(renderer);
				wasHighlighted = true;
			}
		}
		return wasHighlighted;
	}

	private boolean addLinkRenderers(PathwayVertexRep vertexRep, PathwayMultiFormInfo sourceInfo,
			PathwayMultiFormInfo targetInfo, Pair<Rect, Boolean> sourcePair) {
		boolean isContextPortal = PathwayManager.get().areVerticesEquivalent(vertexRep, currentContextVertexRep);
		if (!isShowPortals && !isContextPortal)
			return false;
		boolean wasLinkAdded = false;
		Set<PathwayVertexRep> equivalentVertexReps = PathwayManager.get().getEquivalentVertexRepsInPathway(vertexRep,
				targetInfo.pathway);
		clearWindowStubSets();
		for (PathwayVertexRep v : equivalentVertexReps) {

			if (isPathLink(vertexRep, v) || pathwayRow.getVisibility() == EVisibility.NONE)
				continue;

			Pair<Rect, Boolean> targetPair = getPortalLocation(v, targetInfo);

			// System.out.println("Add link from: " + vertexRep.getShortName() + " to " + v.getShortName() + "("
			// + sourceLocation + " to " + targetLocation + ")");
			float stubSize = Math.max(
					1,
					Math.abs(pathwayLayout.getColumnIndex((GLPathwayWindow) sourceInfo.window)
							- pathwayLayout.getColumnIndex((GLPathwayWindow) targetInfo.window)));
			LinkRenderer renderer = new LinkRenderer(this, vertexRep == currentPortalVertexRep
					|| v == currentPortalVertexRep || isSelectedPortalLink(vertexRep, v), sourcePair.getFirst(),
					targetPair.getFirst(), sourceInfo, targetInfo, stubSize, sourcePair.getSecond(),
					targetPair.getSecond(), isContextPortal, false, vertexRep, v, connectionBandRenderer);
			augmentation.add(renderer);
			wasLinkAdded = true;
		}
		return wasLinkAdded;
	}

	protected boolean isSelectedPortalLink(PathwayVertexRep v1, PathwayVertexRep v2) {
		for (Pair<PathwayVertexRep, PathwayVertexRep> pair : selectedPortalLinks) {
			if ((pair.getFirst() == v1 && pair.getSecond() == v2) || (pair.getFirst() == v2 && pair.getSecond() == v1))
				return true;
		}
		return false;
	}

	protected boolean isPathLink(PathwayVertexRep v1, PathwayVertexRep v2) {
		PathwayVertexRep lastNodeOfPrevSegment = null;
		for (PathSegment segment : path) {
			if (!segment.isEmpty()) {
				if (lastNodeOfPrevSegment != null) {
					if ((v1 == lastNodeOfPrevSegment && v2 == segment.get(0))
							|| (v2 == lastNodeOfPrevSegment && v1 == segment.get(0))) {
						return true;
					}
				}

				lastNodeOfPrevSegment = segment.get(segment.size() - 1);
			}
		}
		return false;
	}

	protected Pair<Rect, Boolean> getPortalLocation(PathwayVertexRep vertexRep, PathwayMultiFormInfo info) {
		Rect rect = null;
		boolean isLocationWindow = false;
		IPathwayRepresentation pathwayRepresentation = info.getCurrentPathwayRepresentation();
		if (pathwayRepresentation != null)
			rect = getAbsoluteVertexLocation(pathwayRepresentation, vertexRep, info.container);

		if (rect == null || info.getCurrentEmbeddingID() == EEmbeddingID.PATHWAY_LEVEL4) {
			rect = new Rect(info.window.getAbsoluteLocation().x(), info.window.getAbsoluteLocation().y(), info.window
					.getSize().x(), 20);
			isLocationWindow = true;
		}
		return new Pair<Rect, Boolean>(rect, isLocationWindow);
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTablePerspective(TablePerspective newTablePerspective) {
		// dataMappingState.addTablePerspective(newTablePerspective);

	}

	@Override
	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
		for (TablePerspective tablePerspective : newTablePerspectives) {
			addTablePerspective(tablePerspective);
		}

	}

	@Override
	public List<TablePerspective> getTablePerspectives() {
		return new ArrayList<>(dataMappingState.getTablePerspectives());
	}

	@Override
	public Set<IDataDomain> getDataDomains() {
		Set<IDataDomain> dataDomains = new HashSet<>();
		for (TablePerspective tp : getTablePerspectives()) {
			dataDomains.add(tp.getDataDomain());
		}
		return dataDomains;
	}

	@Override
	public void removeTablePerspective(TablePerspective tablePerspective) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param showPortalsButton
	 *            setter, see {@link showPortalsButton}
	 */
	public void setShowPortalsButton(ShowPortalsAction showPortalsButton) {
		this.showPortalsButton = showPortalsButton;
	}

	@Override
	public void notifyOfSelectionChange(EventBasedSelectionManager selectionManager) {

		if (selectionManager == vertexSelectionManager) {
			Set<Integer> selectedVertexIDs = selectionManager.getElements(SelectionType.MOUSE_OVER);
			// currentPortalVertexRep = null;
			for (Integer id : selectedVertexIDs) {
				currentPortalVertexRep = allVertexReps.get(id);
			}
			// if (currentPortalVertexRep != null && currentPortalVertexRep.getType() == EPathwayVertexType.map) {
			//
			// PathwayGraph pathway = PathwayManager.get().getPathwayByTitle(currentPortalVertexRep.getName(),
			// EPathwayDatabaseType.KEGG);
			// PathwayMultiFormInfo info = getPathwayMultiFormInfo(pathway);
			// if (info != null) {
			// info.window.setTitleBarColor(new Color(1f, 0f, 0f));
			// }
			// }
			clearSelectedPortalLinks();
			// updatePortalLinks();
			isLayoutDirty = true;
		}
		// else if (selectionManager == pathwaySelectionManager) {
		// Set<Integer> selectedPathwayIDs = selectionManager.getElements(SelectionType.MOUSE_OVER);
		// for (PathwayMultiFormInfo info : pathwayInfos) {
		// info.window.setTitleBarColor(GLTitleBar.DEFAULT_COLOR);
		// }
		// for (Integer id : selectedPathwayIDs) {
		// PathwayMultiFormInfo info = getPathwayMultiFormInfo(id);
		// if (info != null) {
		// info.window.setTitleBarColor(new Color(SelectionType.MOUSE_OVER.getColor()));
		// }
		// }
		//
		// }

	}

	/**
	 * @return the selectedPortalLinks, see {@link #selectedPortalLinks}
	 */
	public List<Pair<PathwayVertexRep, PathwayVertexRep>> getSelectedPortalLinks() {
		return selectedPortalLinks;
	}

	public void clearSelectedPortalLinks() {
		selectedPortalLinks.clear();
	}

	public void addSelectedPortalLink(PathwayVertexRep vertexRep1, PathwayVertexRep vertexRep2) {
		selectedPortalLinks.add(new Pair<PathwayVertexRep, PathwayVertexRep>(vertexRep1, vertexRep2));
	}

	public void setCurrentPortalVertexRep(PathwayVertexRep currentPortalVertexRep) {
		this.currentPortalVertexRep = currentPortalVertexRep;
	}

	/**
	 * @return the pinnedWindows, see {@link #pinnedWindows}
	 */
	public Set<GLPathwayWindow> getPinnedWindows() {
		return pinnedWindows;
	}

	public void addPinnedWindow(GLPathwayWindow window) {
		pinnedWindows.add(window);
	}

	public void removePinnedWindow(GLPathwayWindow window) {
		pinnedWindows.remove(window);
	}

	/**
	 * @return the path, see {@link #path}
	 */
	public PathwayPath getPath() {
		return path;
	}

	public boolean hasPathwayCurrentContext(PathwayGraph pathway) {
		if (currentContextVertexRep == null)
			return false;
		Set<PathwayVertexRep> vertexReps = PathwayManager.get().getEquivalentVertexReps(currentContextVertexRep);
		return !vertexReps.isEmpty() || pathway.vertexSet().contains(currentContextVertexRep);
	}

	// public void addPathwayTextureSelectionListener(IPathwayTextureSelectionListener listener) {
	// textureSelectionListeners.add(listener);
	// }

	public boolean wasContextChanged() {
		return wasContextChanged;
	}

	/**
	 * @param wasContextChanged
	 *            setter, see {@link wasContextChanged}
	 */
	public void setWasContextChanged(boolean wasContextChanged) {
		this.wasContextChanged = wasContextChanged;
	}

	/**
	 * @return the isControlKeyPressed, see {@link #isControlKeyPressed}
	 */
	public boolean isControlKeyPressed() {
		return isControlKeyPressed;
	}

	/**
	 * @return the dataMappingState, see {@link #dataMappingState}
	 */
	public DataMappingState getDataMappingState() {
		return dataMappingState;
	}

	/**
	 * @param selectPathAction
	 *            setter, see {@link selectPathAction}
	 */
	public void setSelectPathAction(SelectPathAction selectPathAction) {
		this.selectPathAction = selectPathAction;
	}

	/**
	 * @return the selectPathAction, see {@link #selectPathAction}
	 */
	public SelectPathAction getSelectPathAction() {
		return selectPathAction;
	}

	public EEmbeddingID getCurrentlyDisplayedPathLevel() {
		return pathInfo.getCurrentEmbeddingID();
	}

	/**
	 * @return the isShowPortals, see {@link #isShowPortals}
	 */
	public boolean isShowPortals() {
		return isShowPortals;
	}

	/**
	 * @return the sampleMappingMode, see {@link #sampleMappingMode}
	 */
	public ESampleMappingMode getSampleMappingMode() {
		return sampleMappingMode;
	}

}
