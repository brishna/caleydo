package org.caleydo.core.view.swt.browser;

import java.util.ArrayList;
import java.util.Iterator;

import org.caleydo.core.data.graph.pathway.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.mapping.EMappingType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.util.graph.EGraphItemProperty;
import org.caleydo.util.graph.IGraphItem;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;

public class IDExtractionLocationListener
	extends LocationAdapter {
	private IGeneralManager generalManager;

	private boolean bSkipNextChangeEvent = false;

	/**
	 * Constructor.
	 */
	public IDExtractionLocationListener(final Browser browser, final int iBrowserId, final int iSelectionSetId) {
		generalManager = GeneralManager.get();
	}

	@Override
	public void changed(LocationEvent event) {

	}

	@Override
	// http://www.genome.jp/dbget-bin/show_pathway?map00020+1.1.1.37
	// http://www.genome.jp/dbget-bin/www_bget?hsa+4190
	// http://www.genome.jp/dbget-bin/www_bget?compound+C00003
	// http://www.genome.jp/dbget-bin/www_bget?enzyme+3.6.3.5
	public void changing(LocationEvent event) {

		if (bSkipNextChangeEvent == true) {
			bSkipNextChangeEvent = false;
			return;
		}

		bSkipNextChangeEvent = false;

		String sSearchPhrase_NCBIGeneId = "http://www.genome.jp/dbget-bin/www_bget?hsa+";
		String sSearchPhrase_Pathway = "http://www.genome.jp/dbget-bin/show_pathway?hsa";

		ArrayList<Integer> iAlSelectionId = null;
		ArrayList<Integer> iAlSelectionDepth = null;
		if (event.location.contains(sSearchPhrase_NCBIGeneId)) {
			String sExtractedID = event.location.substring(sSearchPhrase_NCBIGeneId.length());

			Integer iDavidId =
				generalManager.getIDMappingManager().getID(EIDType.ENTREZ_GENE_ID, EIDType.DAVID,
					Integer.valueOf(sExtractedID));

			if (iDavidId == null || iDavidId == -1)
				return;

			PathwayVertexGraphItem vertexItemBuffer =
				generalManager.getPathwayItemManager().getPathwayVertexGraphItemByDavidId(iDavidId);

			if (vertexItemBuffer == null)
				return;

			Iterator<IGraphItem> iterList =
				vertexItemBuffer.getAllItemsByProp(EGraphItemProperty.ALIAS_CHILD).iterator();

			iAlSelectionId = new ArrayList<Integer>();
			iAlSelectionDepth = new ArrayList<Integer>();

			while (iterList.hasNext()) {
				IGraphItem bufferItem = iterList.next();
				iAlSelectionId.add(bufferItem.getId());
				iAlSelectionDepth.add(0);
			}

		}
		else if (event.location.contains(sSearchPhrase_Pathway)) {
			// Prevent loading of clicked pathway URL
			event.doit = false;

			int iPathwayIdIndex = 0;

			// Extract clicked pathway ID
			if (event.location.contains("map0")) {
				iPathwayIdIndex = event.location.lastIndexOf("map0") + 4;
			}
			else if (event.location.contains("hsa0")) {
				iPathwayIdIndex = event.location.lastIndexOf("hsa0") + 4;
			}
			else
				return;

			Integer.valueOf(event.location.substring(iPathwayIdIndex, event.location.lastIndexOf('+')))
				.intValue();

			// iArSelectionId = new int[0];
			// iArSelectionDepth = new int[0];

			// iAlOptional.add(iPathwayId);
		}
		else
			return;

		// TODO reimplement
		// Selection tmpSelectionSet = (Selection)
		// generalManager.getSetManager().getItem(
		// iSelectionSetId);
		// tmpSelectionSet.updateSelectionSet(iBrowserId, iAlSelectionId,
		// iAlSelectionDepth,
		// iAlOptional);
	}

	public void updateSkipNextChangeEvent(boolean bSkipNextChangeEvent) {

		this.bSkipNextChangeEvent = bSkipNextChangeEvent;
	}
}
