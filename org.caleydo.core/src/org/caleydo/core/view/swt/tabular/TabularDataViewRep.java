package org.caleydo.core.view.swt.tabular;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.logging.Level;
import org.caleydo.core.data.IUniqueObject;
import org.caleydo.core.data.collection.ESetType;
import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.graph.pathway.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.data.selection.DeltaConverter;
import org.caleydo.core.data.selection.DeltaEventContainer;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.GenericSelectionManager;
import org.caleydo.core.data.selection.ISelectionDelta;
import org.caleydo.core.data.selection.IVirtualArray;
import org.caleydo.core.data.selection.IVirtualArrayDelta;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionCommandEventContainer;
import org.caleydo.core.manager.IIDMappingManager;
import org.caleydo.core.manager.event.EMediatorType;
import org.caleydo.core.manager.event.IEventContainer;
import org.caleydo.core.manager.event.IMediatorReceiver;
import org.caleydo.core.manager.event.IMediatorSender;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.mapping.IDMappingHelper;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.caleydo.core.view.IView;
import org.caleydo.core.view.opengl.canvas.storagebased.EDataFilterLevel;
import org.caleydo.core.view.opengl.canvas.storagebased.EStorageBasedVAType;
import org.caleydo.core.view.swt.ASWTView;
import org.caleydo.core.view.swt.ISWTView;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * View shows data from a set in a tabular format.
 * 
 * @author Marc Streit
 */
