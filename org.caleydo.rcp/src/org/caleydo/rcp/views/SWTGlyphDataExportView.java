package org.caleydo.rcp.views;

import org.caleydo.core.manager.event.mediator.IMediatorReceiver;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.view.swt.glyph.GlyphDataExportViewRep;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SWTGlyphDataExportView
	extends ViewPart
{
	public static final String ID = "org.caleydo.rcp.views.SWTGlyphDataExportView";

	private GlyphDataExportViewRep GDEview;

	@Override
	public void createPartControl(Composite parent)
	{
		GDEview = (GlyphDataExportViewRep) GeneralManager.get().getViewGLCanvasManager()
				.createView(EManagedObjectType.VIEW_SWT_GLYPH_DATAEXPORT, -1,
						"Glyph Data Export");

		GDEview.initViewRCP(parent);
		GDEview.drawView();

		GeneralManager.get().getViewGLCanvasManager().registerItem(GDEview);
	}

	@Override
	public void setFocus()
	{

	}

	@Override
	public void dispose()
	{
		super.dispose();

		GeneralManager.get().getEventPublisher().removeReceiver((IMediatorReceiver) GDEview);

		GeneralManager.get().getViewGLCanvasManager().unregisterItem(GDEview.getID());
	}
}
