package org.caleydo.view.genesearch;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.serialize.ASerializedView;

/**
 * Serialized gene search view.
 * 
 * @author Marc Streit
 */
@XmlRootElement
@XmlType
public class SerializedGeneSearchView extends ASerializedView {

	/**
	 * Default constructor with default initialization
	 */
	public SerializedGeneSearchView() {
	}

	@Override
	public String getViewType() {
		return RcpGeneSearchView.VIEW_TYPE;
	}
}
