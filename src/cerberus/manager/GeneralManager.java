package cerberus.manager;

import cerberus.manager.type.ManagerObjectType;
//import prometheus.net.dwt.swing.mdi.DDesktopPane;

public interface GeneralManager {

	/**
	 * Used to create a unique networkwide identification numbers.
	 * This defines the lowest numbers to address applications over the network.
	 * 
	 * Schema: [enumeration of components][type][unique network id]
	 * regular ranges: [ >0][01..99][00..99]
	 * 
	 * exampels: [12][03][09]
	 * 
	 * @see cerberus.manager.GeneralManager#iUniqueId_TypeOffset
	 * @see cerberus.manager.GeneralManager#iUniqueId_Increment
	 * @see cerberus.manager.GeneralManager#createNewId(cerberus.manager.type.ManagerObjectType)
	 */
	public final int iUniqueId_WorkspaceOffset = 100;
	
	/**
	 * Used to create a unique networkwide identification numbers.
	 * This defines the lowest numbers to address applications over the network.
	 * 
	 * Schema: [enumeration of components][type][unique network id]
	 * regular ranges: [ >0][01..99][00..99]
	 * 
	 * exampels: [12][03][09]
	 * 
	 * @see cerberus.manager.GeneralManager#iUniqueId_Increment
	 * @see cerberus.manager.GeneralManager#iUniqueId_WorkspaceOffset
	 * @see cerberus.manager.GeneralManager#createNewId(cerberus.manager.type.ManagerObjectType)
	 */
	public final int iUniqueId_TypeOffset = 100;
	
	/**
	 * Increment from one Id to the next unique id.
	 * 
	 * @see cerberus.manager.GeneralManager#iUniqueId_TypeOffset
	 * @see cerberus.manager.GeneralManager#iUniqueId_Workspace
	 * @see cerberus.manager.GeneralManager#createNewId(cerberus.manager.type.ManagerObjectType)
	 */
	public final int iUniqueId_Increment = 10000;
	
	public final int iUniqueId_Menu_Offset = 5600;
		
	public final int iUniqueId_Menu_Inc = 10000;
	
	public final int iUniqueId_Workspace = iUniqueId_Increment + 900;
	
	public final int iUniqueId_View = iUniqueId_Increment + 800;
	
	public final int iUniqueId_TypeOffset_Collection = 50;
	
	public final int iUniqueId_TypeOffset_Logger = 10;
	
	public final int iUniqueId_TypeOffset_Set = 51;
	
	public final int iUniqueId_TypeOffset_Selection = 52;
	
	public final int iUniqueId_TypeOffset_Storage = 53;
	
	public final int iUniqueId_TypeOffset_Command = 99;
	
	public final int iUniqueId_TypeOffset_Command_Queue = 98;
	
	public final int iUniqueId_TypeOffset_Pathways_Pathway = 60;
	
	public final int iUniqueId_TypeOffset_Pathways_Vertex = 61;
	
	public final int iUniqueId_TypeOffset_Pathways_Edge = 62;		
	
	public final int iUniqueId_TypeOffset_Memento = 95;

	public final int iUniqueId_TypeOffset_Window = 40;
	
	public final int iUniqueId_TypeOffset_GuiComponent = 30;

	public final int iUniqueId_TypeOffset_GuiAWT = 31;
	
	public final int iUniqueId_TypeOffset_GuiSWT = 32;
		
	public final int iUniqueId_TypeOffset_GuiMenu = 39;
	
	
	/**
	 * Tests, if a cairtain iItemId is handled by the manager.
	 * 
	 * @param iItemId to identify an item that is tested
	 * @return TRUE if iItemId exists
	 */
	public boolean hasItem(final int iItemId);

	/**
	 * Return the item bound to the iItemId or null if the id is not 
	 * bound to an item.
	 * 
	 * @param iItemId uniqu id used for lookup
	 * @return object bound to iItemId
	 */
	public Object getItem( final int iItemId);
	
	/**
	 * Get the number of current handled items.
	 * 
	 * @return number of items
	 */
	public int size();

	/**
	 * Type of the manager
	 * 
	 * @return type of the manager
	 */
	public ManagerObjectType getManagerType();
	/**
	 * Returns the reference to the prometheus.app.SingeltonManager.
	 * 
	 * Note: Do not forget to set the reference to the SingeltonManager inside the constructor.
	 * 
	 * @return reference to SingeltonManager
	 */
	public GeneralManager getGeneralManager();
	
	/**
	 * Get the Singleton obejct.
	 * 
	 * @return Singelton object
	 */
	public Singelton getSingelton();
	
	/**
	 * Registers one Id and links it to the reference.
	 * 
	 * @param registerItem Object to be registered
	 * @param iItemId unique Id
	 * @param type defines type, can also be null if type is not known
	 * 
	 * @return TRUE if item was unregisterd by this manager
	 */
	public boolean registerItem( final Object registerItem, final int iItemId , final ManagerObjectType type );
	
	
	/**
	 * Unregisters an item using it's Id.
	 * 
	 * @param iItemId unique Id
	 * @param type defines type, can also be null if type is not known
	 * 
	 * @return TRUE if item was unregisterd by this manager
	 */
	public boolean unregisterItem( final int iItemId, final ManagerObjectType type  );
	
	/**
	 * Create a new unique Id.
	 * 
	 * @return new unique Id
	 * @param setNewBaseType type of object the id shall be created for
	 * 
	 * @see cerberus.manager.GeneralManager#iUniqueId_TypeOffset
	 * @see cerberus.manager.GeneralManager#iUniqueId_WorkspaceOffset
	 */
	public int createNewId( final ManagerObjectType setNewBaseType );

	public GeneralManager getManagerByBaseType(ManagerObjectType managerType);
		
		
	
}