public class TabularDataViewRep
	extends ASWTView
	implements IView, ISWTView, IMediatorReceiver, IMediatorSender
{
	private static int MAX_PREVIEW_TABLE_ROWS = 20;

	protected ISet set;
	protected ESetType setType;

	/**
	 * map selection type to unique id for virtual array
	 */
	protected EnumMap<EStorageBasedVAType, Integer> mapVAIDs;

	/**
	 * This manager is responsible for the content in the storages (the indices)
	 */
	protected GenericSelectionManager contentSelectionManager;

	/**
	 * This manager is responsible for the management of the storages in the set
	 */
	protected GenericSelectionManager storageSelectionManager;

	/**
	 * The id of the virtual array that manages the contents (the indices) in
	 * the storages
	 */
	protected int iContentVAID = -1;

	/**
	 * The id of the virtual array that manages the storage references in the
	 * set
	 */
	protected int iStorageVAID = -1;
	
	/**
	 * Define what level of filtering on the data should be applied
	 */
	protected EDataFilterLevel dataFilterLevel;

	private IIDMappingManager idMappingManager;

	private Composite composite;
	
	private Table labelTable;
	private Table contentTable;
	
	private TableViewer contentTableViewer;
	
	private TableCursor cursor;
	
	private Label lastRemove;

	/**
	 * Constructor.
	 */
	public TabularDataViewRep(final int iParentContainerId, final String sLabel)
	{
		super(iParentContainerId, sLabel, GeneralManager.get().getIDManager().createID(
				EManagedObjectType.VIEW_SWT_TABULAR_DATA_VIEWER));

		GeneralManager.get().getEventPublisher().addSender(EMediatorType.SELECTION_MEDIATOR,
				this);
		GeneralManager.get().getEventPublisher().addReceiver(EMediatorType.SELECTION_MEDIATOR,
				this);
		
		setType = ESetType.GENE_EXPRESSION_DATA;
		mapVAIDs = new EnumMap<EStorageBasedVAType, Integer>(EStorageBasedVAType.class);

		contentSelectionManager = new GenericSelectionManager.Builder(EIDType.EXPRESSION_INDEX)
				.externalIDType(EIDType.REFSEQ_MRNA_INT).mappingType(
						EMappingType.EXPRESSION_INDEX_2_REFSEQ_MRNA_INT,
						EMappingType.REFSEQ_MRNA_INT_2_EXPRESSION_INDEX).build();
		storageSelectionManager = new GenericSelectionManager.Builder(EIDType.EXPERIMENT_INDEX)
				.build();

		idMappingManager = generalManager.getIDMappingManager();
	}

	@Override
	public void initViewSWTComposite(Composite parentComposite)
	{
		composite = new Composite(parentComposite, SWT.NULL);
	    GridLayout layout = new GridLayout(2, false);
	    layout.marginWidth = layout.marginHeight = layout.horizontalSpacing = 0;
	    composite.setLayout(layout);

		initData();
		createPreviewTable();
	}

	@Override
	public void drawView()
	{

	}

	public void initData()
	{
		set = null;

		for (ISet currentSet : alSets)
		{
			if (currentSet.getSetType() == setType)
				set = currentSet;
		}

		String sLevel = GeneralManager.get().getPreferenceStore().getString(
				PreferenceConstants.DATA_FILTER_LEVEL);
		if (sLevel.equals("complete"))
		{
			dataFilterLevel = EDataFilterLevel.COMPLETE;
		}
		else if (sLevel.equals("only_mapping"))
		{
			dataFilterLevel = EDataFilterLevel.ONLY_MAPPING;
		}
		else if (sLevel.equals("only_context"))
		{
			dataFilterLevel = EDataFilterLevel.ONLY_CONTEXT;
		}
		else
		{
			throw new IllegalStateException("Unknown data filter level");
		}
		
		if (!mapVAIDs.isEmpty())
		{

			// This should be done once we get some thread safety, memory leak,
			// and a big one

			for (EStorageBasedVAType eSelectionType : EStorageBasedVAType.values())
			{
				if (mapVAIDs.containsKey(eSelectionType))
					set.removeVirtualArray(mapVAIDs.get(eSelectionType));
			}
			iContentVAID = -1;
			iStorageVAID = -1;
			mapVAIDs.clear();
		}

		if (set == null)
		{
			mapVAIDs.clear();
			contentSelectionManager.resetSelectionManager();
			storageSelectionManager.resetSelectionManager();
			return;
		}

		ArrayList<Integer> alTempList = new ArrayList<Integer>();
		// create VA with empty list
		int iVAID = set.createStorageVA(alTempList);
		mapVAIDs.put(EStorageBasedVAType.EXTERNAL_SELECTION, iVAID);

		alTempList = new ArrayList<Integer>();

		for (int iCount = 0; iCount < set.size(); iCount++)
		{
			alTempList.add(iCount);
		}

		iVAID = set.createSetVA(alTempList);
		mapVAIDs.put(EStorageBasedVAType.STORAGE_SELECTION, iVAID);

		
		// Set<Integer> setMouseOver = storageSelectionManager
		// .getElements(ESelectionType.MOUSE_OVER);


		if (!mapVAIDs.containsKey(EStorageBasedVAType.COMPLETE_SELECTION))
			initCompleteList();
		iContentVAID = mapVAIDs.get(EStorageBasedVAType.COMPLETE_SELECTION);

		iStorageVAID = mapVAIDs.get(EStorageBasedVAType.STORAGE_SELECTION);

		contentSelectionManager.resetSelectionManager();
		storageSelectionManager.resetSelectionManager();

		contentSelectionManager.setVA(set.getVA(iContentVAID));
		storageSelectionManager.setVA(set.getVA(iStorageVAID));

		int iNumberOfColumns = set.getVA(iContentVAID).size();
		int iNumberOfRows = set.getVA(iStorageVAID).size();

		for (int iRowCount = 0; iRowCount < iNumberOfRows; iRowCount++)
		{
			storageSelectionManager.initialAdd(set.getVA(iStorageVAID).get(iRowCount));

		}

		// this for loop executes one per axis
		for (int iColumnCount = 0; iColumnCount < iNumberOfColumns; iColumnCount++)
		{
			contentSelectionManager.initialAdd(set.getVA(iContentVAID).get(iColumnCount));
		}
	}

	/**
	 * Initializes a virtual array with all elements, according to the data
	 * filters, as defined in {@link EDataFilterLevel}.
	 */
	protected final void initCompleteList()
	{
		// initialize virtual array that contains all (filtered) information
		ArrayList<Integer> alTempList = new ArrayList<Integer>(set.depth());

		for (int iCount = 0; iCount < set.depth(); iCount++)
		{
			if (dataFilterLevel != EDataFilterLevel.COMPLETE)
			{
				// Here we get mapping data for all values
				// FIXME: not general, only for genes
				int iDavidID = IDMappingHelper.get().getDavidIDFromStorageIndex(iCount);

				if (iDavidID == -1)
				{
					generalManager.getLogger().log(Level.FINE,
							"Cannot resolve gene to DAVID ID!");
					continue;
				}

				if (dataFilterLevel == EDataFilterLevel.ONLY_CONTEXT)
				{
					// Here all values are contained within pathways as well
					int iGraphItemID = generalManager.getPathwayItemManager()
							.getPathwayVertexGraphItemIdByDavidId(iDavidID);

					if (iGraphItemID == -1)
						continue;

					PathwayVertexGraphItem tmpPathwayVertexGraphItem = ((PathwayVertexGraphItem) generalManager
							.getPathwayItemManager().getItem(iGraphItemID));

					if (tmpPathwayVertexGraphItem == null)
						continue;
				}
			}
			alTempList.add(iCount);
		}

		// TODO: remove possible old virtual array
		int iVAID = set.createStorageVA(alTempList);
		mapVAIDs.put(EStorageBasedVAType.COMPLETE_SELECTION, iVAID);

	}

	private void createPreviewTable()
	{
	    Composite labelTableComposite = new Composite(composite, SWT.NULL);
		GridData data = new GridData();
	    data.widthHint = 250;
	    data.verticalAlignment = SWT.FILL;
	    data.grabExcessHorizontalSpace = false;
	    data.grabExcessVerticalSpace = true;
	    labelTableComposite.setLayoutData(data);        
	    GridLayout layout = new GridLayout(1, false);
	    layout.marginWidth = layout.marginHeight = layout.horizontalSpacing = 0;
	    labelTableComposite.setLayout(layout);
	    
		labelTable = new Table(labelTableComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION| SWT.VIRTUAL);
		labelTable.setLinesVisible(true);
		labelTable.setHeaderVisible(true);
	    
	    data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
	    data.verticalAlignment = SWT.TOP;
	    data.grabExcessHorizontalSpace = false;
	    data.grabExcessVerticalSpace = true;
	    labelTable.setLayoutData(data);
		
	    // Create the table viewer to display the players
//	    contentTableViewer = new TableViewer(composite);

	    // Set the content and label providers
//	    contentTableViewer.setContentProvider(new PlayerContentProvider());
//	    contentTableViewer.setLabelProvider(new PlayerLabelProvider());
//	    contentTableViewer.setSorter(new PlayerViewerSorter());

//		contentTable = contentTableViewer.getTable();
		contentTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION| SWT.VIRTUAL);
		contentTable.setLinesVisible(true);
		contentTable.setHeaderVisible(true);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 300;
		data.widthHint = 700;
		contentTable.setLayoutData(data);
	    
		// Clear table if not empty
		contentTable.removeAll();
		
		// Make selection the same in both tables
		labelTable.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				contentTable.setSelection(labelTable.getSelectionIndices());
			}
		});
		contentTable.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				labelTable.setSelection(contentTable.getSelectionIndices());
			}
		});
		// On Windows, the selection is gray if the table does not have focus.
		// To make both tables appear in focus, draw teh selection background
		// here.
		// This part only works on version 3.2 or later.
		Listener eraseListener = new Listener()
		{
			public void handleEvent(Event event)
			{
				if ((event.detail & SWT.SELECTED) != 0)
				{
					GC gc = event.gc;
					Rectangle rect = event.getBounds();
					gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
					gc.fillRectangle(rect);
					event.detail &= ~SWT.SELECTED;
				}
			}
		};

		labelTable.addListener(SWT.EraseItem, eraseListener);
		contentTable.addListener(SWT.EraseItem, eraseListener);
		// Make vertical scrollbars scroll together
		ScrollBar vBarLeft = labelTable.getVerticalBar();
		vBarLeft.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				contentTable.setTopIndex(labelTable.getTopIndex());
			}
		});

		ScrollBar vBarRight = contentTable.getVerticalBar();
		vBarRight.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				labelTable.setTopIndex(contentTable.getTopIndex());
			}
		});
		// Horizontal bar on second table takes up a little extra space.
		// To keep vertical scroll bars in sink, force table1 to end above
		// horizontal scrollbar
		ScrollBar hBarRight = contentTable.getHorizontalBar();
		Label spacer = new Label(composite, SWT.NONE);
		GridData spacerData = new GridData();
		spacerData.heightHint = hBarRight.getSize().y;
		spacer.setVisible(false);
		composite.setBackground(labelTable.getBackground());
		
		for (TableColumn tmpColumn : contentTable.getColumns())
		{
			tmpColumn.dispose();
		}

		final TableEditor editor = new TableEditor(contentTable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		contentTable.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				super.widgetSelected(e);
				
//				TableItem selectedItem = ((TableItem)e.item);
//				final int iSelectedRowIndex = contentTable.indexOf(selectedItem);
//				addRemoveIcon(iSelectedRowIndex);
//				triggerContentSelectionEvent(iSelectedRowIndex, ESelectionType.SELECTION);

			}
		});

		contentTable.addListener(SWT.MouseDown, new Listener()
		{
			public void handleEvent(Event event)
			{
				Rectangle clientArea = contentTable.getClientArea();
				Point pt = new Point(event.x, event.y);

				int index = 0; // only make caption line editable

				boolean visible = false;
				final TableItem item = contentTable.getItem(index);
				for (int iColIndex = 1; iColIndex < contentTable.getColumnCount(); iColIndex++)
				{
					Rectangle rect = item.getBounds(iColIndex);
					if (rect.contains(pt))
					{
						final int column = iColIndex;
						final Text text = new Text(contentTable, SWT.NONE);
						Listener textListener = new Listener()
						{
							public void handleEvent(final Event e)
							{
								switch (e.type)
								{
									case SWT.FocusOut:
										item.setText(column, text.getText());
										text.dispose();
										break;
									case SWT.Traverse:
										switch (e.detail)
										{
											case SWT.TRAVERSE_RETURN:
												item.setText(column, text.getText());

												// FALL THROUGH
											case SWT.TRAVERSE_ESCAPE:
												text.dispose();
												e.doit = false;
										}
										break;
								}
							}
						};

						text.addListener(SWT.FocusOut, textListener);
						text.addListener(SWT.Traverse, textListener);
						editor.setEditor(text, item, iColIndex);
						text.setText(item.getText(iColIndex));
						text.selectAll();
						text.setFocus();
						return;
					}

					if (!visible && rect.intersects(clientArea))
					{
						visible = true;
					}
				}

				if (!visible)
					return;
				index++;
			}
		});

		TableColumn column;
		TableItem item;
		float fValue;

		column = new TableColumn(labelTable, SWT.NONE);
		column.setText("#");
		column.setWidth(50);

		column = new TableColumn(labelTable, SWT.NONE);
		column.setText("RefSeq ID");
		column.setWidth(110);

		column = new TableColumn(labelTable, SWT.NONE);
		column.setText("Gene Symbol");
		column.setWidth(110);

		for (Integer iStorageIndex : set.getVA(iStorageVAID))
		{
			column = new TableColumn(contentTable, SWT.NONE);
			column.setText(set.get(iStorageIndex).getLabel());
			column.setWidth(120);
			column.setMoveable(true);
//			column.setImage(generalManager.getResourceLoader().getImage(
//					column.getDisplay(), "resources/icons/general/remove.png"));
//			column.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e)
//				{
//					System.out.println(e.toString());
//				}
//			});
		}

		IVirtualArray storageVA = set.getVA(iStorageVAID);

		int iRefSeqID = 0;
		String sGeneSymbol = "";
		for (Integer iContentIndex : set.getVA(iContentVAID))
		{
			// line number
			item = new TableItem(labelTable, SWT.NONE);
//			item.setData(iContentIndex);
			item.setText(0, Integer.toString(iContentIndex));

			iRefSeqID = idMappingManager.getID(
					EMappingType.EXPRESSION_INDEX_2_REFSEQ_MRNA_INT, iContentIndex);

			// RefSeq ID
			item.setText(1, (String) idMappingManager.getID(
					EMappingType.REFSEQ_MRNA_INT_2_REFSEQ_MRNA, iRefSeqID));

			// Gene Symbol
			sGeneSymbol = (String) idMappingManager.getID(EMappingType.DAVID_2_GENE_SYMBOL,
					idMappingManager.getID(EMappingType.REFSEQ_MRNA_INT_2_DAVID, iRefSeqID));

			if (sGeneSymbol != null)
				item.setText(2, sGeneSymbol);
			else
				item.setText(2, "Unknown");

			item = new TableItem(contentTable, SWT.NONE);
			
			for (Integer iStorageIndex : storageVA)
			{
				fValue = set.get(iStorageIndex).getFloat(EDataRepresentation.NORMALIZED,
						iContentIndex);

				item.setText(iStorageIndex, Float.toString(fValue));
			}
			
//			if (iContentIndex > 1000)
//				break;
		}
		

		cursor = new TableCursor(contentTable, SWT.NONE);
		cursor.addSelectionListener(new SelectionAdapter() {
			// when the TableEditor is over a cell, select the corresponding row in 
			// the table
			public void widgetSelected(SelectionEvent e) {
				
				int iStorageIndex = cursor.getColumn();
				contentTable.setSelection(new TableItem[] {cursor.getRow()});
				
				int iRowIndex = contentTable.indexOf(cursor.getRow());
				int iRefSeqID = set.getVA(iContentVAID).get(iRowIndex);
				
				triggerStorageSelectionEvent(iStorageIndex, ESelectionType.SELECTION);				
				triggerContentSelectionEvent(iRefSeqID, ESelectionType.SELECTION);
				
				addRemoveIcon(iRowIndex);
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleExternalEvent(IUniqueObject eventTrigger, IEventContainer eventContainer)
	{
		switch (eventContainer.getEventType())
		{
			case SELECTION_UPDATE:
				DeltaEventContainer<ISelectionDelta> selectionDeltaEventContainer = (DeltaEventContainer<ISelectionDelta>) eventContainer;
				handleSelectionUpdate(eventTrigger, selectionDeltaEventContainer
						.getSelectionDelta());
				break;
			case VA_UPDATE:
				DeltaEventContainer<IVirtualArrayDelta> vaDeltaEventContainer = (DeltaEventContainer<IVirtualArrayDelta>) eventContainer;
				handleVAUpdate(eventTrigger, vaDeltaEventContainer.getSelectionDelta());
				break;
			case TRIGGER_SELECTION_COMMAND:
				SelectionCommandEventContainer commandEventContainer = (SelectionCommandEventContainer) eventContainer;
				switch (commandEventContainer.getIDType())
				{
					case DAVID:
					case REFSEQ_MRNA_INT:
					case EXPRESSION_INDEX:
						contentSelectionManager.executeSelectionCommands(commandEventContainer
								.getSelectionCommands());
						break;
					case EXPERIMENT_INDEX:
						storageSelectionManager.executeSelectionCommands(commandEventContainer
								.getSelectionCommands());
						break;
				}
		}
	}

	private void handleSelectionUpdate(IUniqueObject eventTrigger,
			ISelectionDelta selectionDelta)
	{
		// Check for type that can be handled
		if (selectionDelta.getIDType() == EIDType.REFSEQ_MRNA_INT
				|| selectionDelta.getIDType() == EIDType.EXPRESSION_INDEX)
		{
			contentSelectionManager.setDelta(selectionDelta);
//			ISelectionDelta internalDelta = contentSelectionManager.getCompleteDelta();
			reactOnExternalSelection();
		}

		else if (selectionDelta.getIDType() == EIDType.EXPERIMENT_INDEX)
		{
			storageSelectionManager.setDelta(selectionDelta);
		}
	}

	private void handleVAUpdate(IUniqueObject eventTrigger, IVirtualArrayDelta delta)
	{
		GenericSelectionManager selectionManager;
		if (delta.getIDType() == EIDType.EXPERIMENT_INDEX)
		{
			selectionManager = storageSelectionManager;
		}
		else if (delta.getIDType() == EIDType.REFSEQ_MRNA_INT)
		{
			delta = DeltaConverter.convertDelta(
					EIDType.EXPRESSION_INDEX, delta);
			selectionManager = contentSelectionManager;
		}
		else if (delta.getIDType() == EIDType.EXPRESSION_INDEX)
		{
			selectionManager = contentSelectionManager;
		}
		else
		{
			return;
		}
		
//		reactOnVAChanges(delta);
		selectionManager.setVADelta(delta);
		reactOnExternalSelection();
	}
	
	/**
	 * Highlight the selected cell in the table.
	 * Only the first element is taken, since we cannot handle multiple selections ATM.
	 */
	private void reactOnExternalSelection()
	{		
		composite.getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				contentTable.deselectAll();
				labelTable.deselectAll();
				
				Iterator<Integer> iterContentIndex = contentSelectionManager.getElements(ESelectionType.SELECTION).iterator();
				while (iterContentIndex.hasNext())
				{
					int iRowIndex = set.getVA(iContentVAID).indexOf(
							iterContentIndex.next());
					cursor.setSelection(iRowIndex, cursor.getColumn());
					contentTable.select(iRowIndex);
					labelTable.select(iRowIndex);
					addRemoveIcon(iRowIndex);
//					contentTable.setSelection(iRowIndex);
//					labelTable.setSelection(iRowIndex);
				}
				
				Iterator<Integer> iterStorageIndex = storageSelectionManager.getElements(ESelectionType.SELECTION).iterator();
				if (iterStorageIndex.hasNext())
				{
					cursor.setSelection(cursor.getRow(), iterStorageIndex.next());
				}		
			}
		});		
	}
	
