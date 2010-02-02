package org.caleydo.rcp.preferences;

import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for heat map specific settings
 * 
 * @author Alexander Lex
 */
public class GeneralPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private RadioGroupFieldEditor dataFilterLevelFE;
	private IntegerFieldEditor numRandomSamplesFE;
	private RadioGroupFieldEditor performanceLevelFE;

	public GeneralPreferencePage() {
		super(GRID);
		// setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setPreferenceStore(GeneralManager.get().getPreferenceStore());
		setDescription("General Preferences.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate
	 * various types of preferences. Each field editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		Composite mainComp = new Composite(getFieldEditorParent(), SWT.NULL);
		// Create the layout.
		RowLayout layout = new RowLayout();
		mainComp.setLayout(layout);

		dataFilterLevelFE =
			new RadioGroupFieldEditor(PreferenceConstants.DATA_FILTER_LEVEL,
				"Filter level for gene expression data.", 1, new String[][] { { "No Filtering", "complete" },
						{ "Use only values that have a DAVID ID Mapping", "only_mapping" },
						{ "Use only values that occur in pathways", "only_context" } }, mainComp);
		// dataFilterLevelFE.loadDefault();
		addField(dataFilterLevelFE);

		Label label = new Label(mainComp, SWT.NONE);
		// label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label
			.setText("Note that this only applies for standalone views. \nViews in bucket show only elements that occur in pathways \nby default (You will have to restart for this to take effect)");

		new Label(mainComp, SWT.SEPARATOR | SWT.HORIZONTAL);

		Composite sampleComposite = new Composite(mainComp, SWT.NULL);

		numRandomSamplesFE =
			new IntegerFieldEditor(PreferenceConstants.PC_NUM_RANDOM_SAMPLING_POINT,
				"Number of Random Samples:", sampleComposite);
		numRandomSamplesFE.loadDefault();
		addField(numRandomSamplesFE);

		// Composite sampleComposite = new Composite(mainComp, SWT.NULL);

//		performanceLevelFE =
//			new RadioGroupFieldEditor(PreferenceConstants.PERFORMANCE_LEVEL,
//				"Choose the performance level for your computer", 1, new String[][] { { "High", "high" },
//						{ "Low", "low" } }, mainComp);
//		addField(performanceLevelFE);

		// sampleGroup.pack();
		// getFieldEditorParent().pack();

	}

	@Override
	protected void performDefaults() {

	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	public boolean performOk() {

		boolean bReturn = super.performOk();

		// FIXME after view plugin reogranization
		// Collection<AGLView> eventListeners =
		// GeneralManager.get().getViewGLCanvasManager().getAllGLEventListeners();
		// for (AGLView glView : eventListeners) {
		// if (glView instanceof GLParallelCoordinates) {
		// GLParallelCoordinates parCoords = (GLParallelCoordinates) glView;
		// // if(!heatMap.isRenderedRemote())
		// // {
		// parCoords.setNumberOfSamplesToShow(numRandomSamplesFE.getIntValue());
		// // }
		// }
		// }

		return bReturn;
	}

}