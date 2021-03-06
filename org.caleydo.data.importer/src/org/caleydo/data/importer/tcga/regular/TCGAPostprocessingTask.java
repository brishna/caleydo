/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.data.importer.tcga.regular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.data.importer.tcga.model.ClinicalMapping;
import org.caleydo.data.importer.tcga.model.TCGADataSet;

/**
 * utility task to create the initial table perspectives for the clinical data domain
 *
 */
public class TCGAPostprocessingTask extends RecursiveAction {
	private static final Logger log = Logger.getLogger(TCGAPostprocessingTask.class.getName());
	private static final long serialVersionUID = 7378867458430247164L;

	private final TCGADataSet dataSet;

	public TCGAPostprocessingTask(TCGADataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	protected void compute() {
		if (dataSet.getDataDomain() == null)
			return;

		switch (dataSet.getType()) {
		case clinical:
			updateClinicalNames(dataSet.getDataDomain());
			break;
		default:
			// nothing to do up to now
		}
	}

	/**
	 * create initial table perspectives per column
	 *
	 * @param dataDomain
	 */
	private void updateClinicalNames(ATableBasedDataDomain dataDomain) {
		log.info(dataSet.getType() + " updating clinicial tableperspective names");
		Collection<String> done = new ArrayList<>();
		for (TablePerspective p : dataDomain.getAllTablePerspectives()) {
			if (p.getDimensionPerspective().isDefault())
				continue;
			String name = p.getLabel();
			ClinicalMapping mapping = ClinicalMapping.byName(name);
			if (mapping != null) {
				done.add("updating " + p.getLabel() + "->" + mapping.getLabel());
				p.setLabel(mapping.getLabel());
				p.getDimensionPerspective().setLabel(mapping.getLabel());
				if (!p.getRecordPerspective().isDefault())
					p.getRecordPerspective().setLabel(mapping.getLabel());
			}
		}
		log.info(dataSet.getType() + " updated: " + StringUtils.join(done, ", "));

	}


}
