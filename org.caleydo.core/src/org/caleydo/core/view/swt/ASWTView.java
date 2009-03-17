package org.caleydo.core.view.swt;

import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.view.AView;
import org.caleydo.core.view.swt.browser.HTMLBrowserViewRep;
import org.caleydo.core.view.swt.widget.SWTNativeWidget;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;

public abstract class ASWTView
	extends AView
	implements ISWTView {
	/**
	 * Constructor.
	 */
	public ASWTView(int iParentContainerID, String sLabel, int iViewID) {
		super(iParentContainerID, sLabel, iViewID);
	}

	@Override
	public abstract void drawView();

	@Override
	public abstract void initViewSWTComposite(Composite parentComposite);

	@Override
	public final void initViewRCP(final Composite parentComposite) {
		this.parentComposite = parentComposite;
		initViewSWTComposite(parentComposite);
		
//		if (this instanceof HTMLBrowserViewRep)
//			return;
//		
//		parentComposite.addMouseTrackListener(new MouseTrackAdapter(){
//			@Override
//			public void mouseEnter(MouseEvent e)
//			{
//				super.mouseEnter(e);
//				
//				GeneralManager.get().getViewGLCanvasManager().setActiveSWTView(parentComposite);
//			}
//		});
//		
//		parentComposite.addMouseMoveListener(new MouseMoveListener(){
//		
//			@Override
//			public void mouseMove(MouseEvent e)
//			{
//				System.out.println("moved mouse");
//			}
//		});
//		
//		parentComposite.addFocusListener(new FocusListener(){
//		
//			@Override
//			public void focusLost(FocusEvent e)
//			{	
//			}
//		
//			@Override
//			public void focusGained(FocusEvent e)
//			{
//				GeneralManager.get().getViewGLCanvasManager().setActiveSWTView(parentComposite);
//			}
//		});
	}

	@Override
	public void initView() {
		/**
		 * Method uses the parent container ID to retrieve the GUI widget by calling the createWidget method from
		 * the SWT GUI Manager. formally this was the method: retrieveGUIContainer()
		 */
		SWTNativeWidget sWTNativeWidget =
			(SWTNativeWidget) generalManager.getSWTGUIManager().createWidget(
				EManagedObjectType.GUI_SWT_NATIVE_WIDGET, iParentContainerId);

		parentComposite = sWTNativeWidget.getSWTWidget();

		initViewSWTComposite(parentComposite);
	}
}
