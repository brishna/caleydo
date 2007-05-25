package cerberus.xml.parser.handler;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;

/**
 * Put ContentHandler and EntityResolver into one interface.
 * 
 * @see cerberus.util.system.CerberusInputStream
 * @see cerberus.util.system.CerberusInputStream#parseOnce(InputSource, String, IXmlBaseHandler, ILoggerManager)
 * @see cerberus.xml.parser.handler.IXmlParserHandler
 * @see cerberus.manager.IXmlParserManager
 * 
 * @author Michael Kalkusch
 *
 */
public interface IXmlBaseHandler 
extends ContentHandler, EntityResolver{

}