//	@Override
//	protected void reactOnVAChanges(IVirtualArrayDelta delta)
//	{
//		if (delta.getIDType() == eAxisDataType)
//		{
//
//			IVirtualArray axisVA = set.getVA(iAxisVAID);
//			for (VADeltaItem item : delta)
//			{
//				int iElement = axisVA.get(item.getIndex());
//				if (item.getType() == EVAOperation.REMOVE)
//				{
//					resetAxisSpacing();
//					if (axisVA.containsElement(iElement) == 1)
//						hashGates.remove(iElement);
//				}
//				else if (item.getType() == EVAOperation.REMOVE_ELEMENT)
//				{
//					resetAxisSpacing();
//					hashGates.remove(item.getPrimaryID());
//				}
//			}
//		}
//	}
	
	private void triggerContentSelectionEvent(int iContentIndex, ESelectionType eSelectionType)
	{
		if (contentSelectionManager.checkStatus(eSelectionType, iContentIndex))
			return;
		
		contentSelectionManager.clearSelection(eSelectionType);

		// Resolve multiple spotting on chip and add all to the
		// selection manager.
		Integer iRefSeqID = idMappingManager.getID(
				EMappingType.EXPRESSION_INDEX_2_REFSEQ_MRNA_INT, iContentIndex);
		for (Object iExpressionIndex : idMappingManager.getMultiID(
				EMappingType.REFSEQ_MRNA_INT_2_EXPRESSION_INDEX, iRefSeqID))
		{
			contentSelectionManager.addToType(eSelectionType,
					(Integer) iExpressionIndex);
		}

		ISelectionDelta selectionDelta = contentSelectionManager.getDelta();

		triggerEvent(EMediatorType.SELECTION_MEDIATOR,
				new SelectionCommandEventContainer(EIDType.REFSEQ_MRNA_INT,
						new SelectionCommand(ESelectionCommandType.CLEAR,
								eSelectionType)));

		triggerEvent(EMediatorType.SELECTION_MEDIATOR,
				new DeltaEventContainer<ISelectionDelta>(selectionDelta));
	}

	private void triggerStorageSelectionEvent(int iStorageIndex, ESelectionType eSelectionType)
	{
		if (storageSelectionManager.checkStatus(eSelectionType, iStorageIndex))
			return;
		
		storageSelectionManager.clearSelection(eSelectionType);
		storageSelectionManager.addToType(eSelectionType, iStorageIndex);
		
		triggerEvent(EMediatorType.SELECTION_MEDIATOR,
				new SelectionCommandEventContainer(EIDType.EXPERIMENT_INDEX,
						new SelectionCommand(ESelectionCommandType.CLEAR,
								eSelectionType)));
		ISelectionDelta selectionDelta = storageSelectionManager.getDelta();
		triggerEvent(EMediatorType.SELECTION_MEDIATOR,
				new DeltaEventContainer<ISelectionDelta>(selectionDelta));
	}
	
	@Override
	public void triggerEvent(EMediatorType eMediatorType, IEventContainer eventContainer)
	{
		generalManager.getEventPublisher().triggerEvent(eMediatorType, this, eventContainer);

	}
	
	private void addRemoveIcon(int iRowIndex)
	{
		Label lblRemove = new Label(labelTable, SWT.CENTER);
		lblRemove.setBackground(lblRemove.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		lblRemove.setImage(generalManager.getResourceLoader().getImage(
				lblRemove.getDisplay(), "resources/icons/general/remove.png"));
		lblRemove.setData(contentTable.getItem(iRowIndex));
		
		// Only one remove icon should be shown at a time
		if (lastRemove != null)
		{
			lastRemove.dispose();
		}
		
		lastRemove = lblRemove;
		
		final TableEditor editor = new TableEditor(labelTable);
		editor.grabHorizontal = true;
		editor.setEditor(lblRemove, contentTable.getItem(iRowIndex), 0);

		lblRemove.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				TableItem item = (TableItem)e.widget.getData();
				editor.getEditor().dispose();
				editor.dispose();
				item.dispose();
			}
		});

	}
}
