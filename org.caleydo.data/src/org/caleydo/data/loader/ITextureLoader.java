/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.data.loader;

import com.jogamp.opengl.util.texture.Texture;

/**
 * @see ResourceLoader
 * @author Samuel Gratzl
 * 
 */
public interface ITextureLoader {
	Texture getTexture(String fileName);
}
