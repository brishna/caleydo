package org.caleydo.core.data.virtualarray;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
import org.caleydo.core.manager.datadomain.IDataDomain;

public class SetBasedSegmentData implements ISegmentData {

	private ASetBasedDataDomain dataDomain;
	private ContentVirtualArray contentVA;
	private Group group;
	private ISet set;
	private SetBasedDimensionGroupData dimensionGroupData;

	public SetBasedSegmentData(ASetBasedDataDomain dataDomain, ISet set,
			ContentVirtualArray contentVA, Group group,
			SetBasedDimensionGroupData dimensionGroupData) {
		this.set = set;
		this.dataDomain = dataDomain;
		this.contentVA = contentVA;
		this.group = group;
		this.dimensionGroupData = dimensionGroupData;
	}

	@Override
	public IDataDomain getDataDomain() {
		// TODO Auto-generated method stub
		return dataDomain;
	}

	@Override
	public ContentVirtualArray getContentVA() {
		// TODO Auto-generated method stub
		return contentVA;
	}

	@Override
	public Group getGroup() {
		// TODO Auto-generated method stub
		return group;
	}

	@Override
	public String getLabel() {
		return "Group " + group.getGroupID() + " in " + set.getLabel();
	}

	public ISet getSet() {
		return set;
	}

}