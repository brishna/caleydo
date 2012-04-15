/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 * 
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.data.importer.tcga;

import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.GroupingParseSpecification;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.ParsingRule;
import org.caleydo.data.importer.setupgenerator.DataSetDescriptionSerializer;

/**
 * Generator class that writes the loading information of a series of TCGA data
 * sets to an XML file.
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class SampleTCGADataXMLGenerator extends DataSetDescriptionSerializer {

	/**
	 * @param arguments
	 */
	public SampleTCGADataXMLGenerator(String[] arguments) {
		super(arguments);
	}

	public static final String DROPBOX_GBM_FOLDER = System.getProperty("user.home")
			+ System.getProperty("file.separator")
			+ "Dropbox/TCGA GDAC/Omics Integration/testdata/20110728/gbm/";

	public static final String MRNA = DROPBOX_GBM_FOLDER
			+ "mrna_cnmf/outputprefix.expclu.gct";
	public static final String MRNA_GROUPING = DROPBOX_GBM_FOLDER
			+ "mrna_cnmf/cnmf.membership.txt";

	public static final String MIRNA = DROPBOX_GBM_FOLDER
			+ "mir_cnmf/cnmf.normalized.gct";
	public static final String MIRNA_GROUPING = DROPBOX_GBM_FOLDER
			+ "mir_cnmf/cnmf.membership.txt";

	public static final String METHYLATION = DROPBOX_GBM_FOLDER
			+ "methylation_cnmf/cnmf.normalized.gct";
	public static final String METHYLATION_GROUPING = DROPBOX_GBM_FOLDER
			+ "methylation_cnmf/cnmf.membership.txt";

	public static final String COPY_NUMBER = DROPBOX_GBM_FOLDER
			+ "copy_number/all_thresholded_by_genes.txt";

	public static final String GROUND_TRUTH_GROUPING = DROPBOX_GBM_FOLDER
			+ "ground_truth/2011_exp_assignments.txt";

	public static final String CLINICAL = DROPBOX_GBM_FOLDER
			+ "clinical/clinical_patient_public_GBM.txt";

	public static final String MUTATION = DROPBOX_GBM_FOLDER
			+ "mutation/mut_patient_centric_table_public_transposed_01.txt";

	public static final String OUTPUT_FILE_PATH = System.getProperty("user.home")
			+ System.getProperty("file.separator") + "caleydo_data.xml";

	public static final String TCGA_ID_SUBSTRING_REGEX = "TCGA\\-|\\-...\\-";

	private IDSpecification sampleIDSpecification;

	/*
	 * public static final String DROPBOX_GBM_FOLDER =
	 * System.getProperty("user.home") + System.getProperty("file.separator") +
	 * "Dropbox/TCGA GDAC/Omics Integration/testdata/20111026/brca/"; public
	 * static final String MRNA = DROPBOX_GBM_FOLDER +
	 * "mrna_cnmf/outputprefix.expclu.gct"; public static final String
	 * MRNA_GROUPING = DROPBOX_GBM_FOLDER + "mrna_cnmf/cnmf.membership.txt";
	 * public static final String MIRNA = DROPBOX_GBM_FOLDER +
	 * "mir_cnmf/cnmf.normalized.gct"; public static final String MIRNA_GROUPING
	 * = DROPBOX_GBM_FOLDER + "mir_cnmf/cnmf.membership.txt"; public static
	 * final String METHYLATION = DROPBOX_GBM_FOLDER +
	 * "methylation_cnmf/cnmf.normalized.gct"; public static final String
	 * METHYLATION_GROUPING = DROPBOX_GBM_FOLDER +
	 * "methylation_cnmf/cnmf.membership.txt"; public static final String
	 * COPY_NUMBER = DROPBOX_GBM_FOLDER +
	 * "copy_number/all_thresholded_by_genes.txt"; public static final String
	 * OUTPUT_FILE_PATH = System.getProperty("user.home") +
	 * System.getProperty("file.separator") + "tcga_brca_data.xml";
	 */

	public static void main(String[] args) {

		SampleTCGADataXMLGenerator generator = new SampleTCGADataXMLGenerator(args);
		generator.run();
	}

	@Override
	protected void setUpDataSetDescriptions() {

		sampleIDSpecification = new IDSpecification();
		sampleIDSpecification.setIdType("SAMPLE");
		sampleIDSpecification.setReplacementExpression("\\.", "-");
		sampleIDSpecification.setSubStringExpression(TCGA_ID_SUBSTRING_REGEX);

		dataSetDescriptionCollection.add(setUpMRNAData());
		dataSetDescriptionCollection.add(setUpMiRNAData());
		dataSetDescriptionCollection.add(setUpMethylationData());
		dataSetDescriptionCollection.add(setUpCopyNumberData());
		dataSetDescriptionCollection.add(setUpClinicalData());
//		dataSetDescriptionCollection.add(setUpMutationData());
		
		
	}

	private DataSetDescription setUpMRNAData() {
		DataSetDescription mrnaData = new DataSetDescription();
		mrnaData.setDataSetName("mRNA");

		mrnaData.setDataSourcePath(MRNA);
		mrnaData.setNumberOfHeaderLines(3);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(2);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT",
				ColumnDescription.CONTINUOUS));
		mrnaData.addParsingRule(parsingRule);
		mrnaData.setTransposeMatrix(true);

		IDSpecification geneIDSpecification = new IDSpecification();
		geneIDSpecification.setIDTypeGene(true);
		geneIDSpecification.setIdType("GENE_SYMBOL");
		mrnaData.setRowIDSpecification(geneIDSpecification);
		mrnaData.setColumnIDSpecification(sampleIDSpecification);

		GroupingParseSpecification firehoseClustering = new GroupingParseSpecification(
				MRNA_GROUPING);
		firehoseClustering.setContainsColumnIDs(false);
		firehoseClustering.setRowIDSpecification(sampleIDSpecification);
		mrnaData.addColumnGroupingSpecification(firehoseClustering);

