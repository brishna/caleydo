package org.caleydo.core.util.mapping.color;

import java.util.ArrayList;
import java.util.EnumMap;

import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.IListenerOwner;
import org.caleydo.core.manager.event.view.histogram.UpdateColorMappingEvent;
import org.caleydo.core.manager.event.view.storagebased.RedrawViewEvent;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.listener.IColorMappingHandler;
import org.caleydo.core.view.opengl.canvas.listener.UpdateColorMappingListener;

/**
 * Manage color mappings for different situations system-wide. There can only be one color mapping for each of
 * the values specified in {@link EColorMappingType}. The color mapping can be initialized, otherwise a
 * default is provided. The class follows the Singleton pattern.
 * 
 * @author Alexander Lex
 */

public class ColorMappingManager
	implements IColorMappingHandler {

	private static ColorMappingManager colorMappingManager = null;
	private EnumMap<EColorMappingType, ColorMapping> hashColorMapping = null;

	private IEventPublisher eventPublisher;
	private UpdateColorMappingListener updateColorMappingListener;

	/**
	 * Constructor, only called internally
	 */
	private ColorMappingManager() {
		eventPublisher = GeneralManager.get().getEventPublisher();
		registerEventListeners();
		hashColorMapping = new EnumMap<EColorMappingType, ColorMapping>(EColorMappingType.class);
	}

	/**
	 * Get the instance of the colorMappingManager
	 * 
	 * @return the manager
	 */
	public static ColorMappingManager get() {
		if (colorMappingManager == null) {
			colorMappingManager = new ColorMappingManager();
		}
		return colorMappingManager;
	}

	private void registerEventListeners() {

		updateColorMappingListener = new UpdateColorMappingListener();
		updateColorMappingListener.setHandler(this);
		eventPublisher.addListener(UpdateColorMappingEvent.class, updateColorMappingListener);

	}

	/**
	 * TODO from where should this method be called? are managers released anywhere?
	 */
	@SuppressWarnings("unused")
	private void unregisterEventListeners() {

		if (updateColorMappingListener != null) {
			eventPublisher.removeListener(updateColorMappingListener);
			updateColorMappingListener = null;
		}

	}

	/**
	 * Set a color mapping for a mapping type. If there is already a mapping present it is replaced.
	 * 
	 * @param colorMappingType
	 * @param alMarkerPoints
	 *            a list of marker points based on which the color mapping is created
	 */
	public void initColorMapping(EColorMappingType colorMappingType,
		ArrayList<ColorMarkerPoint> alMarkerPoints) {
		if (hashColorMapping.containsKey(colorMappingType)) {
			hashColorMapping.get(colorMappingType).resetColorMapping(alMarkerPoints);
			return;
		}
		hashColorMapping.put(colorMappingType, new ColorMapping(colorMappingType, alMarkerPoints));
	}

	/**
	 * Initializes a gene expression color mapping from values stored in the preference store. Sets all
	 * display list to dirty to have immediate effect.
	 */
	public void initiFromPreferenceStore(EColorMappingType colorMappingType) {

		if (hashColorMapping.containsKey(colorMappingType)) {
			hashColorMapping.get(colorMappingType).initiFromPreferenceStore();
			return;
		}
		hashColorMapping.put(colorMappingType, new ColorMapping(colorMappingType));

		for (AGLView view : GeneralManager.get().getViewGLCanvasManager().getAllGLEventListeners()) {
			view.setDisplayListDirty();
		}

	}

	/**
	 * Returns the color mapping for a particular mapping type. Creates a default mapping if no custom mapping
	 * was set beforehand.
	 * 
	 * @param colorMappingType
	 *            the type
	 * @return the color mapping
	 */
	public ColorMapping getColorMapping(EColorMappingType colorMappingType) {
		ColorMapping colorMapping = hashColorMapping.get(colorMappingType);
		if (colorMapping == null) {
			colorMapping = getDefaultColorMapping(colorMappingType);
			hashColorMapping.put(colorMappingType, colorMapping);
		}
		return colorMapping;
	}

	/**
	 * Create default color mapping
	 * 
	 * @param colorMappingType
	 *            the type
	 * @return the color mapping
	 */
	private ColorMapping getDefaultColorMapping(EColorMappingType colorMappingType) {

		ColorMapping colorMapping;
		ArrayList<ColorMarkerPoint> alColorMarkerPoints = new ArrayList<ColorMarkerPoint>();
		switch (colorMappingType) {
			case GENE_EXPRESSION:

				alColorMarkerPoints.add(new ColorMarkerPoint(0, 0, 1, 0));
				alColorMarkerPoints.add(new ColorMarkerPoint(0.2f, 0, 0, 0));
				alColorMarkerPoints.add(new ColorMarkerPoint(1, 1, 0, 0));
				colorMapping = new ColorMapping(colorMappingType, alColorMarkerPoints);
				break;
			default:
				alColorMarkerPoints.add(new ColorMarkerPoint(0, 0, 1, 0));
				alColorMarkerPoints.add(new ColorMarkerPoint(1, 0, 0, 0));
				colorMapping = new ColorMapping(colorMappingType, alColorMarkerPoints);
		}

		return colorMapping;
	}

	/**
	 * Handles changes of {@link ColorMapping}s by storing the new received ColorMapping with the contained
	 * {@link EColorMappingType}.
	 * 
	 * @param colorMapping
	 *            changed {@link ColorMapping} to store
	 */
	public void distributeColorMapping(ColorMapping colorMapping) {
		hashColorMapping.put(colorMapping.getColorMappingType(), colorMapping);
		colorMapping.writeToPrefStore();

		RedrawViewEvent redrawViewEvent = new RedrawViewEvent();
		redrawViewEvent.setSender(this);
		eventPublisher.triggerEvent(redrawViewEvent);
	}

	/**
	 * @param colorMapping
	 */
	public void changeColorMapping(ColorMapping colorMapping) {
		distributeColorMapping(colorMapping);

		UpdateColorMappingEvent updateColorMappingEvent = new UpdateColorMappingEvent();
		updateColorMappingEvent.setSender(this);
		updateColorMappingEvent.setColorMapping(colorMapping);
		eventPublisher.triggerEvent(updateColorMappingEvent);
	}

	@Override
	public void queueEvent(AEventListener<? extends IListenerOwner> listener, AEvent event) {
		listener.handleEvent(event);
	}

}
