package org.caleydo.core.manager.usecase;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.collection.ESetType;
import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.set.LoadDataParameters;
import org.caleydo.core.data.collection.set.Set;
import org.caleydo.core.data.graph.tree.Tree;
import org.caleydo.core.data.mapping.EIDCategory;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.EVAType;
import org.caleydo.core.data.selection.IVirtualArray;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.VirtualArray;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.IVirtualArrayDelta;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.IListenerOwner;
import org.caleydo.core.manager.event.data.ReplaceVirtualArrayEvent;
import org.caleydo.core.manager.event.data.ReplaceVirtualArrayInUseCaseEvent;
import org.caleydo.core.manager.event.data.StartClusteringEvent;
import org.caleydo.core.manager.event.view.NewSetEvent;
import org.caleydo.core.manager.event.view.SelectionCommandEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.event.view.storagebased.VirtualArrayUpdateEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.specialized.clinical.ClinicalUseCase;
import org.caleydo.core.manager.specialized.genetic.GeneticUseCase;
import org.caleydo.core.util.clusterer.ClusterNode;
import org.caleydo.core.util.clusterer.ClusterState;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IVirtualArrayUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.SelectionCommandListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
import org.caleydo.core.view.opengl.canvas.listener.VirtualArrayUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract use case class that implements data and view management.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
@XmlType
@XmlRootElement
@XmlSeeAlso( { GeneticUseCase.class, ClinicalUseCase.class, UnspecifiedUseCase.class })
public abstract class AUseCase
	implements IVirtualArrayUpdateHandler, ISelectionUpdateHandler, ISelectionCommandHandler, IUseCase,
	IListenerOwner {

	protected String contentLabelSingular = "<not specified>";
	protected String contentLabelPlural = "<not specified>";

	protected EDataFilterLevel dataFilterLevel = EDataFilterLevel.ONLY_CONTEXT;

	/**
	 * This mode determines whether the user can load and work with gene expression data or otherwise if an
	 * not further specified data set is loaded. In the case of the unspecified data set some specialized gene
	 * expression features are not available.
	 */
	protected EDataDomain useCaseMode = EDataDomain.UNSPECIFIED;

	/** map selection type to unique id for virtual array */
	protected EnumMap<EVAType, Integer> mapVAIDs;

	/** The set which is currently loaded and used inside the views for this use case. */
	protected ISet set;

	/** parameters for loading the the data-{@link set} */
	protected LoadDataParameters loadDataParameters;

	/** bootstrap filename this application was started with */
	protected String bootsTrapFileName;

	/** central {@link IEventPublisher} to receive and send events */
	private IEventPublisher eventPublisher;

	protected SelectionUpdateListener selectionUpdateListener;
	protected SelectionCommandListener selectionCommandListener;
	private StartClusteringListener startClusteringListener;
	private ReplaceVirtualArrayInUseCaseListener replaceVirtualArrayInUseCaseListener;
	private VirtualArrayUpdateListener virtualArrayUpdateListener;

	/** Every use case needs to state all views that can visualize its data */
	protected ArrayList<String> possibleViews;

	/**
	 * Every use case needs to state all ID Categories it can handle. The string must specify which primary
	 * VAType ({@link EVAType#getPrimaryVAType()} is associated for the ID Category
	 */
	protected HashMap<EIDCategory, String> possibleIDCategories;

	protected EIDType contentIDType;
	protected EIDType storageIDType;

	protected SelectionManager contentSelectionManager;
	protected SelectionManager storageSelectionManager;

	public AUseCase() {
		eventPublisher = GeneralManager.get().getEventPublisher();
		registerEventListeners();
	}

	@Override
	public EDataDomain getDataDomain() {
		return useCaseMode;
	}

	@Override
	public String getVATypeForIDCategory(EIDCategory idCategory) {
		return possibleIDCategories.get(idCategory);
	}

	// public void setUseCaseMode(EDataDomain useCaseMode) {
	// this.useCaseMode = useCaseMode;
	// }

	@Override
	public ArrayList<String> getPossibleViews() {
		return possibleViews;
	}

	@XmlTransient
	@Override
	public ISet getSet() {
		return set;
	}

	@Override
	public void setSet(ISet set) {
		assert (set != null);

		if ((set.getSetType() == ESetType.GENE_EXPRESSION_DATA && useCaseMode == EDataDomain.GENETIC_DATA)
			|| (set.getSetType() == ESetType.CLINICAL_DATA && useCaseMode == EDataDomain.CLINICAL_DATA)
			|| (set.getSetType() == ESetType.UNSPECIFIED && useCaseMode == EDataDomain.UNSPECIFIED)
			|| (set.getSetType() == ESetType.GENE_EXPRESSION_DATA && useCaseMode == EDataDomain.PATHWAY_DATA)) {

			ISet oldSet = this.set;
			this.set = set;
			if (oldSet != null) {
				oldSet.destroy();
				oldSet = null;
			}

		}
		else {
			throw new IllegalStateException("The Set " + set + " specified is not suited for the use case "
				+ this);
		}

	}

	@Override
	public void updateSetInViews() {

		initVAs();
		initSelectionManagers();
		NewSetEvent newSetEvent = new NewSetEvent();
		newSetEvent.setSet((Set) set);
		GeneralManager.get().getEventPublisher().triggerEvent(newSetEvent);

		// GLRemoteRendering glRemoteRenderingView = null;
		//
		// // Update set in the views
		// for (IView view : alView) {
		// view.setSet(set);
		//			
		//
		// if (view instanceof GLRemoteRendering) {
		// glRemoteRenderingView = (GLRemoteRendering) view;
		// }
		// }

		// TODO check
		// oldSet.destroy();
		// oldSet = null;
		// When new data is set, the bucket will be cleared because the internal heatmap and parcoords cannot
		// be updated in the context mode.
		// if (glRemoteRenderingView != null)
		// glRemoteRenderingView.clearAll();
	}

	@Override
	public String getContentLabel(boolean bCapitalized, boolean bPlural) {

		String sContentLabel = "";

		if (bPlural)
			sContentLabel = contentLabelPlural;
		else
			sContentLabel = contentLabelSingular;

		if (bCapitalized) {

			// Make first char capitalized
			sContentLabel =
				sContentLabel.substring(0, 1).toUpperCase()
					+ sContentLabel.substring(1, sContentLabel.length());
		}

		return sContentLabel;
	}

	private void initVAs() {

		mapVAIDs = new EnumMap<EVAType, Integer>(EVAType.class);

		if (!mapVAIDs.isEmpty()) {

			for (EVAType SelectionType : EVAType.values()) {
				if (mapVAIDs.containsKey(SelectionType)) {
					set.removeVirtualArray(mapVAIDs.get(SelectionType));
				}
			}

			mapVAIDs.clear();
		}

		if (set == null) {
			mapVAIDs.clear();
			return;
		}

		// create VA with empty list
		int iVAID = set.createVA(EVAType.CONTENT_CONTEXT, new ArrayList<Integer>());
		mapVAIDs.put(EVAType.CONTENT_CONTEXT, iVAID);

		iVAID = set.createVA(EVAType.CONTENT_EMBEDDED_HM, new ArrayList<Integer>());
		mapVAIDs.put(EVAType.CONTENT_EMBEDDED_HM, iVAID);

		ArrayList<Integer> alTempList = new ArrayList<Integer>();

		alTempList = new ArrayList<Integer>();

		for (int iCount = 0; iCount < set.size(); iCount++) {
			alTempList.add(iCount);
		}

		iVAID = set.createVA(EVAType.STORAGE, alTempList);
		mapVAIDs.put(EVAType.STORAGE, iVAID);

		initFullVA();
	}

	protected void initFullVA() {
		// initialize virtual array that contains all (filtered) information
		ArrayList<Integer> alTempList = new ArrayList<Integer>(set.depth());

		for (int iCount = 0; iCount < set.depth(); iCount++) {

			alTempList.add(iCount);
		}

		// TODO: remove possible old virtual array
		int iVAID = set.createVA(EVAType.CONTENT, alTempList);
		mapVAIDs.put(EVAType.CONTENT, iVAID);
	}

	protected void initSelectionManagers() {
		contentSelectionManager = new SelectionManager.Builder(contentIDType).build();
		storageSelectionManager = new SelectionManager.Builder(storageIDType).build();
	}

	public IVirtualArray getVA(EVAType vaType) {
		IVirtualArray va = set.getVA(mapVAIDs.get(vaType));
		IVirtualArray vaCopy = va.clone();
		return vaCopy;
	}

	@Override
	public void startClustering(ClusterState clusterState) {

		// if (!(this instanceof GeneticUseCase))
		// return;

		clusterState.setContentVaId(mapVAIDs.get(clusterState.getContentVAType()));
		clusterState.setStorageVaId(mapVAIDs.get(EVAType.STORAGE));

		ArrayList<IVirtualArray> iAlNewVAs = set.cluster(clusterState);

		if (iAlNewVAs != null) {
			set.replaceVA(mapVAIDs.get(clusterState.getContentVAType()), iAlNewVAs.get(0));
			set.replaceVA(mapVAIDs.get(EVAType.STORAGE), iAlNewVAs.get(1));
		}

		// This should be done to avoid problems with group info in HHM
		set.setGeneClusterInfoFlag(false);
		set.setExperimentClusterInfoFlag(false);

		eventPublisher.triggerEvent(new ReplaceVirtualArrayEvent(EIDCategory.GENE, clusterState
			.getContentVAType()));
		eventPublisher.triggerEvent(new ReplaceVirtualArrayEvent(EIDCategory.EXPERIMENT, EVAType.STORAGE));

	}

	/**
	 * This is the method which is used to synchronize the views with the Virtual Array, which is initiated
	 * from this class. Therefore it should not be called any time!
	 */
	@Override
	public void replaceVirtualArray(EIDCategory idCategory, EVAType vaType) {
		throw new IllegalStateException("UseCases shouldn't react to this");

	}

	@Override
	public void replaceVirtualArray(EIDCategory idCategory, EVAType vaType, IVirtualArray virtualArray) {

		String idCategoryAsscoatedVAType = possibleIDCategories.get(idCategory);
		if (idCategoryAsscoatedVAType == null)
			return;

		if (!idCategoryAsscoatedVAType.equals(vaType.getPrimaryVAType()))
			vaType = EVAType.getVATypeForPrimaryVAType(idCategoryAsscoatedVAType);

		set.replaceVA(mapVAIDs.get(vaType), virtualArray.clone());

		Tree<ClusterNode> tree = null;
		if (vaType == EVAType.CONTENT)
			tree = set.getClusteredTreeGenes();
		else if (vaType == EVAType.STORAGE)
			tree = set.getClusteredTreeExps();

		if (tree != null) {
			GeneralManager.get().getGUIBridge().getDisplay().asyncExec(new Runnable() {
				public void run() {
					Shell shell = new Shell();
					MessageBox messageBox = new MessageBox(shell, SWT.CANCEL);
					messageBox.setText("Warning");
					messageBox
						.setMessage("Modifications break tree structure, therefore dendrogram will be closed!");
					messageBox.open();
				}
			});
			if (vaType == EVAType.CONTENT)
				set.setClusteredTreeGenes(null);
			else if (vaType == EVAType.STORAGE)
				set.setClusteredTreeExps(null);
		}

		virtualArray.setGroupList(null);

		eventPublisher.triggerEvent(new ReplaceVirtualArrayEvent(idCategory, vaType));
	}

	public void setVirtualArray(EVAType vaType, IVirtualArray virtualArray) {
		set.replaceVA(mapVAIDs.get(vaType), virtualArray);
		mapVAIDs.put(vaType, virtualArray.getID());
	}

	@Override
	public void handleVirtualArrayUpdate(IVirtualArrayDelta vaDelta, String info) {

	}

	public void registerEventListeners() {

		// groupMergingActionListener = new GroupMergingActionListener();
		// groupMergingActionListener.setHandler(this);
		// eventPublisher.addListener(MergeGroupsEvent.class, groupMergingActionListener);
		//
		// groupInterChangingActionListener = new GroupInterChangingActionListener();
		// groupInterChangingActionListener.setHandler(this);
		// eventPublisher.addListener(InterchangeGroupsEvent.class, groupInterChangingActionListener);

		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);

		selectionCommandListener = new SelectionCommandListener();
		selectionCommandListener.setHandler(this);
		eventPublisher.addListener(SelectionCommandEvent.class, selectionCommandListener);

		startClusteringListener = new StartClusteringListener();
		startClusteringListener.setHandler(this);
		eventPublisher.addListener(StartClusteringEvent.class, startClusteringListener);

		replaceVirtualArrayInUseCaseListener = new ReplaceVirtualArrayInUseCaseListener();
		replaceVirtualArrayInUseCaseListener.setHandler(this);
		eventPublisher.addListener(ReplaceVirtualArrayInUseCaseEvent.class,
			replaceVirtualArrayInUseCaseListener);

		virtualArrayUpdateListener = new VirtualArrayUpdateListener();
		virtualArrayUpdateListener.setHandler(this);
		eventPublisher.addListener(VirtualArrayUpdateEvent.class, virtualArrayUpdateListener);
	}

	// TODO this is never called!
	public void unregisterEventListeners() {

		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}

		if (selectionCommandListener != null) {
			eventPublisher.removeListener(selectionCommandListener);
			selectionCommandListener = null;
		}

		if (startClusteringListener != null) {
			eventPublisher.removeListener(startClusteringListener);
			startClusteringListener = null;
		}

		if (replaceVirtualArrayInUseCaseListener != null) {
			eventPublisher.removeListener(replaceVirtualArrayInUseCaseListener);
			replaceVirtualArrayInUseCaseListener = null;
		}

		if (virtualArrayUpdateListener != null) {
			eventPublisher.removeListener(virtualArrayUpdateListener);
			virtualArrayUpdateListener = null;
		}
	}

	@Override
	public synchronized void queueEvent(AEventListener<? extends IListenerOwner> listener, AEvent event) {

		// FIXME: concurrency issues?
		listener.handleEvent(event);

	}

	@Override
	public void resetContextVA() {
		int iUniqueID = mapVAIDs.get(EVAType.CONTENT_CONTEXT);
		set.replaceVA(iUniqueID, new VirtualArray(EVAType.CONTENT_CONTEXT, set.depth(),
			new ArrayList<Integer>()));

	}

	public String getContentLabelSingular() {
		return contentLabelSingular;
	}

	public void setContentLabelSingular(String contentLabelSingular) {
		this.contentLabelSingular = contentLabelSingular;
	}

	public String getContentLabelPlural() {
		return contentLabelPlural;
	}

	public void setContentLabelPlural(String contentLabelPlural) {
		this.contentLabelPlural = contentLabelPlural;
	}

	public EDataFilterLevel getDataFilterLevel() {
		return dataFilterLevel;
	}

	public void setDataFilterLevel(EDataFilterLevel dataFilterLevel) {
		this.dataFilterLevel = dataFilterLevel;
	}

	@Override
	public LoadDataParameters getLoadDataParameters() {
		return loadDataParameters;
	}

	@Override
	public void setLoadDataParameters(LoadDataParameters loadDataParameters) {
		this.loadDataParameters = loadDataParameters;
	}

	public String getBootstrapFileName() {
		return bootsTrapFileName;
	}

	public void setBootstrapFileName(String bootsTrapFileName) {
		this.bootsTrapFileName = bootsTrapFileName;
	}

	public SelectionManager getContentSelectionManager() {
		return contentSelectionManager.clone();
	}

	public SelectionManager getStorageSelectionManager() {
		return storageSelectionManager.clone();
	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta, boolean scrollToSelection, String info) {
		IIDMappingManager mappingManager = GeneralManager.get().getIDMappingManager();
		if (mappingManager.hasMapping(selectionDelta.getIDType(), contentSelectionManager.getIDType())) {
			contentSelectionManager.setDelta(selectionDelta);
		}
		else if (mappingManager.hasMapping(selectionDelta.getIDType(), storageSelectionManager.getIDType())) {
			storageSelectionManager.setDelta(selectionDelta);
		}
	}

	@Override
	public void handleSelectionCommand(EIDCategory category, SelectionCommand selectionCommand) {
		// TODO Auto-generated method stub

	}

}
