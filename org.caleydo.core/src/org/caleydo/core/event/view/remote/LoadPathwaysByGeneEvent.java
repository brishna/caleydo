package org.caleydo.core.event.view.remote;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.event.AEvent;

/**
 * Event to signal that pathways should be added to the bucket in which a certain gene is contained.
 * 
 * @author Marc Streit
 */
@XmlRootElement
@XmlType
public class LoadPathwaysByGeneEvent
	extends AEvent {

	/** gene ID of the idType */
	private int geneID = -1;

	private IDType idType;

	public int getGeneID() {
		return geneID;
	}

	public void setGeneID(int geneId) {
		geneID = geneId;
	}

	public IDType getIdType() {
		return idType;
	}

	public void setTableIDType(IDType idType) {
		this.idType = idType;
	}

	@Override
	public boolean checkIntegrity() {
		if (geneID == -1)
			throw new IllegalStateException("geneID was not set");
		if (idType == null)
			throw new NullPointerException("idType is null");
		return true;
	}

}
