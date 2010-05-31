package org.caleydo.core.command.data;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.command.base.ACmdCreational;
import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.set.Set;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.ISetBasedDataDomain;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.parser.parameter.IParameterHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Command, creates a new storage.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * @author Alexander Lex
 */
public class CmdDataCreateSet
	extends ACmdCreational<ISet> {

	private ISetBasedDataDomain dataDomain;
	private ArrayList<Integer> iAlStorageIDs;

	/**
	 * Constructor.
	 */
	public CmdDataCreateSet(final ECommandType cmdType) {
		super(cmdType);

		iAlStorageIDs = new ArrayList<Integer>();
	}

	private void fillSets(ISet newSet) {
		if (iAlStorageIDs.isEmpty())
			throw new IllegalStateException("No data available for creating storage.");

		for (int iStorageID : iAlStorageIDs) {
			newSet.addStorage(iStorageID);
		}
	}

	/**
	 * Load data from file using a token pattern.
	 */
	public void doCommand() {

		createdObject = new Set();
		createdObject.setLabel(sLabel);

		if (iExternalID != -1) {
			generalManager.getIDManager().mapInternalToExternalID(createdObject.getID(), iExternalID);
		}

		fillSets(createdObject);

		generalManager.getLogger().log(
			new Status(IStatus.INFO, IGeneralManager.PLUGIN_ID, "New Set with internal ID "
				+ createdObject.getID() + " and external ID " + iExternalID + " created."));

		dataDomain.setSet(createdObject);

		commandManager.runDoCommand(this);
	}

	@Override
	public void undoCommand() {
		commandManager.runUndoCommand(this);
	}

	@Override
	public void setParameterHandler(final IParameterHandler parameterHandler) {
		super.setParameterHandler(parameterHandler);

		StringTokenizer strToken_StorageBlock =
			new StringTokenizer(parameterHandler.getValueString(ECommandType.TAG_ATTRIBUTE2.getXmlKey()),
				IGeneralManager.sDelimiter_Paser_DataItemBlock);

		while (strToken_StorageBlock.hasMoreTokens()) {
			StringTokenizer strToken_StorageId =
				new StringTokenizer(strToken_StorageBlock.nextToken(),
					IGeneralManager.sDelimiter_Parser_DataItems);

			while (strToken_StorageId.hasMoreTokens()) {
				iAlStorageIDs.add(Integer.valueOf(strToken_StorageId.nextToken()).intValue());
			}
		}

		// Convert external IDs from XML file to internal IDs
		iAlStorageIDs = GeneralManager.get().getIDManager().convertExternalToInternalIDs(iAlStorageIDs);

		String sAttrib3 = parameterHandler.getValueString(ECommandType.TAG_ATTRIBUTE3.getXmlKey());
		dataDomain = (ISetBasedDataDomain) DataDomainManager.getInstance().getDataDomain(sAttrib3);
		if (dataDomain == null) {
			DataDomainManager.getInstance().createDataDomain(sAttrib3);
			GeneralManager.get().getLogger().log(
				new Status(IStatus.INFO, IGeneralManager.PLUGIN_ID, "Lazy creation of data domain "
					+ sAttrib3));
		}
	}

	public void setAttributes(ArrayList<Integer> iAlStorageIDs, ISetBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
		this.iAlStorageIDs = iAlStorageIDs;
	}
}
