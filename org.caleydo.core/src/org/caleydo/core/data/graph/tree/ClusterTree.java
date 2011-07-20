package org.caleydo.core.data.graph.tree;

import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.data.NewMetaSetsEvent;
import org.caleydo.core.util.clusterer.ClusterNode;

public class ClusterTree
	extends Tree<ClusterNode> {

	public ClusterTree() {
		super();
	}

	public ClusterTree(IDType leaveIDType) {
		super(leaveIDType);
	}

	public void createMetaSets(DataTable set) {
		getRoot().createMetaSets(set);
		NewMetaSetsEvent event = new NewMetaSetsEvent();
		event.setDataDomainID(set.getDataDomain().getDataDomainID());
		GeneralManager.get().getEventPublisher().triggerEvent(event);
	}

}
