package org.caleydo.view.bucket.listener;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.opengl.canvas.listener.ARemoteRenderingListener;

public class DisableConnectionLinesListener extends ARemoteRenderingListener {

	@Override
	public void handleEvent(AEvent event) {
		handler.setConnectionLinesEnabled(false);
	}

}
