/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.stratomex.brick.layout;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.layout.util.ColorRenderer;
import org.caleydo.core.view.opengl.layout.util.Zoomer;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.button.Button;
import org.caleydo.core.view.opengl.util.button.ButtonRenderer;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.stratomex.EPickingType;
import org.caleydo.view.stratomex.GLStratomex;
import org.caleydo.view.stratomex.brick.GLBrick;
import org.caleydo.view.stratomex.brick.configurer.IBrickConfigurer;
import org.caleydo.view.stratomex.brick.ui.HandleRenderer;
import org.caleydo.view.stratomex.brick.ui.RelationIndicatorRenderer;
import org.caleydo.view.stratomex.column.BrickColumn;

/**
 * Layout for the detailed inspection of a brick. Contains a tool bar, a view and a footer bar.
 *
 * @author Partl
 *
 */
public class DetailBrickLayoutTemplate extends ABrickLayoutConfiguration {

	protected static final int FOOTER_BAR_HEIGHT_PIXELS = 4;
	protected static final int TOOLBAR_HEIGHT_PIXELS = 16;
	protected static final int BUTTON_HEIGHT_PIXELS = 16;
	protected static final int BUTTON_WIDTH_PIXELS = 16;
	protected static final int RELATION_INDICATOR_WIDTH_PIXELS = 3;
	protected static final int HANDLE_SIZE_PIXELS = 8;

	protected static final int CLOSE_BUTTON_ID = 0;
	protected static final int LOCK_RESIZING_BUTTON_ID = 1;
	protected static final int VIEW_SWITCHING_MODE_BUTTON_ID = 5;

	protected static final int DEFAULT_HANDLES = HandleRenderer.ALL_RESIZE_HANDLES;

	@SuppressWarnings("hiding")
	protected static final int SPACING_PIXELS = 4;

	// protected ArrayList<BrickViewSwitchingButton> viewSwitchingButtons;
	protected List<ElementLayout> toolBarElements;
	protected List<ElementLayout> footerBarElements;
	protected Row toolBar;
	protected Row footerBar;

	// protected Button heatMapButton;
	// protected Button parCoordsButton;
	// protected Button histogramButton;
	// protected Button overviewHeatMapButton;
	protected Button viewSwitchingModeButton;
	protected Button lockResizingButton;

	protected boolean showFooterBar;

	protected RelationIndicatorRenderer leftRelationIndicatorRenderer;
	protected RelationIndicatorRenderer rightRelationIndicatorRenderer;

	public DetailBrickLayoutTemplate(GLBrick brick, BrickColumn brickColumn, GLStratomex stratomex) {
		super(brick, brickColumn, stratomex);
		toolBarElements = new ArrayList<ElementLayout>();
		footerBarElements = new ArrayList<ElementLayout>();
		// check if this is the header brick
		// if (!brick.isHeaderBrick()) {
		// leftRelationIndicatorRenderer = new RelationIndicatorRenderer(brick, stratomex, true);
		// rightRelationIndicatorRenderer = new RelationIndicatorRenderer(brick, stratomex, false);
		// }
		toolBar = new Row();
		footerBar = new Row();
		lockResizingButton = new Button(EPickingType.BRICK_LOCK_RESIZING_BUTTON.name(), LOCK_RESIZING_BUTTON_ID,
				EIconTextures.PIN);
		viewSwitchingModeButton = new Button(EPickingType.BRICK_VIEW_SWITCHING_MODE_BUTTON.name(),
				VIEW_SWITCHING_MODE_BUTTON_ID, EIconTextures.LOCK);
		viewSwitchingModeButton.setSelected(brickColumn.isGlobalViewSwitching());

		registerPickingListeners();
		// viewTypeChanged(getDefaultViewType());

	}

	@Override
	public void setLockResizing(boolean lockResizing) {
		lockResizingButton.setSelected(lockResizing);
	}

	// public void setLeftRelationIndicatorRenderer(RelationIndicatorRenderer leftRelationIndicatorRenderer) {
	// this.leftRelationIndicatorRenderer = leftRelationIndicatorRenderer;
	// }
	//
	// public void setRightRelationIndicatorRenderer(RelationIndicatorRenderer rightRelationIndicatorRenderer) {
	// this.rightRelationIndicatorRenderer = rightRelationIndicatorRenderer;
	// }

