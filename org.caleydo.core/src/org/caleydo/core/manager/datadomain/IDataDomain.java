package org.caleydo.core.manager.datadomain;

import java.util.Set;

import org.caleydo.core.data.collection.set.LoadDataParameters;
import org.caleydo.core.data.virtualarray.IDimensionGroupData;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;

/**
 * Use cases are the unique points of coordinations for views and its data. Genetic data is one example -
 * another is a more generic one where Caleydo can load arbitrary tabular data but without any special
 * features of genetic analysis.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
public interface IDataDomain {

	/**
	 * Returns the qualified name of the concrete data domain
	 */
	public String getDataDomainType();

	/**
	 * Set the dataDomain Type.
	 * 
	 * @param dataDomainType
	 */
	public void setDataDomainType(String dataDomainType);

	/**
	 * Returns the icon representing the data contained in this domain
	 */
	public EIconTextures getIcon();

	/**
	 * Gets the parameters for loading the data-{@link Set} contained in this use case
	 * 
	 * @return parameters for loading the data-{@link Set} of this use case
	 */
	public LoadDataParameters getLoadDataParameters();

	/**
	 * Sets the parameters for loading the data-{@link Set} contained in this use case
	 * 
	 * @param loadDataParameters
	 *            parameters for loading the data-{@link Set} of this use case
	 */
	public void setLoadDataParameters(LoadDataParameters loadDataParameters);

	/** Sets the name of the boots-trap xml-file this useCase was or should be loaded */
	public String getFileName();

	/** Gets the name of the boots-trap xml-file this useCase was or should be loaded */
	public void setFileName(String bootstrapFileName);

	/**
	 * @return The dimension groups that have been created for this IDataDomain object (data set).
	 */
	public Set<IDimensionGroupData> getDimensionGroups();
	
	/**
	 * Sets the dimension groups for this IDataDomain object (data set).
	 * 
	 * @param dimensionGroups
	 */
	public void setDimensionGroups(Set<IDimensionGroupData> dimensionGroups);
	
	/**
	 * Adds a dimension group to this IDataDomain object (data set).
	 * 
	 * @param dimensionGroup
	 */
	public void addDimensionGroup(IDimensionGroupData dimensionGroup);

}
