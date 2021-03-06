/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.io.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.caleydo.core.data.collection.table.TableUtils;
import org.caleydo.core.data.configuration.DataChooserComposite;
import org.caleydo.core.manager.GeneralManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * File dialog for exporting data files.
 *
 * @author Marc Streit
 * @author Alexander Lex
 */
public class ExportDataDialog
	extends Dialog
	implements IDataOKListener {

	private ArrayList<Integer> genesToExport = null;
	private ArrayList<Integer> experimentsToExport = null;

	// private Button[] radios = new Button[3];

	private Composite composite;

	private Text txtFileName;

	private String sFileName = "";
	private String sFilePath = "";

	DataChooserComposite dataChooserComposite;

	/**
	 * Constructor.
	 */
	public ExportDataDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Add data for group exports.
	 *
	 * @param genesToExport
	 *            the list of genes to export
	 * @param experimentsToExport
	 *            the list of experiments to export
	 */
	public void addGroupData(ArrayList<Integer> genesToExport, ArrayList<Integer> experimentsToExport) {
		this.genesToExport = genesToExport;
		this.experimentsToExport = experimentsToExport;

	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Export Data");
		newShell.setImage(GeneralManager.get().getResourceLoader()
			.getImage(newShell.getDisplay(), "resources/icons/general/export_data.png"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		createGUI(parent);
		return parent;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);

		return control;
	}

	private void createGUI(final Composite parent) {

		composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Button buttonFileChooser = new Button(composite, SWT.PUSH);
		buttonFileChooser.setText("Choose export destination..");

		txtFileName = new Text(composite, SWT.BORDER);
		txtFileName.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		buttonFileChooser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.SAVE);
				fileDialog.setText("Save");
				fileDialog.setFilterPath(sFilePath);
				String[] filterExt = { "*.csv", "*.txt", "*.*" };
				fileDialog.setFilterExtensions(filterExt);

				String sFilePath =
					"caleydo_export_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv";

				fileDialog.setFileName(sFilePath);
				sFileName = fileDialog.open();

				txtFileName.setText(sFileName);
				checkOK();

			}
		});

		dataChooserComposite = new DataChooserComposite(this, composite, null, SWT.NONE);

		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		gridData.horizontalSpan = 2;
		dataChooserComposite.setLayoutData(gridData);

		// --- old stuff

		// radios[0] = new Button(composite, SWT.RADIO);
		// radios[0].setText("Export bucket contents");
		// radios[0].setBounds(10, 5, 75, 30);
		//
		// radios[1] = new Button(composite, SWT.RADIO);
		// radios[1].setText("Export data as shown in the standalone views");
		// radios[1].setBounds(10, 30, 75, 30);
		// if (experimentsToExport == null)
		// radios[1].setSelection(true);
		//
		// radios[2] = new Button(composite, SWT.RADIO);
		// radios[2].setText("Export group data");
		// radios[2].setBounds(10, 30, 75, 30);
		// if (experimentsToExport == null) {
		// radios[2].setEnabled(false);
		// }
		// else {
		// radios[2].setSelection(true);
		// }

		// if (!doesHeatMapExist) {
		// radios[1].setEnabled(false);
		// }
		// else if (!bDoesBucketExist) {
		// radios[1].setSelection(true);
		// }

		// radios[2] = new Button(composite, SWT.RADIO);
		// radios[2].setText("Export Parallel Coordinates");
		// radios[2].setBounds(10, 30, 75, 30);
		// if (!doParallelCoordinatesExist) {
		// radios[1].setEnabled(false);
		// }
		// else if (!bDoesBucketExist) {
		// radios[1].setSelection(true);
		// }

	}

	/**
	 * Called internally by listeners
	 *
	 * @return
	 */
	private final boolean checkOK() {
		if (sFileName == null || !dataChooserComposite.isOK()) {
			return false;
		}
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;

	}

	@Override
	public void dataOK() {
		if (sFileName != null && dataChooserComposite.isOK()) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}

	@Override
	protected void okPressed() {

		TableUtils.export(dataChooserComposite.getDataDomain(), sFileName,
			dataChooserComposite.getRecordPerspective(), dataChooserComposite.getDimensionPerspective(),
 null, null, false, false);

		super.okPressed();
	}

	public static void main(String[] args) {
		new ExportDataDialog(new Shell()).open();
	}


}