	@Override
	public void setStaticLayouts() {
		Row baseRow = new Row("baseRow");

		baseRow.setFrameColor(0, 0, 1, 0);
		baseElementLayout = baseRow;
		// if (!brick.isHeaderBrick()) {
		// leftRelationIndicatorRenderer.updateRelations();
		//
		// rightRelationIndicatorRenderer.updateRelations();
		//
		// ElementLayout leftRelationIndicatorLayout = new ElementLayout("RightRelationIndicatorLayout");
		// leftRelationIndicatorLayout.setPixelSizeX(RELATION_INDICATOR_WIDTH_PIXELS);
		// leftRelationIndicatorLayout.setRenderer(leftRelationIndicatorRenderer);
		// baseRow.append(leftRelationIndicatorLayout);
		// }
		Column baseColumn = new Column("baseColumn");
		baseColumn.setFrameColor(0, 1, 0, 0);

		baseRow.setRenderer(borderedAreaRenderer);

		if (handles == null) {
			handles = DEFAULT_HANDLES;
		}

		handleRenderer = new HandleRenderer(brick, HANDLE_SIZE_PIXELS, brick.getTextureManager(), handles);
		baseRow.addForeGroundRenderer(handleRenderer);

		ElementLayout spacingLayoutX = new ElementLayout("spacingLayoutX");
		spacingLayoutX.setPixelSizeX(SPACING_PIXELS);
		spacingLayoutX.setRatioSizeY(0);

		baseRow.append(spacingLayoutX);
		baseRow.append(baseColumn);
		baseRow.append(spacingLayoutX);

		if (viewLayout == null) {
			viewLayout = new ElementLayout("viewLayout");
			viewLayout.setFrameColor(1, 0, 0, 1);
			viewLayout.addBackgroundRenderer(new ColorRenderer(new float[] { 1, 1, 1, 1 }));
			Zoomer zoomer = new Zoomer(stratomex, viewLayout);
			viewLayout.setZoomer(zoomer);
		}
		viewLayout.setRenderer(viewRenderer);
		viewLayout.addForeGroundRenderer(innerBorderedAreaRenderer);
		innerBorderedAreaRenderer.setRenderSides(true);

		toolBar = createToolBar();
		footerBar = createFooterBar();

		ElementLayout spacingLayoutY = new ElementLayout("spacingLayoutY");
		spacingLayoutY.setPixelSizeY(SPACING_PIXELS);
		spacingLayoutY.setPixelSizeX(0);

		// baseColumn.appendElement(dimensionBarLayout);
		baseColumn.append(spacingLayoutY);
		if (showFooterBar) {
			baseColumn.append(footerBar);
			baseColumn.append(spacingLayoutY);
		}
		baseColumn.append(viewLayout);
		baseColumn.append(spacingLayoutY);
		baseColumn.append(toolBar);
		baseColumn.append(spacingLayoutY);
		if (!brick.isHeaderBrick()) {

			ElementLayout rightRelationIndicatorLayout = new ElementLayout("RightRelationIndicatorLayout");
			rightRelationIndicatorLayout.setPixelSizeX(RELATION_INDICATOR_WIDTH_PIXELS);
			rightRelationIndicatorLayout.setRenderer(rightRelationIndicatorRenderer);
			baseRow.append(rightRelationIndicatorLayout);
		}
	}