//		GroupingParseSpecification groundTruthGrouping = new GroupingParseSpecification();
//		groundTruthGrouping.setDataSourcePath(GROUND_TRUTH_GROUPING);
//		groundTruthGrouping.addColum(2);
//		groundTruthGrouping.setRowIDSpecification(sampleIDSpecification);
//		mrnaData.addColumnGroupingSpecification(groundTruthGrouping);

		return mrnaData;
	}

	private DataSetDescription setUpMiRNAData() {
		DataSetDescription mirnaData = new DataSetDescription();
		mirnaData.setDataSetName("miRNA");

		mirnaData.setDataSourcePath(MIRNA);
		mirnaData.setNumberOfHeaderLines(3);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(2);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT",
				ColumnDescription.CONTINUOUS));
		mirnaData.addParsingRule(parsingRule);

		IDSpecification mirnaIDSpecification = new IDSpecification();
		mirnaIDSpecification.setIdType("miRNA");
		mirnaData.setRowIDSpecification(mirnaIDSpecification);
		mirnaData.setTransposeMatrix(true);

		IDSpecification sampleIDSpecification = new IDSpecification();
		sampleIDSpecification.setIdType("SAMPLE");
		sampleIDSpecification.setReplacementExpression("\\.", "-");
		sampleIDSpecification.setSubStringExpression(TCGA_ID_SUBSTRING_REGEX);
		mirnaData.setColumnIDSpecification(sampleIDSpecification);

		GroupingParseSpecification firehoseClustering = new GroupingParseSpecification(
				MIRNA_GROUPING);
		firehoseClustering.setContainsColumnIDs(false);
		firehoseClustering.setRowIDSpecification(sampleIDSpecification);
		mirnaData.addColumnGroupingSpecification(firehoseClustering);

		return mirnaData;
	}

	private DataSetDescription setUpMethylationData() {
		DataSetDescription methylationData = new DataSetDescription();
		methylationData.setDataSetName("Methylation");

		methylationData.setDataSourcePath(METHYLATION);
		methylationData.setNumberOfHeaderLines(3);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(2);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT",
				ColumnDescription.CONTINUOUS));
		methylationData.addParsingRule(parsingRule);
		methylationData.setTransposeMatrix(true);

		IDSpecification sampleIDSpecification = new IDSpecification();
		sampleIDSpecification.setIdType("SAMPLE");
		sampleIDSpecification.setReplacementExpression("\\.", "-");
		sampleIDSpecification.setSubStringExpression(TCGA_ID_SUBSTRING_REGEX);

		IDSpecification methylationIDSpecification = new IDSpecification();
		methylationIDSpecification.setIdType("methylation");
		methylationData.setRowIDSpecification(methylationIDSpecification);
		methylationData.setColumnIDSpecification(sampleIDSpecification);

		GroupingParseSpecification firehoseClustering = new GroupingParseSpecification(
				METHYLATION_GROUPING);
		firehoseClustering.setContainsColumnIDs(false);
		firehoseClustering.setRowIDSpecification(sampleIDSpecification);
		methylationData.addColumnGroupingSpecification(firehoseClustering);

		return methylationData;
	}

	private DataSetDescription setUpCopyNumberData() {
		DataSetDescription copyNumberData = new DataSetDescription();
		copyNumberData.setDataSetName("Copy number");

		copyNumberData.setDataSourcePath(COPY_NUMBER);
		copyNumberData.setNumberOfHeaderLines(1);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(3);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT",
				ColumnDescription.ORDINAL));
		copyNumberData.addParsingRule(parsingRule);
		copyNumberData.setTransposeMatrix(true);

		IDSpecification geneIDSpecification = new IDSpecification();
		geneIDSpecification.setIDTypeGene(true);
		geneIDSpecification.setIdType("GENE_SYMBOL");
		copyNumberData.setRowIDSpecification(geneIDSpecification);
		copyNumberData.setColumnIDSpecification(sampleIDSpecification);

		return copyNumberData;

		// copyNumberData.setColorScheme(EDefaultColorSchemes.RED_YELLOW_BLUE_DIVERGING
		// .name());
	}

	//
	private DataSetDescription setUpClinicalData() {
		DataSetDescription clinicalData = new DataSetDescription();
		clinicalData.setDataSetName("Clinical");
		clinicalData.setDataHomogeneous(false);

		clinicalData.setDataSourcePath(CLINICAL);
		clinicalData.setNumberOfHeaderLines(1);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(10);
		parsingRule.setToColumn(11);
		parsingRule.setColumnDescripton(new ColumnDescription());
		clinicalData.addParsingRule(parsingRule);
		parsingRule = new ParsingRule();
		parsingRule.setFromColumn(13);
		parsingRule.setToColumn(15);
		parsingRule.setColumnDescripton(new ColumnDescription());
		clinicalData.addParsingRule(parsingRule);

		IDSpecification clinicalIdSpecification = new IDSpecification();
		clinicalIdSpecification.setIdType("clinical");

		clinicalData.setColumnIDSpecification(clinicalIdSpecification);
		clinicalData.setRowIDSpecification(sampleIDSpecification);

		// columnLabels.add("Days to birth");
		// columnLabels.add("Days to death");
		// columnLabels.add("Days to last followup");
		// columnLabels.add("Days to tumor progression");
		// columnLabels.add("Days to tumor recurrence");

		return clinicalData;
	}

	private DataSetDescription setUpMutationData() {
		DataSetDescription mutationDataMetaInfo = new DataSetDescription();
		mutationDataMetaInfo.setDataSetName("Mutation Status");
		mutationDataMetaInfo.setDataSourcePath(MUTATION);

		mutationDataMetaInfo.setNumberOfHeaderLines(1);

		ParsingRule parsingRule = new ParsingRule();
		parsingRule.setFromColumn(1);
		parsingRule.setParseUntilEnd(true);
		parsingRule.setColumnDescripton(new ColumnDescription("FLOAT",
				ColumnDescription.NOMINAL));
		mutationDataMetaInfo.addParsingRule(parsingRule);
		mutationDataMetaInfo.setTransposeMatrix(true);

		IDSpecification geneIDSpecification = new IDSpecification();
		geneIDSpecification.setIDTypeGene(true);
		geneIDSpecification.setIdType("GENE_SYMBOL");
		mutationDataMetaInfo.setRowIDSpecification(geneIDSpecification);
		mutationDataMetaInfo.setColumnIDSpecification(sampleIDSpecification);

		return mutationDataMetaInfo;
	}

}
