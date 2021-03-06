/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
/**
 *
 */
package org.caleydo.core.io.gui.dataimport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.logging.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

/**
 * Parses the preview of a file and stores it in a data matrix.
 *
 * @author Christian Partl
 *
 */
public class FilePreviewParser {

	/**
	 * Matrix that stores the data for rows and all columns of the data file.
	 */
	private List<List<String>> dataMatrix = new ArrayList<>();

	/**
	 * The total number of columns detected in the file.
	 */
	private int totalNumberOfColumns;

	/**
	 * The total number of rows detected in the file.
	 */
	private int totalNumberOfRows;

	/**
	 *
	 */
	public FilePreviewParser() {
	}

	public void parseWithProgress(Shell shell, final String fileName, final String delimiter,
			final boolean parseAllRows,
			final int maxRowsToParse) {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

		try {
			dialog.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					parse(fileName, delimiter, parseAllRows, maxRowsToParse, monitor);
				}
			});
		} catch (Exception e) {
			Logger.log(new Status(IStatus.ERROR, this.toString(), "Dataset loading failed: " + e.getMessage()));
		}
	}

	/**
	 * Parses the specified file and stores the data in {@link #dataMatrix}. Additionally, the
	 * {@link #totalNumberOfRows} and {@link #totalNumberOfColumns} are set accordingly.
	 *
	 * @param fileName
	 *            Filename that specifies the file to be parsed.
	 * @param delimiter
	 *            The delimiter that is used to separate columns
	 * @param parseAllRows
	 *            Determines whether all rows of the file should be parsed.
	 * @param maxRowsToParse
	 *            The number of rows that should be parsed if parseAllRows is false.
	 */
	public void parse(String fileName, String delimiter, boolean parseAllRows, int maxRowsToParse,
			IProgressMonitor monitor) {

		reset();

		// Read preview
		try {
			BufferedReader file = GeneralManager.get().getResourceLoader().getResource(fileName);

			String line = "";

			monitor.beginTask("Loading file " + fileName, IProgressMonitor.UNKNOWN);

			while ((line = file.readLine()) != null) {

				if (parseAllRows || (maxRowsToParse > totalNumberOfRows)) {

					String[] row = line.split(delimiter);
					int currentNumberOfColumns = row.length;
					List<String> currentDataRow = new ArrayList<String>(currentNumberOfColumns);

					for (int i = 0; i < currentNumberOfColumns; i++) {
						currentDataRow.add(row[i]);
					}

					if (currentNumberOfColumns > totalNumberOfColumns) {
						for (List<String> previousDataRow : dataMatrix) {
							int previousRowLength = previousDataRow.size();
							for (int i = 0; i < currentNumberOfColumns - previousRowLength; i++) {
								previousDataRow.add("");
							}
						}
						totalNumberOfColumns = currentNumberOfColumns;
					}

					if (currentNumberOfColumns < totalNumberOfColumns) {
						for (int i = 0; i < totalNumberOfColumns - currentNumberOfColumns; i++) {
							currentDataRow.add("");
						}
					}
					dataMatrix.add(currentDataRow);
				}
				totalNumberOfRows++;
				monitor.worked(1);
			}
			file.close();
			Logger.log(new Status(IStatus.INFO, "FilePreviewParser", "Preview data loaded from '" + fileName + "'!"));

		} catch (FileNotFoundException e) {
			Logger.log(new Status(IStatus.ERROR, "FilePreviewParser", "File '" + fileName + "' not found!"));
			reset();
		} catch (IOException ioe) {
			Logger.log(new Status(IStatus.ERROR, "FilePreviewParser", "Input/output problem while reading file '"
					+ fileName + "'!"));
			reset();
		}
	}

	private void reset() {
		totalNumberOfColumns = 0;
		totalNumberOfRows = 0;
		for (List<String> row : dataMatrix) {
			row.clear();
		}
		dataMatrix.clear();
	}

	/**
	 * @return the dataMatrix, see {@link #dataMatrix}
	 */
	public List<List<String>> getDataMatrix() {
		return dataMatrix;
	}

	/**
	 * @return the totalNumberOfColumns, see {@link #totalNumberOfColumns}
	 */
	public int getTotalNumberOfColumns() {
		return totalNumberOfColumns;
	}

	/**
	 * @return the totalNumberOfRows, see {@link #totalNumberOfRows}
	 */
	public int getTotalNumberOfRows() {
		return totalNumberOfRows;
	}
}
