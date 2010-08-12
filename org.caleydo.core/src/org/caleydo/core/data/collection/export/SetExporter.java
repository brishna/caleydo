package org.caleydo.core.data.collection.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.graph.tree.Tree;
import org.caleydo.core.data.graph.tree.TreePorter;
import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.data.selection.ContentVAType;
import org.caleydo.core.data.selection.ContentVirtualArray;
import org.caleydo.core.data.selection.StorageVAType;
import org.caleydo.core.data.selection.StorageVirtualArray;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.manager.ISetBasedDataDomain;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.util.clusterer.ClusterNode;

/**
 * Exports data so a CSV file.
 * 
 * @author Alexander Lex
 */
public class SetExporter {

	public enum EWhichViewToExport {
		BUCKET,
		WHOLE_DATA
	}

	public void exportGroups(ISet set, String sFileName, ArrayList<Integer> alGenes,
		ArrayList<Integer> alExperiments, IDType targetIDType) {

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(sFileName)));
			// Writing storage labels
			out.print("Identifier \t");
			for (Integer iStorageIndex : alExperiments) {
				out.print(set.get(iStorageIndex).getLabel());
				out.print("\t");
			}

			out.println();

			// IUseCase useCase = GeneralManager.get().getUseCase(set.getSetType().getDataDomain());
			//
			String identifier;
			IIDMappingManager iDMappingManager = GeneralManager.get().getIDMappingManager();
			for (Integer iContentIndex : alGenes) {
				if (set.getDataDomain().getDataDomainType().equals("org.caleydo.datadomain.genetic")) {
					Set<String> setRefSeqIDs =
						iDMappingManager.getIDAsSet(set.getDataDomain().getContentIDType(), targetIDType,
							iContentIndex);

					if ((setRefSeqIDs != null && !setRefSeqIDs.isEmpty())) {
						identifier = (String) setRefSeqIDs.toArray()[0];
					}
					else {
						continue;
					}
				}
				else {
					identifier =
						iDMappingManager.getID(set.getDataDomain().getContentIDType(), targetIDType,
							iContentIndex);
				}
				out.print(identifier + "\t");
				for (Integer iStorageIndex : alExperiments) {
					IStorage storage = set.get(iStorageIndex);
					out.print(storage.getFloat(EDataRepresentation.RAW, iContentIndex));
					out.print("\t");
				}
				out.println();
			}

			out.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public void export(ISet set, String sFileName, EWhichViewToExport eWhichViewToExport, IDType targetIDType) {
		ContentVirtualArray contentVA = null;
		StorageVirtualArray storageVA = null;

		ISetBasedDataDomain useCase = set.getDataDomain();

		if (eWhichViewToExport == EWhichViewToExport.BUCKET) {

			contentVA = useCase.getContentVA(ContentVAType.CONTENT_CONTEXT);
			storageVA = useCase.getStorageVA(StorageVAType.STORAGE);
		}
		else if (eWhichViewToExport == EWhichViewToExport.WHOLE_DATA) {
			contentVA = useCase.getContentVA(ContentVAType.CONTENT);
			storageVA = useCase.getStorageVA(StorageVAType.STORAGE);
		}

		if (contentVA == null || storageVA == null)
			throw new IllegalStateException("Not sure which VA to take.");

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(sFileName)));

			// Writing storage labels
			out.print("Identifier \t");
			for (Integer iStorageIndex : storageVA) {
				out.print(set.get(iStorageIndex).getLabel());
				out.print("\t");
			}

			if (contentVA.getGroupList() != null)
				out.print("Cluster_Number\tCluster_Repr\t");

			out.println();

			int cnt = -1;
			int cluster = 0;
			int iExample = 0;
			int index = 0;
			int offset = 0;
			String identifier;
			IIDMappingManager iDMappingManager = GeneralManager.get().getIDMappingManager();
			for (Integer iContentIndex : contentVA) {
				if (useCase.getDataDomainType().equals("org.caleydo.datadomain.genetic")) {

					// FIXME: Due to new mapping system, a mapping involving expression index can return a Set
					// of
					// values, depending on the IDType that has been specified when loading expression data.
					// Possibly a different handling of the Set is required.
					Set<String> setRefSeqIDs =
						iDMappingManager.getIDAsSet(set.getDataDomain().getContentIDType(), targetIDType,
							iContentIndex);

					if ((setRefSeqIDs != null && !setRefSeqIDs.isEmpty())) {
						identifier = (String) setRefSeqIDs.toArray()[0];
					}
					else {
						continue;
					}
					// Integer iRefseqMrnaInt =
					// iDMappingManager.getID(EIDType.EXPRESSION_INDEX, EIDType.REFSEQ_MRNA_INT,
					// iContentIndex);
					// if (iRefseqMrnaInt == null) {
					// continue;
					// }
					//
					// identifier =
					// iDMappingManager.getID(EIDType.REFSEQ_MRNA_INT, EIDType.REFSEQ_MRNA, iRefseqMrnaInt);
				}
				else {
					identifier =
						iDMappingManager.getID(set.getDataDomain().getContentIDType(), targetIDType,
							iContentIndex);
				}
				out.print(identifier + "\t");
				for (Integer iStorageIndex : storageVA) {
					IStorage storage = set.get(iStorageIndex);
					out.print(storage.getFloat(EDataRepresentation.RAW, iContentIndex));
					out.print("\t");
				}

				// export partitional cluster info for genes/entities
				if (contentVA.getGroupList() != null) {
					if (cnt == contentVA.getGroupList().get(cluster).getNrElements() - 1) {
						offset = offset + contentVA.getGroupList().get(cluster).getNrElements();
						cluster++;
						cnt = 0;
					}
					else {
						cnt++;
					}

					iExample = contentVA.getGroupList().get(cluster).getIdxExample();

					out.print(cluster + "\t" + iExample + "\t");

					index++;
				}
				out.println();
			}

			// export partitional cluster info for experiments
			if (storageVA.getGroupList() != null) {

				String stClusterNr = "Cluster_Number\t";
				String stClusterRep = "Cluster_Repr\t";

				cluster = 0;
				cnt = -1;

				for (Integer iStorageIndex : storageVA) {
					if (cnt == storageVA.getGroupList().get(cluster).getNrElements() - 1) {
						offset = offset + storageVA.getGroupList().get(cluster).getNrElements();
						cluster++;
						cnt = 0;
					}
					else {
						cnt++;
					}

					iExample = storageVA.getGroupList().get(cluster).getIdxExample();

					stClusterNr += cluster + "\t";
					stClusterRep += iExample + "\t";
				}

				stClusterNr += "\n";
				stClusterRep += "\n";

				out.print(stClusterNr);
				out.print(stClusterRep);

			}

			out.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	// FIXME: implement different content data / storage data instances
	public void exportTrees(ISet set, String directory) {
		try {
			// export gene cluster tree to own xml file
			Tree<ClusterNode> tree = set.getContentData(ContentVAType.CONTENT).getContentTree();
			if (tree != null) {
				TreePorter treePorter = new TreePorter();
				treePorter.setDataDomain(set.getDataDomain());
				treePorter.exportTree(directory + "/horizontal_gene.xml", tree);
			}
			// export experiment cluster tree to own xml file
			tree = set.getStorageData(StorageVAType.STORAGE).getStorageTree();
			if (tree != null) {
				TreePorter treePorter = new TreePorter();
				treePorter.setDataDomain(set.getDataDomain());
				treePorter.exportTree(directory + "/vertical_experiments.xml", tree);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
