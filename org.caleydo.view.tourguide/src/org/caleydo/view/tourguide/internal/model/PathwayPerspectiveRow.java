/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDType;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.data.PathwayTablePerspective;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;

/**
 * @author Samuel Gratzl
 *
 */
public final class PathwayPerspectiveRow extends AScoreRow {
	private final PathwayGraph pathway;
	private final Group group;

	public PathwayPerspectiveRow(PathwayGraph pathway) {
		this.pathway = pathway;
		List<Integer> idsInPathway = new ArrayList<Integer>();
		for (PathwayVertexRep vertexRep : pathway.vertexSet()) {
			idsInPathway.add(vertexRep.getID());
		}
		this.group = new Group(pathway.vertexSet().size(), 0);
		this.group.setLabel(pathway.getName(), false);
		this.group.setStartIndex(0);
		this.group.setGroupIndex(0);
	}

	@Override
	public PathwayPerspectiveRow clone() {
		return (PathwayPerspectiveRow) super.clone();
	}

	public EPathwayDatabaseType getType() {
		return pathway.getType();
	}

	/**
	 * @return the pathway, see {@link #pathway}
	 */
	public PathwayGraph getPathway() {
		return pathway;
	}

	@Override
	public String getLabel() {
		return pathway.getTitle();
	}

	@Override
	public String getPersistentID() {
		return pathway.getName();
	}

	@Override
	public IDataDomain getDataDomain() {
		return DataDomainManager.get().getDataDomainByType(PathwayDataDomain.DATA_DOMAIN_TYPE);
	}

	@Override
	public boolean is(TablePerspective tablePerspective) {
		return tablePerspective instanceof PathwayTablePerspective
				&& ((PathwayTablePerspective) tablePerspective).getPathway().equals(pathway);
	}

	@Override
	public IDType getIdType() {
		return PathwayVertexRep.getIdType();
	}

	@Override
	public IDType getDimensionIdType() {
		return null;
	}

	@Override
	public Iterable<Integer> getDimensionIDs() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Group> getGroups() {
		return Collections.singleton(group);
	}

	@Override
	public int getGroupSize() {
		return 1;
	}

	@Override
	public Collection<GroupInfo> getGroupInfos() {
		return Collections.singleton(new GroupInfo(group.getLabel(), group.getSize(), null));
	}

	@Override
	public Collection<Integer> of(Group group) {
		Set<Integer> idsInPathway = new TreeSet<Integer>();
		for (PathwayVertexRep vertexRep : pathway.vertexSet()) {
			idsInPathway.add(vertexRep.getID());
		}
		return idsInPathway;
	}

	@Override
	public int size() {
		return group.getSize();
	}

	@Override
	public Iterator<Integer> iterator() {
		return of(null).iterator();
	}
}


