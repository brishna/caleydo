/**
 * 
 */
package cerberus.data.collection.set.selection;

import cerberus.data.collection.SetType;
import cerberus.data.collection.set.SetPlanarSimple;
import cerberus.manager.IGeneralManager;


/**
 * Selection SET that gives access to
 * the selection storage arrays.
 * The access methods are thread safe.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class SetSelection 
extends SetPlanarSimple 
implements ISetSelection {

	/**
	 * Constructor.
	 * 
	 * @param iSetCollectionId
	 * @param setGeneralManager
	 */
	public SetSelection(int iSetCollectionId, 
			IGeneralManager refGeneralManager) {

		super(iSetCollectionId, 
				refGeneralManager, 
				SetType.SET_SELECTION);
	}
	
	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#setSelectionIdArray(int[])
	 */
	public void setSelectionIdArray(int[] iArSelectionId) {
		
		this.getWriteToken();
		this.getStorageByDimAndIndex(0, 0).setArrayInt(iArSelectionId);
		this.returnWriteToken();
	}
	
	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#setGroupArray(int[])
	 */
	public void setGroupArray(int[] iArSelectionGroup) {
		
		this.getWriteToken();
		this.getStorageByDimAndIndex(0, 1).setArrayInt(iArSelectionGroup);
		this.returnWriteToken();
	}

	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#setOptionalDataArray(int[])
	 */
	public void setOptionalDataArray(int[] iArSelectionOptionalData) {
		
		this.getWriteToken();
		this.getStorageByDimAndIndex(0, 2).setArrayInt(iArSelectionOptionalData);
		this.returnWriteToken();
	}

	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#setAllSelectionDataArrays(int[], int[], int[])
	 */
	public void setAllSelectionDataArrays(int[] iArSelectionId, 
			int[] iArSelectionGroup, 
			int[] iArSelectionOptionalData) {
		
		this.getWriteToken();
		this.getStorageByDimAndIndex(0, 0).setArrayInt(iArSelectionId);
		this.getStorageByDimAndIndex(0, 1).setArrayInt(iArSelectionGroup);
		this.getStorageByDimAndIndex(0, 2).setArrayInt(iArSelectionOptionalData);
		this.returnWriteToken();
	}
	
	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#getSelectionIdArray()
	 */
	public int[] getSelectionIdArray() {
		
		this.getReadToken();
		int[] tmp = this.getStorageByDimAndIndex(0, 0).getArrayInt();
		this.returnReadToken();
		
		return tmp;
	}
	
	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#getGroupArray()
	 */
	public int[] getGroupArray() {	

		this.getReadToken();
		int[] tmp = this.getStorageByDimAndIndex(0, 1).getArrayInt();
		this.returnReadToken();
		
		return tmp;
	}
	
	/*
	 * (non-Javadoc)
	 * @see cerberus.data.collection.selection.ISetSelection#getOptionalDataArray()
	 */
	public int[] getOptionalDataArray() {

		this.getReadToken();
		int[] tmp = this.getStorageByDimAndIndex(0, 2).getArrayInt();
		this.returnReadToken();
		
		return tmp;	
	}
}
