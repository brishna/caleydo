/**
 * 
 */
package cerberus.base.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cerberus.data.mapping.GenomeMappingDataType;
import cerberus.data.mapping.GenomeMappingType;
import cerberus.manager.data.IGenomeIdManager;
import cerberus.manager.data.genome.GenomeIdMapInt2Int;
import cerberus.manager.data.genome.IGenomeIdMap;



/**
 * @author Michael Kalkusch
 *
 * @see cerberus.base.map.MultiHashArrayIntegerMap
 * @see cerberus.manager.data.genome.IGenomeIdMap
 */
public class MultiHashArrayStringMap 
extends HashMap <String,ArrayList<String>> 
implements GenericMultiMap <String>  {

	static final long serialVersionUID = 80806677;
	
	final int iDefaultLengthInternalArrayList = 3;
	
	/**
	 * @param arg0
	 * @param arg1
	 */
	public MultiHashArrayStringMap(int arg0, float arg1) {

		super(arg0, arg1);
		
	}

	/**
	 * @param arg0
	 */
	public MultiHashArrayStringMap(int arg0) {

		super(arg0);
		
	}

	/**
	 * 
	 */
	public MultiHashArrayStringMap() {

		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public MultiHashArrayStringMap(Map <String,ArrayList<String>>  arg0) {

		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.collections.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	public Object remove(Object key, Object item) {

		ArrayList<String> bufferArrayList = super.get( key );
	        
	    if ( bufferArrayList == null )
	       return null;
	        
	    bufferArrayList.remove( item );
		return null;
	}
	

	 public void putAll(Map mapToPut) {

		super.putAll((MultiStringMap) mapToPut);
	}

	public Object put(String key, String value) {

		// NOTE:: put might be called during deserialization !!!!!!
		// so we must provide a hook to handle this case
		// This means that we cannot make MultiMaps of ArrayLists !!!

		ArrayList<String> bufferArrayList = super.get(key);

		if (bufferArrayList == null)
		{
			/**
			 * new value!
			 */
			bufferArrayList = new ArrayList<String>(
					iDefaultLengthInternalArrayList);
		}

		bufferArrayList.add(value);
		return super.put(key, bufferArrayList);
	}

	/**
	 * Checkes if value is already registerd to the key.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object putChecked(String key, String value) {

		// NOTE:: put might be called during deserialization !!!!!!
		// so we must provide a hook to handle this case
		// This means that we cannot make MultiMaps of ArrayLists !!!

		ArrayList<String> bufferArrayList = super.get(key);

		if (bufferArrayList == null)
		{
			/**
			 * new value!
			 */
			bufferArrayList = new ArrayList<String>(
					iDefaultLengthInternalArrayList);
			bufferArrayList.add(value);
			return (super.put(key, bufferArrayList));
		}

		if (!bufferArrayList.contains(value))
		{
			bufferArrayList.add(value);
			return super.put(key, bufferArrayList);
		}
		return null;
	}

	public boolean containsValue(Object value) {

		Set<Entry<String, ArrayList<String>>> pairs = super.entrySet();

		if (pairs == null)
			return false;

		Iterator<Entry<String, ArrayList<String>>> pairsIterator = pairs
				.iterator();

		while (pairsIterator.hasNext())
		{
			Map.Entry<String, ArrayList<String>> keyValuePair = pairsIterator
					.next();

			ArrayList<String> list = keyValuePair.getValue();

			if (list.contains(value))
				return true;
		}
		return false;
	}

	public void clear() {

		Set<Entry<String, ArrayList<String>>> pairs = super.entrySet();

		Iterator<Entry<String, ArrayList<String>>> pairsIterator = pairs
				.iterator();

		while (pairsIterator.hasNext())
		{
			Map.Entry<String, ArrayList<String>> keyValuePair = pairsIterator
					.next();

			ArrayList<String> list = keyValuePair.getValue();
			list.clear();
		}

		super.clear();
	}

	public Collection<ArrayList<String>> values() {

		assert false : "values() in not implemented yet!";

		return null;
	}
	 
	 
	 public static ArrayList<String> mergeRemoveCopies( 
			 ArrayList<String> first, 
			 final ArrayList<String> second) {
		 
		 first.ensureCapacity( first.size() + second.size() );		 		 
		 Iterator <String> iter = second.iterator();
		 
		 String sBuffer;
		 while ( iter.hasNext() ) {			 
			 if ( ! first.contains( sBuffer = iter.next() ) ) {
				 first.add( sBuffer );
			 }
		 }
		 
		 return first;
	 }
	 
	/**
	 * Method takes a map that contains identifier codes and creates a new
	 * resolved codes. Resolving means mapping from code to internal ID.
	 * 
	 * TODO: Maybe this method should moved to outside of this class.
	 */
	public MultiHashArrayIntegerMap getCodeResolvedMap(
			IGenomeIdManager refGenomeIdManager,
			GenomeMappingType genomeMappingLUT_1,
			GenomeMappingType genomeMappingLUT_2) {
		
		MultiHashArrayIntegerMap codeResolvedMultMapInt = 
			new MultiHashArrayIntegerMap();
		
		System.out.println(genomeMappingLUT_1.getTypeOrigin().toString());

		/** 
		 * Read HashMap and write it to new HashMap
		 */
		Set<Map.Entry<String,ArrayList<String>>> entrySet = this.entrySet();	
		Iterator <Entry<String,ArrayList<String>>> iterEntries = entrySet.iterator();
		int iResolvedID_1 = 0;
		int iResolvedID_2 = 0;
		Entry<String,ArrayList<String>> entryBuffer = null;
		Iterator<String> iterValues;
		
		while ( iterEntries.hasNext() ) 
		{
			entryBuffer = iterEntries.next();
								
			iResolvedID_1 = refGenomeIdManager.getIdIntFromStringByMapping(
					entryBuffer.getKey().toString(), 
					genomeMappingLUT_1);
			
			ArrayList<String> tmp = entryBuffer.getValue();
			iterValues = tmp.iterator();
			while (iterValues.hasNext())
			{
				iResolvedID_2 = refGenomeIdManager.getIdIntFromStringByMapping(
						iterValues.next().toString(),
						genomeMappingLUT_2);
			
				codeResolvedMultMapInt.putChecked(iResolvedID_1, iResolvedID_2);
			}
		}
			
		return codeResolvedMultMapInt;
	}	
}
