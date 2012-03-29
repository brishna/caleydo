package org.caleydo.core.event.view.pathway;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.event.AEvent;

/**
 * Events that signals that gene mapping within pathway views should be disabled.
 * 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class DisableGeneMappingEvent
	extends AEvent {

	@Override
	public boolean checkIntegrity() {
		// nothing to check
		return true;
	}
}
