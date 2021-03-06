/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.datadomain.genetic;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.EDataFilterLevel;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDeltaItem;
import org.caleydo.core.event.data.SelectionCommandEvent;
import org.caleydo.core.event.data.SelectionUpdateEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDCreator;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDType;

/**
 * DataDomain for genetic data.
 *
 * @author Marc Streit
 * @author Alexander Lex
 */
@XmlType
@XmlRootElement
public class GeneticDataDomain extends ATableBasedDataDomain {

	public final static String DATA_DOMAIN_TYPE = "org.caleydo.datadomain.genetic";

	private static final String CLINICAL_DATADOMAIN_TYPE = "org.caleydo.datadomain.clinical";

	/**
	 * Constructor. Do not create a {@link GeneticDataDomain} yourself, use
	 * {@link DataDomainManager#createDataDomain(String)} instead.
	 */
	public GeneticDataDomain() {
		super(DATA_DOMAIN_TYPE, DATA_DOMAIN_TYPE + DataDomainManager.DATA_DOMAIN_INSTANCE_DELIMITER
				+ IDCreator.createPersistentID(GeneticDataDomain.class));
	}

	@Override
	public void setTable(Table set) {
		super.setTable(set);

	}

	/**
	 * Initializes a virtual array with all elements, according to the data filters, as defined in
	 * {@link EDataFilterLevel}.
	 */

