/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 * 
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.pathway;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.AEventListener;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.IListenerOwner;
import org.caleydo.core.event.view.remote.LoadPathwayEvent;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.core.view.opengl.canvas.listener.AddPathwayListener;
import org.caleydo.core.view.opengl.canvas.listener.IRemoteRenderingHandler;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class RcpGLPathwayView extends ARcpGLViewPart implements IListenerOwner,
		IRemoteRenderingHandler {

	private AddPathwayListener addPathwayListener;

	private EventPublisher eventPublisher;

	/**
	 * Constructor.
	 */
	public RcpGLPathwayView() {
		super();

		try {
			viewContext = JAXBContext.newInstance(SerializedPathwayView.class);
		} catch (JAXBException ex) {
			throw new RuntimeException("Could not create JAXBContext", ex);
		}

		eventPublisher = GeneralManager.get().getEventPublisher();
		registerEventListeners();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		createGLCanvas();

		view = new GLPathway(glCanvas, parentComposite, serializedView.getViewFrustum());
		initializeView();
		createPartControlGL();
	}

	@Override
	public void createDefaultSerializedView() {

		serializedView = new SerializedPathwayView();
		determineDataConfiguration(serializedView);
	}

	@Override
	public String getViewGUIID() {
		return GLPathway.VIEW_TYPE;
	}

	@Override
	public void queueEvent(final AEventListener<? extends IListenerOwner> listener,
			final AEvent event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				listener.handleEvent(event);
			}
		});
	}

	@Override
	public void registerEventListeners() {
		addPathwayListener = new AddPathwayListener();
		addPathwayListener.setHandler(this);
		eventPublisher.addListener(LoadPathwayEvent.class, addPathwayListener);
	}

	@Override
	public void unregisterEventListeners() {
		if (addPathwayListener != null) {
			eventPublisher.removeListener(addPathwayListener);
			addPathwayListener = null;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		unregisterEventListeners();
	}

	@Override
	public void addPathwayView(int pathwayID, String dataDomainID) {
		((GLPathway) view).setPathway(pathwayID);

		PathwayGraph pathway = PathwayManager.get().getItem(pathwayID);
		minSizeComposite.setMinSize(pathway.getWidth(), pathway.getHeight());
	}

	@Override
	public void setConnectionLinesEnabled(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGeneMappingEnabled(boolean geneMappingEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNeighborhoodEnabled(boolean neighborhoodEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPathwayTexturesEnabled(boolean pathwayTexturesEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleNavigationMode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleZoom() {
		// TODO Auto-generated method stub

	}
}