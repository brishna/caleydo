package org.caleydo.rcp.views;

import java.util.ArrayList;
import java.util.logging.Level;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.command.view.swt.CmdViewCreateDataEntitySearcher;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering;
import org.caleydo.rcp.Application;
import org.caleydo.rcp.EApplicationMode;
import org.caleydo.rcp.action.view.TakeSnapshotAction;
import org.caleydo.rcp.action.view.remote.CloseOrResetContainedViews;
import org.caleydo.rcp.action.view.remote.SearchAction;
import org.caleydo.rcp.action.view.remote.ToggleLayoutAction;
import org.caleydo.rcp.util.search.SearchBar;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;

public class GLRemoteRenderingView
	extends AGLViewPart
{

	public static final String ID = "org.caleydo.rcp.views.GLRemoteRenderingView";

	private ArrayList<Integer> iAlContainedViewIDs;

	/**
	 * Constructor.
	 */
	public GLRemoteRenderingView()
	{
		super();

		createToolBarItems(-1);
		iAlContainedViewIDs = new ArrayList<Integer>();
	}

	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);

		createGLCanvas();

		// Only create parcoords and heatmap if the application is NOT in
		// pathway viewer mode
		if (Application.applicationMode != EApplicationMode.PATHWAY_VIEWER)
		{
			iAlContainedViewIDs.add(createGLEventListener(ECommandType.CREATE_GL_HEAT_MAP_3D,
					-1, true));
			// iAlContainedViewIDs.add(createGLEventListener(ECommandType.CREATE_GL_TEXTURE_HEAT_MAP_3D,
			// -1));
			iAlContainedViewIDs.add(createGLEventListener(
					ECommandType.CREATE_GL_PARALLEL_COORDINATES_GENE_EXPRESSION, -1, true));
			// iAlContainedViewIDs.add(createGLEventListener(ECommandType.CREATE_GL_CELL,
			// -1, true));
			// FIXME: This is just a temporary solution to check if glyph view
			// should be added to bucket.
			try
			{
				GeneralManager.get().getIDManager().getInternalFromExternalID(453010);
				iAlContainedViewIDs.add(createGLEventListener(ECommandType.CREATE_GL_GLYPH,
						-1, true));
				iAlContainedViewIDs.add(createGLEventListener(ECommandType.CREATE_GL_GLYPH,
						-1, true));
			}
			catch (IllegalArgumentException e)
			{
				GeneralManager.get().getLogger().log(Level.WARNING,
						"Cannot add glyph to bucket! No glyph data loaded!");
			}
		}

		createGLEventListener(ECommandType.CREATE_GL_BUCKET_3D, glCanvas.getID(), true);

		// Trigger gene/pathway search command
		CmdViewCreateDataEntitySearcher cmd = (CmdViewCreateDataEntitySearcher) GeneralManager
				.get().getCommandManager().createCommandByType(
						ECommandType.CREATE_VIEW_DATA_ENTITY_SEARCHER);
		cmd.doCommand();

		GLRemoteRendering glRemoteRenderedView = ((GLRemoteRendering) GeneralManager.get()
				.getViewGLCanvasManager().getGLEventListener(iGLEventListenerID));

		glRemoteRenderedView.setInitialContainedViews(iAlContainedViewIDs);
	}

	public static void createToolBarItems(int iViewID)
	{
		alToolbar = new ArrayList<IAction>();

		IAction takeSnapshotAction = new TakeSnapshotAction(iViewID);
		alToolbar.add(takeSnapshotAction);
		IAction closeOrResetContainedViews = new CloseOrResetContainedViews(iViewID);
		alToolbar.add(closeOrResetContainedViews);
		IAction toggleLayoutAction = new ToggleLayoutAction(iViewID);
		alToolbar.add(toggleLayoutAction);

		if (GeneralManager.get().getPreferenceStore().getBoolean(
				PreferenceConstants.XP_CLASSIC_STYLE_MODE))
		{
			alToolbar.add(new SearchAction(iViewID));
		}
	}

	protected final void fillToolBar()
	{
		if (alToolbar == null)
		{
			createToolBarItems(iGLEventListenerID);
		}

		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		fillToolBar(toolBarManager);

	}

	/**
	 * Overloads static fillToolBar method in AGLViewPart because the search bar
	 * must be added in a different way as usual toolbar items.
	 * 
	 * @param toolBarManager
	 */
	public static void fillToolBar(final IToolBarManager toolBarManager)
	{
		// Add search bar
		if (!GeneralManager.get().getPreferenceStore().getBoolean(
				PreferenceConstants.XP_CLASSIC_STYLE_MODE))
		{
			toolBarManager.add(new SearchBar("Quick search"));
		}

		for (IAction toolBarAction : alToolbar)
		{
			toolBarManager.add(toolBarAction);
		}
	}

	@Override
	public void dispose()
	{
		super.dispose();

		for (Integer iContainedViewID : iAlContainedViewIDs)
		{
			GeneralManager.get().getViewGLCanvasManager().unregisterGLEventListener(
					iContainedViewID);
		}

		// TODO: cleanup data entity searcher view
	}
}