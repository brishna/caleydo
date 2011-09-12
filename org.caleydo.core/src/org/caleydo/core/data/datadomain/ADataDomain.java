package org.caleydo.core.data.datadomain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.collection.table.LoadDataParameters;
import org.caleydo.core.data.container.ADimensionGroupData;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.AEventHandler;
import org.caleydo.core.manager.event.data.DimensionGroupsChangedEvent;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;

/**
 * Abstract class that implements data and view management.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
@XmlType
@XmlRootElement
public abstract class ADataDomain
	extends AEventHandler
	implements IDataDomain {

	protected String contentLabelSingular = "<not specified>";
	protected String recordLabelPlural = "<not specified>";

	protected EDataFilterLevel dataFilterLevel = EDataFilterLevel.ONLY_CONTEXT;

	protected String dataDomainType; 
	
	@XmlTransient
	protected List<ADimensionGroupData> dimensionGroups = new ArrayList<ADimensionGroupData>();
	
	/**
	 * This mode determines whether the user can load and work with gene expression data or otherwise if an
	 * not further specified data set is loaded. In the case of the unspecified data set some specialized gene
	 * expression features are not available.
	 */
	protected String dataDomainID = "unspecified";

	protected EIconTextures icon = EIconTextures.NO_ICON_AVAILABLE;

	/** parameters for loading the data-{@link set} */
	protected LoadDataParameters loadDataParameters;

	/** bootstrap filename this application was started with */
	protected String fileName;
	
	protected Color color;

	/**
	 * Every use case needs to state all ID Categories it can handle. The string must specify which primary
	 * VAType ({@link VAType#getPrimaryVAType()} is associated for the ID Category
	 */
	// protected HashMap<IDCategory, String> possibleIDCategories;

	/**
	 * DO NOT CALL THIS CONSTRUCTOR! ONLY USED FOR DESERIALIZATION.
	 */
	public ADataDomain() {
		initIDMappings();
	}

	public ADataDomain(String dataDomainType, String dataDomainID) {
		this.dataDomainType = dataDomainType;
		this.dataDomainID = dataDomainID;
		initIDMappings();
	}

	protected abstract void initIDMappings();

	@Override
	public String getDataDomainID() {
		return dataDomainID;
	}

	@Override
	public void setDataDomainID(String dataDomainType) {
		this.dataDomainID = dataDomainType;
	}
	
	@Override
	public String getDataDomainType() {
		return dataDomainType;
	}
	
	@Override
	public void setDataDomainType(String dataDomainType) {
		this.dataDomainType = dataDomainType;
	}

	@Override
	public EIconTextures getIcon() {
		return icon;
	}

	public String getContentLabelSingular() {
		return contentLabelSingular;
	}

	public void setContentLabelSingular(String contentLabelSingular) {
		this.contentLabelSingular = contentLabelSingular;
	}

	public String getContentLabelPlural() {
		return recordLabelPlural;
	}

	public void setContentLabelPlural(String contentLabelPlural) {
		this.recordLabelPlural = contentLabelPlural;
	}

	public EDataFilterLevel getDataFilterLevel() {
		return dataFilterLevel;
	}

	public void setDataFilterLevel(EDataFilterLevel dataFilterLevel) {
		this.dataFilterLevel = dataFilterLevel;
	}

	@Override
	public LoadDataParameters getLoadDataParameters() {
		return loadDataParameters;
	}

	@Override
	public void setLoadDataParameters(LoadDataParameters loadDataParameters) {
		this.loadDataParameters = loadDataParameters;
		
		fileName = loadDataParameters.getFileName();
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void setFileName(String bootSrapFileName) {
		this.fileName = bootSrapFileName;
	}

	@Override
	public String toString() {
		return dataDomainID;
	}
	

	@Override
	@XmlTransient
	public List<ADimensionGroupData> getDimensionGroups() {
		return dimensionGroups;
	}

	@Override
	public void setDimensionGroups(List<ADimensionGroupData> dimensionGroups) {
		this.dimensionGroups = dimensionGroups;
		DimensionGroupsChangedEvent event = new DimensionGroupsChangedEvent(this);
		event.setSender(this);
		GeneralManager.get().getEventPublisher().triggerEvent(event);
	}

	@Override
	public void addDimensionGroup(ADimensionGroupData dimensionGroup) {
		dimensionGroups.add(dimensionGroup);
		DimensionGroupsChangedEvent event = new DimensionGroupsChangedEvent(this);
		event.setSender(this);
		GeneralManager.get().getEventPublisher().triggerEvent(event);
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
}
