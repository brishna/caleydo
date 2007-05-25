package cerberus.manager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cerberus.manager.type.ManagerObjectType;
import cerberus.net.dwt.swing.WorkspaceSwingFrame;
import cerberus.view.gui.IViewRep;
import cerberus.view.gui.IView;
import cerberus.view.gui.ViewType;
import cerberus.view.gui.opengl.IGLCanvasUser;

/**
 * Manage all canvas, view, ViewRep's nad GLCanvas objects.
 * 
 * @author Michael Kalkusch
 */
public interface IViewManager 
extends IGeneralManager {
	
	public IView createView(final ManagerObjectType useViewType, 
			int iViewId, 
			int iParentContainerId, 
			String sLabel);
	
	public void destroyOnExit();

	public void addViewRep(IView refView);
	
	public void removeViewRep(IView refView) ;
	
	public Collection<IView> getAllViews();
	
	public Collection<IGLCanvasUser> getAllGLCanvasUsers();
	
	public ArrayList<IViewRep> getViewRepByType(ViewType viewType);
	
	/**
	 * Create a new JFrame.
	 * 
	 * @param useViewCanvasType
	 * @param sAditionalParameter
	 * @return
	 */
	public WorkspaceSwingFrame createWorkspace( 
			final ManagerObjectType useViewCanvasType,
			final String sAditionalParameter );
	
	/**
	 * Get an iterator for all avaliable JFrames (== WorkspaceSwingFrame)
	 * 
	 * @return
	 */
	public Iterator<WorkspaceSwingFrame> getWorkspaceIterator();
}