	/**
	 * Creates the toolbar containing buttons for view switching.
	 *
	 * @param pixelHeight
	 * @return
	 */
	protected Row createToolBar() {
		Row toolBar = new Row("ToolBarRow");
		toolBar.setPixelSizeY(TOOLBAR_HEIGHT_PIXELS);

		for (ElementLayout element : toolBarElements) {
			toolBar.append(element);
		}

		ElementLayout spacingLayoutX = new ElementLayout("spacingLayoutX");
		spacingLayoutX.setPixelSizeX(SPACING_PIXELS * 3);
		spacingLayoutX.setPixelSizeY(0);

		// for (int i = 0; i < viewSwitchingButtons.size(); i++) {
		// BrickViewSwitchingButton button = viewSwitchingButtons.get(i);
		// ElementLayout buttonLayout = new ElementLayout();
		// buttonLayout.setPixelGLConverter(pixelGLConverter);
		// buttonLayout.setPixelSizeX(pixelHeight);
		// buttonLayout.setPixelSizeY(pixelHeight);
		// buttonLayout.setRenderer(new ButtonRenderer(button, brick, brick
		// .getTextureManager()));
		// toolBar.append(buttonLayout);
		// if (i != viewSwitchingButtons.size() - 1) {
		// toolBar.append(spacingLayoutX);
		// }
		// }

		// ElementLayout ratioSpacingLayoutX = new ElementLayout(
		// "ratioSpacingLayoutX");
		// ratioSpacingLayoutX.setRatioSizeX(1);
		// ratioSpacingLayoutX.setRatioSizeY(0);

		// ElementLayout lockResizingButtonLayout = new ElementLayout("lockResizingButton");
		// lockResizingButtonLayout.setPixelSizeX(BUTTON_WIDTH_PIXELS);
		// lockResizingButtonLayout.setPixelSizeY(BUTTON_HEIGHT_PIXELS);
		// lockResizingButtonLayout.setRenderer(new ButtonRenderer.Builder(brick, lockResizingButton).build());

		ElementLayout toggleViewSwitchingButtonLayout = new ElementLayout("viewSwitchtingButtonLayout");
		toggleViewSwitchingButtonLayout.setPixelSizeX(BUTTON_WIDTH_PIXELS);
		toggleViewSwitchingButtonLayout.setPixelSizeY(BUTTON_HEIGHT_PIXELS);
		toggleViewSwitchingButtonLayout.setRenderer(new ButtonRenderer.Builder(brick, viewSwitchingModeButton).build());

		ElementLayout closeButtonLayout = new ElementLayout("closeButtonLayout");
		closeButtonLayout.setFrameColor(1, 0, 0, 1);
		closeButtonLayout.setPixelSizeX(BUTTON_WIDTH_PIXELS);
		closeButtonLayout.setPixelSizeY(BUTTON_HEIGHT_PIXELS);
		closeButtonLayout.setRenderer(new ButtonRenderer.Builder(brick, new Button(EPickingType.BRICK_CLOSE_BUTTON
				.name(), CLOSE_BUTTON_ID, EIconTextures.REMOVE)).build());

		// toolBar.append(ratioSpacingLayoutX);
		// toolBar.append(lockResizingButtonLayout);
		toolBar.append(spacingLayoutX);
		toolBar.append(toggleViewSwitchingButtonLayout);
		toolBar.append(spacingLayoutX);
		toolBar.append(closeButtonLayout);

		return toolBar;
	}

	protected Row createFooterBar() {
		Row footerBar = new Row("footerBar");
		footerBar.setPixelSizeY(FOOTER_BAR_HEIGHT_PIXELS);

		for (ElementLayout element : footerBarElements) {
			footerBar.append(element);
		}

		return footerBar;
	}

	@Override
	protected void registerPickingListeners() {

		// brick.addIDPickingListener(new APickingListener() {
		//
		// @Override
		// public void clicked(Pick pick) {
		// // boolean isResizingLocked = !lockResizingButton.isSelected();
		// // brick.setSizeFixed(isResizingLocked);
		// // lockResizingButton.setSelected(isResizingLocked);
		// }
		// }, EPickingType.BRICK_LOCK_RESIZING_BUTTON.name(), LOCK_RESIZING_BUTTON_ID);

		brick.addIDPickingListener(new APickingListener() {

			@Override
			public void clicked(Pick pick) {
				boolean isGlobalViewSwitching = !viewSwitchingModeButton.isSelected();
				brickColumn.setGlobalViewSwitching(isGlobalViewSwitching);
				viewSwitchingModeButton.setSelected(isGlobalViewSwitching);
			}
		}, EPickingType.BRICK_VIEW_SWITCHING_MODE_BUTTON.name(), VIEW_SWITCHING_MODE_BUTTON_ID);
		brick.addIDPickingTooltipListener("Toggle column-wide view switching",
				EPickingType.BRICK_VIEW_SWITCHING_MODE_BUTTON.name(), VIEW_SWITCHING_MODE_BUTTON_ID);

		brick.addIDPickingListener(new APickingListener() {

			@Override
			public void clicked(Pick pick) {
				brickColumn.hideDetailedBrick();
			}
		}, EPickingType.BRICK_CLOSE_BUTTON.name(), CLOSE_BUTTON_ID);
		brick.addIDPickingTooltipListener("Close detail", EPickingType.BRICK_CLOSE_BUTTON.name(), CLOSE_BUTTON_ID);
	}

