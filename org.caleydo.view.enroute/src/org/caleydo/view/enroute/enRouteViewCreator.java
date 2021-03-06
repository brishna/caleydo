/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.enroute;

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.IRemoteViewCreator;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.view.enroute.path.PathSizeConfiguration;

public class enRouteViewCreator implements IRemoteViewCreator {

	public enRouteViewCreator() {
	}

	@Override
	public AGLView createRemoteView(AGLView remoteRenderingView, List<TablePerspective> tablePerspectives,
			String embeddingEventSpace) {
		GLEnRoutePathway enRoute = (GLEnRoutePathway) GeneralManager
				.get()
				.getViewManager()
				.createGLView(GLEnRoutePathway.class, remoteRenderingView.getParentGLCanvas(),
						new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, 0, 1, 0, 1, -1, 1));

		enRoute.setRemoteRenderingGLView((IGLRemoteRenderingView) remoteRenderingView);
		enRoute.addTablePerspectives(tablePerspectives);
		enRoute.setPathwayPathEventSpace(embeddingEventSpace);
		enRoute.initialize();
		enRoute.getPathRenderer().setSizeConfig(PathSizeConfiguration.ENROUTE_COMPACT);

		return enRoute;
	}

}