	// TODO: Re-write this as a filter
	// protected void initFullVA() {
	//
	// String sLevel = GeneralManager.get().getPreferenceStore()
	// .getString(PreferenceConstants.DATA_FILTER_LEVEL);
	// if (sLevel.equals("complete")) {
	// dataFilterLevel = EDataFilterLevel.COMPLETE;
	// } else if (sLevel.equals("only_mapping")) {
	// dataFilterLevel = EDataFilterLevel.ONLY_MAPPING;
	// } else if (sLevel.equals("only_context")) {
	// // Only apply only_context when pathways are loaded
	// // TODO we need to wait for the pathways to be loaded here!
	// if (PathwayManager.get().size() > 100) {
	// dataFilterLevel = EDataFilterLevel.ONLY_CONTEXT;
	// } else {
	// dataFilterLevel = EDataFilterLevel.ONLY_MAPPING;
	// }
	// } else
	// dataFilterLevel = EDataFilterLevel.COMPLETE;
	//
	// // initialize virtual array that contains all (filtered) information
	// ArrayList<Integer> alTempList = new
	// ArrayList<Integer>(table.getMetaData()
	// .depth());
	//
	// for (int iCount = 0; iCount < table.getMetaData().depth(); iCount++) {
	// if (dataFilterLevel != EDataFilterLevel.COMPLETE) {
	//
	// Integer iDavidID = null;
	// // Here we get mapping data for all values
	// // FIXME: Due to new mapping system, a mapping involving
	// // expression index can return a Set of
	// // values, depending on the IDType that has been specified when
	// // loading expression data.
	// // Possibly a different handling of the Set is required.
	// java.util.Set<Integer> setDavidIDs = GeneralManager.get()
	// .getIDMappingManager()
	// .getIDAsSet(recordIDType, primaryRecordMappingType, iCount);
	//
	// if ((setDavidIDs != null && !setDavidIDs.isEmpty())) {
	// iDavidID = (Integer) setDavidIDs.toArray()[0];
	// }
	// // GeneticIDMappingHelper.get().getDavidIDFromDimensionIndex(iCount);
	//
	// if (iDavidID == null) {
	// // generalManager.getLogger().log(new Status(IStatus.WARNING,
	// // GeneralManager.PLUGIN_ID,
	// // "Cannot resolve gene to DAVID ID!"));
	// continue;
	// }
	//
	// if (dataFilterLevel == EDataFilterLevel.ONLY_CONTEXT) {
	// // Here all values are contained within pathways as well
	// PathwayVertexGraphItem tmpPathwayVertexGraphItem = PathwayItemManager
	// .get().getPathwayVertexGraphItemByDavidId(iDavidID);
	//
	// if (tmpPathwayVertexGraphItem == null) {
	// continue;
	// }
	// }
	// }
	//
	// alTempList.add(iCount);
	// }
	// VirtualArray recordVA = new RecordVirtualArray(Table.RECORD,
	// alTempList);
	// // removeDuplicates(recordVA);
	// // FIXME make this a filter?
	// table.setRecordVA(Table.RECORD, recordVA);
	// }

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();
	}

	@Override
	public void handleForeignSelectionUpdate(String dataDomainType, SelectionDelta delta) {
		// if (dataDomainType == CLINICAL_DATADOMAIN_TYPE)
		// System.out
		// .println("TODO Convert and re-send selection from clinical to genetic");

		if (dimensionIDType.equals(delta.getIDType())) {
			// for(ISeldelta)
			SelectionUpdateEvent resendEvent = new SelectionUpdateEvent();
			resendEvent.setEventSpace(this.dataDomainID);

			SelectionDelta convertedDelta = new SelectionDelta(delta.getIDType());
			for (SelectionDeltaItem item : delta) {
				SelectionDeltaItem convertedItem = new SelectionDeltaItem();
				convertedItem.setSelectionType(item.getSelectionType());
				Integer converteID = convertClinicalExperimentToGeneticExperiment(item.getID());
				if (converteID == null)
					continue;

				convertedItem.setID(converteID);
				convertedItem.setConnectionIDs(item.getConnectionIDs());
				convertedItem.setRemove(item.isRemove());
				convertedDelta.add(convertedItem);
			}
			resendEvent.setSelectionDelta(convertedDelta);

			eventPublisher.triggerEvent(resendEvent);
		} else
			return;
	}

	@Override
	public void handleForeignRecordVAUpdate(String dataDomainType, String vaType, PerspectiveInitializationData data) {

		// FIXME its not clear which dimension va should be updated here
		// if (dataDomainType.equals(CLINICAL_DATADOMAIN_TYPE)) {
		// VirtualArray newDimensionVirtualArray = new
		// DimensionVirtualArray();
		//
		// for (Integer clinicalContentIndex : virtualArray) {
		// Integer converteID =
		// convertClinicalExperimentToGeneticExperiment(clinicalContentIndex);
		// if (converteID != null)
		// newDimensionVirtualArray.append(converteID);
		//
		// }

		// replaceDimensionVA(tableID, dataDomainType, Table.DIMENSION,
		// newDimensionVirtualArray);
		// }

	}

	// FIXME its not clear which dimension va should be updated here
	private Integer convertClinicalExperimentToGeneticExperiment(Integer clinicalContentIndex) {
		return null;
	}

	//
	// // FIXME - this is a hack for one special dataset (asslaber)
	// Table clinicalSet = ((ATableBasedDataDomain) DataDomainManager.get()
	// .getDataDomainByType(CLINICAL_DATADOMAIN_TYPE)).getTable();
	// int dimensionID = clinicalSet.getDimensionData(Table.DIMENSION)
	// .getDimensionVA().get(1);
	//
	// NominalDimension clinicalDimension = (NominalDimension<String>)
	// clinicalSet
	// .get(dimensionID);
	// VirtualArray origianlGeneticDimensionVA =
	// table.getDimensionData(
	// Table.DIMENSION).getDimensionVA();
	//
	// String label = (String) clinicalDimension.getRaw(clinicalContentIndex);
	//
	// label = label.replace("\"", "");
	// // System.out.println(label);
	//
	// for (Integer dimensionIndex : origianlGeneticDimensionVA) {
	// if (label.equals(table.get(dimensionIndex).getLabel()))
	// return dimensionIndex;
	// }
	//
	// return null;
	// }

	@Override
	public void handleForeignSelectionCommand(String dataDomainType, IDCategory idCategory,
			SelectionCommand selectionCommand) {

		if (CLINICAL_DATADOMAIN_TYPE.equals(dataDomainType) && dimensionIDCategory.equals(idCategory)) {
			SelectionCommandEvent newCommandEvent = new SelectionCommandEvent();
			newCommandEvent.setSelectionCommand(selectionCommand);
			newCommandEvent.setIDCategory(idCategory);
			newCommandEvent.setEventSpace(dataDomainType);
			eventPublisher.triggerEvent(newCommandEvent);
		}
	}

	// @Override
	// public String getRecordLabel(IDType idType, Object id) {
	// return super.getRecordLabel(idType, id);
	// // String geneSymbol = null;
	// //
	// // Set<String> setGeneSymbols =
	// // getGeneIDMappingManager().getIDAsSet(idType,
	// // humanReadableRecordIDType, id);
	// //
	// // if ((setGeneSymbols != null && !setGeneSymbols.isEmpty())) {
	// // geneSymbol = (String) setGeneSymbols.toArray()[0];
	// // }
	// //
	// // if (geneSymbol != null)
	// // return geneSymbol;// + " | " + refSeq;
	// // // else if (refSeq != null)
	// // // return refSeq;
	// // else
	// // return "No mapping";
	//
	// }

	public IDMappingManager getGeneIDMappingManager() {
		if (isColumnDimension())
			return recordIDMappingManager;
		else
			return dimensionIDMappingManager;
	}

	public IDMappingManager getSampleIDMappingManager() {
		if (isColumnDimension())
			return dimensionIDMappingManager;
		else
			return recordIDMappingManager;
	}

	/**
	 * Returns the idType for the content in the data table, which is either the recordIDType or the dimensionIDType
	 * depending on the result of {@link #isColumnDimension()}
	 *
	 * @return
	 */
	public IDType getGeneIDType() {
		if (isColumnDimension())
			return getRecordIDType();
		else
			return getDimensionIDType();
	}

	public IDType getSampleIDType() {
		if (isColumnDimension())
			return getDimensionIDType();
		else
			return getRecordIDType();
	}

	public IDType getGeneGroupIDType() {
		if (isColumnDimension())
			return getRecordGroupIDType();
		else
			return getDimensionGroupIDType();
	}

	public IDType getSampleGroupIDType() {
		if (isColumnDimension())
			return getDimensionGroupIDType();
		else
			return getRecordGroupIDType();

	}

	/**
	 * Returns the value of the type specified in the dataRepresentation from the table based on the ID of the gene and
	 * the experiment. Resolves dimension/record association for you in doing so.
	 */
	public float getNormalizedGeneValue(Integer geneID, Integer experimentID) {
		Integer recordID;
		Integer dimensionID;
		if (isGeneRecord()) {
			recordID = geneID;
			dimensionID = experimentID;
		} else {
			recordID = experimentID;
			dimensionID = geneID;
		}
		return table.getNormalizedValue(dimensionID, recordID);
	}

	/**
	 * Returns the raw value from the table based on the ID of the gene and the experiment. Resolves dimension/record
	 * association for you in doing so.
	 */
	public float getRawGeneValue(Integer geneID, Integer experimentID) {
		Integer recordID;
		Integer dimensionID;
		if (isGeneRecord()) {
			recordID = geneID;
			dimensionID = experimentID;
		} else {
			recordID = experimentID;
			dimensionID = geneID;
		}
		return table.getRaw(dimensionID, recordID);
	}

	public boolean isGeneRecord() {
		return (recordIDCategory == IDCategory.getIDCategory("GENE"));
	}
}