	@Override
	public int getMinHeightPixels() {
		return 4 * SPACING_PIXELS + FOOTER_BAR_HEIGHT_PIXELS + TOOLBAR_HEIGHT_PIXELS
				+ viewRenderer.getMinHeightPixels();
		// return dimensionGroup.getDetailBrickHeightPixels();
	}

	@Override
	public int getMinWidthPixels() {
		int toolBarWidth = calcSumPixelWidth(toolBar);
		int footerBarWidth = showFooterBar ? calcSumPixelWidth(footerBar) : 0;

		int guiElementsWidth = Math.max(toolBarWidth, footerBarWidth) + 2 * SPACING_PIXELS;
		if (viewRenderer == null)
			return guiElementsWidth;
		return Math.max(guiElementsWidth, (2 * SPACING_PIXELS) + viewRenderer.getMinWidthPixels());
	}

	// @Override
	// public void viewTypeChanged(EContainedViewType viewType) {
	//
	// for (BrickViewSwitchingButton button : viewSwitchingButtons) {
	// if (viewType == button.getViewType()) {
	// button.setSelected(true);
	// } else {
	// button.setSelected(false);
	// }
	// }
	//
	// }

	@Override
	public void setGlobalViewSwitching(boolean isGlobalViewSwitching) {
		viewSwitchingModeButton.setSelected(isGlobalViewSwitching);
	}

	// public void setViewSwitchingButtons(
	// ArrayList<BrickViewSwitchingButton> buttons) {
	// viewSwitchingButtons = buttons;
	// }

	@Override
	public ABrickLayoutConfiguration getCollapsedLayoutTemplate() {
		return this;
	}

	@Override
	public ABrickLayoutConfiguration getExpandedLayoutTemplate() {
		return this;
	}

	@Override
	public int getDefaultHeightPixels() {
		return brickColumn.getDetailBrickHeightPixels();
	}

	@Override
	public int getDefaultWidthPixels() {
		return brickColumn.getDetailBrickWidthPixels(brickColumn.isLeftmost());
	}

	/**
	 * Sets the elements that should appear in the tool bar. The elements will placed from left to right using the order
	 * of the specified list.
	 *
	 * @param toolBarElements
	 */
	public void setToolBarElements(List<ElementLayout> toolBarElements) {
		this.toolBarElements = toolBarElements;
	}

	/**
	 * Sets the elements that should appear in the footer bar. The elements will placed from left to right using the
	 * order of the specified list.
	 *
	 * @param footerBarElements
	 */
	public void setFooterBarElements(List<ElementLayout> footerBarElements) {
		this.footerBarElements = footerBarElements;
	}

	/**
	 * Sets whether the footer bar shall be displayed.
	 *
	 * @param showFooterBar
	 */
	public void showFooterBar(boolean showFooterBar) {
		this.showFooterBar = showFooterBar;
	}

	@Override
	public void destroy() {
		super.destroy();
		brick.removeAllIDPickingListeners(EPickingType.BRICK_LOCK_RESIZING_BUTTON.name(), LOCK_RESIZING_BUTTON_ID);

		brick.removeAllIDPickingListeners(EPickingType.BRICK_VIEW_SWITCHING_MODE_BUTTON.name(),
				VIEW_SWITCHING_MODE_BUTTON_ID);

		brick.removeAllIDPickingListeners(EPickingType.BRICK_CLOSE_BUTTON.name(), CLOSE_BUTTON_ID);
	}

	@Override
	public void configure(IBrickConfigurer configurer) {
		configurer.configure(this);
	}

	@Override
	public ToolBar getToolBar() {
		return null;
	}
}
