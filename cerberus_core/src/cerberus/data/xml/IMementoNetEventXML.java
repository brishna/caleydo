/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.data.xml;

//import java.io.InputStream;
// import org.xml.sax.InputSource;

import cerberus.data.xml.IMementoXML;
import cerberus.data.xml.IMementoItemXML;
import cerberus.data.xml.IMementoCallbackXML;

/**
 * Interface for loading and saving DNetEventComponentInterface objects to and from XML file.
 * 
 * @author Michael Kalkusch
 * 
 * @see prometheus.net.dwt.DNetEventComponentInterface
 */
public interface IMementoNetEventXML 
extends IMementoXML, IMementoItemXML, IMementoCallbackXML {
	
	
	/**
	 * Creates a memento containing all infomation for this component.
	 * 
	 * @return String containing a XML definition for this component
	 */
	public String createMementoXMLperObject();
	
		
}
