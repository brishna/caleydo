/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.data.importer.tcga.regular;

import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

import org.caleydo.core.util.color.Color;
import org.caleydo.data.importer.tcga.FirehoseProvider;
import org.caleydo.data.importer.tcga.TCGAFileInfo;
import org.caleydo.datadomain.genetic.TCGADefinitions;
import org.caleydo.view.tourguide.api.external.ScoreParseSpecification;
import org.caleydo.view.tourguide.api.score.ECombinedOperator;
import org.caleydo.vis.lineup.model.mapping.EStandardMappings;

/**
 * @author Samuel Gratzl
 *
 */
public class MutSigTask extends RecursiveTask<ScoreParseSpecification> {
	private static final long serialVersionUID = 193680768589791994L;
	private static final Logger log = Logger.getLogger(MutSigTask.class.getName());
	private final FirehoseProvider fileProvider;

	public MutSigTask(FirehoseProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	@Override
	protected ScoreParseSpecification compute() {
		log.info(fileProvider + " start");
		TCGAFileInfo mutsig = fileProvider.findMutSigReport();
		if (mutsig == null) {
			log.warning(fileProvider + " file not found");
			return null;
		}
		log.warning(fileProvider + " data found: " + mutsig);
		ScoreParseSpecification spec = new ScoreParseSpecification(mutsig.getFile().getAbsolutePath());


		spec.setDelimiter("\t");
		spec.setNumberOfHeaderLines(1);
		spec.setContainsColumnIDs(false);
		spec.setColumnOfRowIds(0);

		spec.setRowIDSpecification(TCGADefinitions.createGeneIDSpecificiation());

		// q-value
		spec.setRankingName("MutSig Q-Value");
		spec.addColum(14);
		spec.setNormalizeScores(false);
		spec.setOperator(ECombinedOperator.MEAN);
		spec.setColor(Color.GRAY);
		spec.setMappingMin(0);
		spec.setMappingMax(1);
		spec.setMapping(EStandardMappings.P_Q_VALUE);

		return spec;
	}

}
