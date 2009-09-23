package org.caleydo.core.manager.general;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;

import org.caleydo.core.bridge.gui.IGUIBridge;
import org.caleydo.core.command.system.CmdFetchPathwayData;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.manager.ISWTGUIManager;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.IViewManager;
import org.caleydo.core.manager.IXmlParserManager;
import org.caleydo.core.manager.command.CommandManager;
import org.caleydo.core.manager.data.ISetManager;
import org.caleydo.core.manager.data.IStorageManager;
import org.caleydo.core.manager.data.set.SetManager;
import org.caleydo.core.manager.data.storage.StorageManager;
import org.caleydo.core.manager.event.EventPublisher;
import org.caleydo.core.manager.gui.SWTGUIManager;
import org.caleydo.core.manager.id.IDManager;
import org.caleydo.core.manager.mapping.IDMappingManager;
import org.caleydo.core.manager.parser.XmlParserManager;
import org.caleydo.core.manager.specialized.clinical.glyph.GlyphManager;
import org.caleydo.core.manager.specialized.genetic.EOrganism;
import org.caleydo.core.manager.specialized.genetic.IPathwayItemManager;
import org.caleydo.core.manager.specialized.genetic.IPathwayManager;
import org.caleydo.core.manager.specialized.genetic.pathway.EPathwayDatabaseType;
import org.caleydo.core.manager.specialized.genetic.pathway.PathwayItemManager;
import org.caleydo.core.manager.specialized.genetic.pathway.PathwayManager;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.manager.view.ViewManager;
import org.caleydo.core.net.IGroupwareManager;
import org.caleydo.core.serialize.SerializationManager;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.caleydo.core.util.tracking.TrackDataProvider;
import org.caleydo.core.util.wii.WiiRemote;
import org.caleydo.data.loader.ResourceLoader;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * General manager that contains all module managers.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class GeneralManager
	implements IGeneralManager {

	/**
	 * General manager as a singleton
	 */
	private static IGeneralManager generalManager;

	/**
	 * Preferences store enables storing and restoring of application specific preference data.
	 */
	private PreferenceStore preferenceStore;

	private IStorageManager storageManager;
	private ISetManager setManager;
	private ICommandManager commandManager;
	private ISWTGUIManager sWTGUIManager;
	private IViewManager viewGLCanvasManager;
	private IPathwayManager pathwayManager;
	private IPathwayItemManager pathwayItemManager;
	private IEventPublisher eventPublisher;
	private IXmlParserManager xmlParserManager;
	private IIDMappingManager genomeIdManager;
	private GlyphManager glyphManager;
	private IDManager IDManager;
	private ILog logger;
	private IGUIBridge guiBridge;
	private ResourceLoader resourceLoader;
	private WiiRemote wiiRemote;
	private TrackDataProvider trackDataProvider;
	private IGroupwareManager groupwareManager;
	private SerializationManager serializationManager;

	/**
	 * The use case determines which kind of data is loaded in the views.
	 */
	private EnumMap<EDataDomain, IUseCase> useCaseMap;

	private boolean bIsWiiMode = false;

	@Override
	public void init(IGUIBridge externalGUIBridge) {
		this.init();
		this.guiBridge = externalGUIBridge;
	}

	@Override
	public void init() {
		initLogger();

		storageManager = new StorageManager();
		setManager = new SetManager();
		// connectedElementRepManager = new SelectionManager();
		commandManager = new CommandManager();
		eventPublisher = new EventPublisher();
		viewGLCanvasManager = new ViewManager();
		sWTGUIManager = new SWTGUIManager();
		genomeIdManager = new IDMappingManager();
		pathwayManager = new PathwayManager();
		pathwayItemManager = new PathwayItemManager();
		xmlParserManager = new XmlParserManager();
		glyphManager = new GlyphManager();
		IDManager = new IDManager();
		xmlParserManager.initHandlers();

		groupwareManager = null;
		serializationManager = new SerializationManager();

		initPreferences();

		resourceLoader = new ResourceLoader();

		wiiRemote = new WiiRemote();
		if (GeneralManager.get().isWiiModeActive()) {
			wiiRemote.connect();
		}

		trackDataProvider = new TrackDataProvider();
		useCaseMap = new EnumMap<EDataDomain, IUseCase>(EDataDomain.class);
	}

	/**
	 * Returns the general method as a singleton object. When first called the general manager is created
	 * (lazy).
	 * 
	 * @return singleton GeneralManager instance
	 */
	public static IGeneralManager get() {
		if (generalManager == null) {
			generalManager = new GeneralManager();
		}
		return generalManager;
	}

	private void initPreferences() {
		preferenceStore = new PreferenceStore(IGeneralManager.CALEYDO_HOME_PATH + PREFERENCE_FILE_NAME);

		try {
			if (IGeneralManager.VERSION == null)
				throw new IllegalStateException("Cannot determine current version of Caleydo.");

			preferenceStore.load();
			String sStoredVersion = preferenceStore.getString(PreferenceConstants.VERSION);

			// If stored version is older then current version - remove old Caleydo folder
			// Test 1st and 2nd number of version string
			if (sStoredVersion.equals("")
				|| (new Integer(sStoredVersion.substring(0, 1)) <= new Integer(IGeneralManager.VERSION
					.substring(0, 1)) && new Integer(sStoredVersion.substring(2, 3)) < new Integer(
					IGeneralManager.VERSION.substring(2, 3)))) {

				MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
				messageBox.setText("Clean old data");
				messageBox.setMessage("You have downloaded a new major version of Caleydo ("
					+ IGeneralManager.VERSION
					+ "). \nYour old Caleydo settings and pathway data will be discarded and newly created.");
				messageBox.open();

				CmdFetchPathwayData.deleteDir(new File(IGeneralManager.CALEYDO_HOME_PATH));

				initCaleydoFolder();
			}

		}
		catch (IOException e) {
			initCaleydoFolder();
		}

		if (preferenceStore.getBoolean(PreferenceConstants.USE_PROXY)) {
			System.setProperty("network.proxy_host", preferenceStore
				.getString(PreferenceConstants.PROXY_SERVER));
			System.setProperty("network.proxy_port", preferenceStore
				.getString(PreferenceConstants.PROXY_PORT));
		}
	}

	private void initCaleydoFolder() {

		// Create .caleydo folder
		if (!new File(IGeneralManager.CALEYDO_HOME_PATH).exists()) {
			if (!new File(IGeneralManager.CALEYDO_HOME_PATH).mkdir())
				throw new IllegalStateException(
					"Unable to create home folder .caleydo. Check user permissions!");
		}

		// Create log folder in .caleydo
		if (!new File(IGeneralManager.CALEYDO_HOME_PATH + "logs").mkdirs())
			throw new IllegalStateException(
				"Unable to create log folder .caleydo/log. Check user permissions!");

		// logger.log(new Status(Status.INFO, GeneralManager.PLUGIN_ID, "Create new preference store at "
		// + IGeneralManager.CALEYDO_HOME_PATH + PREFERENCE_FILE_NAME));

	}

	/**
	 * Initialize the Java internal logger
	 */
	private void initLogger() {
		logger = Platform.getLog(Platform.getBundle("org.caleydo.rcp"));
	}

	@Override
	public final ILog getLogger() {
		return logger;
	}

	@Override
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Override
	public IStorageManager getStorageManager() {
		return storageManager;
	}

	@Override
	public ISetManager getSetManager() {
		return setManager;
	}

	@Override
	public IViewManager getViewGLCanvasManager() {
		return viewGLCanvasManager;
	}

	@Override
	public IPathwayManager getPathwayManager() {
		return pathwayManager;
	}

	@Override
	public IPathwayItemManager getPathwayItemManager() {
		return pathwayItemManager;
	}

	@Override
	public ISWTGUIManager getSWTGUIManager() {
		return sWTGUIManager;
	}

	@Override
	public IEventPublisher getEventPublisher() {
		return eventPublisher;
	}

	@Override
	public IXmlParserManager getXmlParserManager() {
		return this.xmlParserManager;
	}

	@Override
	public IIDMappingManager getIDMappingManager() {
		return this.genomeIdManager;
	}

	@Override
	public ICommandManager getCommandManager() {
		return commandManager;
	}

	@Override
	public GlyphManager getGlyphManager() {
		return glyphManager;
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	@Override
	public IDManager getIDManager() {
		return IDManager;
	}

	@Override
	public IGUIBridge getGUIBridge() {
		return guiBridge;
	}

	@Override
	public boolean isWiiModeActive() {
		return bIsWiiMode;
	}

	@Override
	public WiiRemote getWiiRemote() {
		return wiiRemote;
	}

	@Override
	public TrackDataProvider getTrackDataProvider() {
		return trackDataProvider;
	}

	@Override
	public void addUseCase(IUseCase useCase) {
		useCaseMap.put(useCase.getDataDomain(), useCase);
	}

	@Override
	public IUseCase getUseCase(EDataDomain useCaseType) {
		return useCaseMap.get(useCaseType);
	}

	@Override
	public IGroupwareManager getGroupwareManager() {
		return groupwareManager;
	}

	@Override
	public void setGroupwareManager(IGroupwareManager groupwareManager) {
		this.groupwareManager = groupwareManager;
	}

	@Override
	public SerializationManager getSerializationManager() {
		return serializationManager;
	}

	@Override
	public Collection<IUseCase> getAllUseCases() {
		return useCaseMap.values();
	}
}
