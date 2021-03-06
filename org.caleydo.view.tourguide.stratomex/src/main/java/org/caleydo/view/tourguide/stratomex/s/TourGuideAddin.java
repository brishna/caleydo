/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.tourguide.stratomex.s;

import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_ACCEPT;
import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_ACCEPT_DISABLED;
import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_ADD_DEPENDENT;
import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_ADD_INDEPENDENT;
import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_CANCEL;
import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_UNDO;
import static org.caleydo.view.tourguide.stratomex.StratomexRenderStyle.ICON_UNDO_DIASBLED;
import gleem.linalg.Vec3f;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.DataDomainOracle;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.listener.RemoveTablePerspectiveEvent;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.ElementLayouts;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.data.PathwayTablePerspective;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.stratomex.EEmbeddingID;
import org.caleydo.view.stratomex.EPickingType;
import org.caleydo.view.stratomex.GLStratomex;
import org.caleydo.view.stratomex.addin.IStratomeXAddIn;
import org.caleydo.view.stratomex.brick.GLBrick;
import org.caleydo.view.stratomex.brick.configurer.ClinicalDataConfigurer;
import org.caleydo.view.stratomex.brick.configurer.IBrickConfigurer;
import org.caleydo.view.stratomex.brick.configurer.PathwayDataConfigurer;
import org.caleydo.view.stratomex.brick.sorting.NoSortingSortingStrategy;
import org.caleydo.view.stratomex.column.BlockAdapter;
import org.caleydo.view.stratomex.column.BrickColumn;
import org.caleydo.view.stratomex.column.BrickColumnManager;
import org.caleydo.view.stratomex.column.FrameHighlightRenderer;
import org.caleydo.view.tourguide.stratomex.event.AddNewColumnEvent;
import org.caleydo.view.tourguide.stratomex.event.HighlightBrickEvent;
import org.caleydo.view.tourguide.stratomex.event.UpdateNumericalPreviewEvent;
import org.caleydo.view.tourguide.stratomex.event.UpdatePathwayPreviewEvent;
import org.caleydo.view.tourguide.stratomex.event.UpdateStratificationPreviewEvent;
import org.caleydo.view.tourguide.stratomex.event.WizardActionsEvent;
import org.caleydo.view.tourguide.stratomex.wizard.WizardElementLayout;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jogamp.opengl.util.texture.Texture;

/**
 * @author Samuel Gratzl
 *
 */
public class TourGuideAddin implements IStratomeXAddIn {
	private static final Logger log = Logger.create(TourGuideAddin.class);
	private static final Color COLOR_SELECTED = SelectionType.SELECTION.getColor();
	private static final Color COLOR_POSSIBLE_SELECTION = Color.NEUTRAL_GREY;

	private static final String ADD_PICKING_TYPE = "templateAdd";
	private static final String ADD_DEPENDENT_PICKING_TYPE = "templateDependentAdd";
	private static final String ADD_INDEPENDENT_PICKING_TYPE = "templateInDependentAdd";
	private static final String CONFIRM_PICKING_TYPE = "templateConfirm";
	private static final String CANCEL_PICKING_TYPE = "templateAbort";
	private static final String BACK_PICKING_TYPE = "templateBack";

	private GLStratomex stratomex;

	private AddWizardElement wizard;

	private int previewIndex; // where
	// what either an element or a brick
	private EWizardMode wizardMode;
	private ElementLayout wizardElement;
	// wrapper element of the wizard element for layouting
	private WizardElementLayout wizardElementWrapper;
	private List<BrickColumn> wizardPreviews = new ArrayList<>();

	/**
	 * the current selection and related information
	 */
	private ESelectionMode selectionMode = null;
	private GLBrick selectionCurrent = null;

	private String hoveredButton = "";

	/**
	 * events that has to be triggered one frame later
	 */
	private final List<AEvent> delayedEvents = new ArrayList<>();

	@Override
	public void stampTo(GLStratomex stratomeX) {
		this.stratomex = stratomeX;
	}

	@Override
	public void postDisplay() {
		for (AEvent event : delayedEvents)
			EventPublisher.trigger(event);
		delayedEvents.clear();
	}

	@Override
	public void renderOptionTrigger(GL2 gl, float x, float y, float w, float h, int id) {
		if (isWizardActive()) // not more than one at the same time
			return;
		renderButton(gl, x, y, w, h, 32, stratomex, ADD_PICKING_TYPE, id, ICON_ADD_INDEPENDENT);
	}

	public boolean isWizardActive() {
		return wizardElement != null || !wizardPreviews.isEmpty();
	}

	public void renderStartButton(GL2 gl, float x, float y, float w, float h, int id) {
		if (isWizardActive()) // not more than one at the same time
			return;
		renderButton(gl, x, y, w, h, 32, stratomex, ADD_PICKING_TYPE, id, ICON_ADD_INDEPENDENT);
	}

	public void renderAddDependentButton(GL2 gl, float x, float y, float w, float h, int id) {
		if (isWizardActive()) // not more than one at the same time
			return;
		renderButton(gl, x, y, w, h, 24, stratomex, ADD_DEPENDENT_PICKING_TYPE, id, ICON_ADD_DEPENDENT);
	}

	public void renderAddInDependentButton(GL2 gl, float x, float y, float w, float h, int id) {
		if (isWizardActive()) // not more than one at the same time
			return;
		renderButton(gl, x, y, w, h, 24, stratomex, ADD_INDEPENDENT_PICKING_TYPE, id, ICON_ADD_INDEPENDENT);
	}

	public void renderConfirmButton(GL2 gl, float x, float y, float w, float h) {
		boolean disabled = wizardPreviews.isEmpty(); // no preview no accept
		renderButton(gl, x, y, w, h, 32, stratomex, CONFIRM_PICKING_TYPE, 1, disabled ? ICON_ACCEPT_DISABLED
				: ICON_ACCEPT);
	}

	public void renderCancelButton(GL2 gl, float x, float y, float w, float h) {
		renderButton(gl, x, y, w, h, 32, stratomex, CANCEL_PICKING_TYPE, 1, ICON_CANCEL);
	}

	public void renderBackButton(GL2 gl, float x, float y, float w, float h) {
		boolean disabled = wizard == null || !wizard.canGoBack(); // check can go back
		renderButton(gl, x, y, w, h, 32, stratomex, BACK_PICKING_TYPE, 1, disabled ? ICON_UNDO_DIASBLED : ICON_UNDO);
	}

	private void renderButton(GL2 gl, float x, float y, float w, float h, int diameter, AGLView view,
			String pickingType, int id, URL texture) {
		GLGraphics.checkError(gl);

		boolean isHovered = Objects.equals(hoveredButton, pickingType + (id + 1));
		id = view.getPickingManager().getPickingID(view.getID(), pickingType, id + 1);
		// stratomex.addIDPickingTooltipListener("Add another column", pickingType, pickedObjectID)
		gl.glPushName(id);

		final float wi = view.getPixelGLConverter().getGLWidthForPixelWidth(diameter);
		final float hi = view.getPixelGLConverter().getGLHeightForPixelHeight(diameter);
		final float xi = x + w * 0.5f - wi * 0.5f;
		final float yi = y + h * 0.5f - hi * 0.5f;
		final float z = 1.5f;

		Vec3f lowerLeftCorner = new Vec3f(xi, yi, z);
		Vec3f lowerRightCorner = new Vec3f(xi + wi, yi, z);
		Vec3f upperRightCorner = new Vec3f(xi + wi, yi + hi, z);
		Vec3f upperLeftCorner = new Vec3f(xi, yi + hi, z);

		Color col = isHovered ? new Color(0.8f, 0.8f, 0.8f) : new Color(1f, 1f, 1f);
		gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
		TextureManager t = view.getTextureManager();
		Texture tex = t.get(texture);
		tex.enable(gl);
		// gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_MODULATE);
		t.renderTexture(gl, tex, lowerLeftCorner, lowerRightCorner, upperRightCorner, upperLeftCorner, col);
		gl.glPopAttrib();
		gl.glPopName();
		GLGraphics.checkError(gl);
	}

	@Override
	public void registerPickingListeners() {
		final Object receiver = TourGuideAddin.this;

		stratomex.addTypePickingTooltipListener("Add another column at this position", ADD_PICKING_TYPE);
		stratomex.addTypePickingListener(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				switch (pick.getPickingMode()) {
				case CLICKED:
					log.debug("add new column");
					EventPublisher.trigger(new AddNewColumnEvent(pick.getObjectID() - 1).to(receiver).from(this));
					break;
				case MOUSE_OVER:
					hoveredButton = ADD_PICKING_TYPE + pick.getObjectID();
					break;
				case MOUSE_OUT:
					hoveredButton = null;
					break;
				default:
					break;
				}
			}
		}, ADD_PICKING_TYPE);

		stratomex.addTypePickingTooltipListener("Add column based on this column's stratification",
				ADD_DEPENDENT_PICKING_TYPE);
		stratomex.addTypePickingListener(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				switch (pick.getPickingMode()) {
				case CLICKED:
					log.debug("add new dependent column");
					EventPublisher.trigger(new AddNewColumnEvent(pick.getObjectID() - 1, EWizardMode.DEPENDENT).to(
							receiver).from(this));
					break;
				case MOUSE_OVER:
					hoveredButton = ADD_DEPENDENT_PICKING_TYPE + pick.getObjectID();
					break;
				case MOUSE_OUT:
					hoveredButton = null;
					break;
				default:
					break;
				}
			}
		}, ADD_DEPENDENT_PICKING_TYPE);
		stratomex.addTypePickingTooltipListener("Add stratification column based on this column",
				ADD_INDEPENDENT_PICKING_TYPE);
		stratomex.addTypePickingListener(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				switch (pick.getPickingMode()) {
				case CLICKED:
					log.debug("add new independent column");
					EventPublisher.trigger(new AddNewColumnEvent(pick.getObjectID() - 1, EWizardMode.INDEPENDENT).to(
							receiver).from(this));
					break;
				case MOUSE_OVER:
					hoveredButton = ADD_DEPENDENT_PICKING_TYPE + pick.getObjectID();
					break;
				case MOUSE_OUT:
					hoveredButton = null;
					break;
				default:
					break;
				}
			}
		}, ADD_INDEPENDENT_PICKING_TYPE);

		class ActionPickingListener extends APickingListener {
			private final String pickingType;

			ActionPickingListener(String pickingType) {
				this.pickingType = pickingType;
			}

			@Override
			protected void clicked(Pick pick) {
				log.debug("click on wizardaction: " + pickingType);
				EventPublisher.trigger(new WizardActionsEvent(pickingType).to(receiver).from(this));
			}

			@Override
			protected void mouseOut(Pick pick) {
				hoveredButton = null;
			}

			@Override
			protected void mouseOver(Pick pick) {
				hoveredButton = pickingType + pick.getObjectID();
			}
		}

		stratomex.addTypePickingTooltipListener("Confirm the current previewed element", CONFIRM_PICKING_TYPE);
		stratomex.addTypePickingListener(new ActionPickingListener(CONFIRM_PICKING_TYPE), CONFIRM_PICKING_TYPE);

		stratomex.addTypePickingTooltipListener("Cancel temporary column", CANCEL_PICKING_TYPE);
		stratomex.addTypePickingListener(new ActionPickingListener(CANCEL_PICKING_TYPE), CANCEL_PICKING_TYPE);

		stratomex.addTypePickingTooltipListener("Go back", BACK_PICKING_TYPE);
		stratomex.addTypePickingListener(new ActionPickingListener(BACK_PICKING_TYPE), BACK_PICKING_TYPE);

		IPickingListener brickPicker = new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onBrickPick(pick);
			}
		};
		stratomex.addTypePickingListener(brickPicker, EPickingType.BRICK.name());
		stratomex.addTypePickingListener(brickPicker, EPickingType.BRICK_TITLE.name());

		stratomex.addTypePickingListener(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				if (wizard != null)
					wizard.onPick(pick);
			}
		}, AddWizardElement.PICKING_TYPE);
		stratomex.addTypePickingTooltipListener(new IPickingLabelProvider() {

			@Override
			public String getLabel(Pick pick) {
				if (wizard != null)
					return wizard.getLabel(pick);
				return null;
			}
		}, AddWizardElement.PICKING_TYPE);
	}

	/**
	 * if we pick an brick
	 *
	 * @param pick
	 */
	protected void onBrickPick(Pick pick) {
		// don't need to select
		if (pick.getPickingMode() != PickingMode.CLICKED || selectionMode == null || wizard == null)
			return;
		GLBrick brick = findBick(pick.getObjectID());
		if (brick == null)
			return;
		boolean isHeader = brick.isHeaderBrick();
		if (!isHeader && (selectionMode == ESelectionMode.STRATIFICATION))
			return;
		if (this.selectionCurrent == brick)
			return;
		if (this.wizardPreviews.contains(brick.getBrickColumn())) // can't select temporarly
			return;
		selectBrick(brick);
	}

	private void selectBrick(GLBrick brick) {
		boolean handled = false;

		// Replace sampled dimension perspective with full one
		TablePerspective tablePerspective = brick.getBrickColumn().getTablePerspective();
		ATableBasedDataDomain dataDomain = tablePerspective.getDataDomain();
		Perspective dimension = dataDomain.getTable().getDefaultDimensionPerspective(false);
		// if it does not exist, it will be created
		tablePerspective = dataDomain.getTablePerspective(tablePerspective.getRecordPerspective().getPerspectiveID(),
				dimension.getPerspectiveID());

		switch (selectionMode) {
		case STRATIFICATION:
			handled = wizard.onSelected(tablePerspective);
			break;
		case GROUP:
			handled = wizard.onSelected(tablePerspective, brick.getTablePerspective().getRecordGroup());
			break;
		}
		if (handled) {
			if (this.selectionCurrent != null) {
				changeHighlight(this.selectionCurrent, COLOR_POSSIBLE_SELECTION);
			}
			changeHighlight(brick, COLOR_SELECTED);
			this.selectionCurrent = brick;

			stratomex.setDisplayListDirty();
		}
	}

	private void changeHighlight(GLBrick brick, Color color) {
		if (brick == null)
			return;
		if (brick.isHeaderBrick()) {
			brick.getBrickColumn().setHighlightColor(color == null ? BrickColumn.REVERT_COLOR : color);
		} else {
			ElementLayout layout = brick.getLayout();
			if (color == null)
				layout.clearBackgroundRenderers(FrameHighlightRenderer.class, GLContext.getCurrentGL().getGL2());
			else {
				// select brick by changing highlight
				for (FrameHighlightRenderer glow : Iterables.filter(layout.getBackgroundRenderer(),
						FrameHighlightRenderer.class)) {
					glow.setColor(color);
					return;
				}
				// no yet there add one
				layout.addBackgroundRenderer(new FrameHighlightRenderer(color, true));
			}
		}
	}

	public void selectStratification(Predicate<TablePerspective> filter, boolean autoSelectLeftOfMe) {
		this.selectionMode = ESelectionMode.STRATIFICATION;
		// highlight all possibles
		int index = 0;
		GLBrick toSelect = null;
		for (BrickColumn col : stratomex.getBrickColumnManager().getBrickColumns()) {
			if (filter.apply(col.getTablePerspective())) {
				changeHighlight(col.getHeaderBrick(), COLOR_POSSIBLE_SELECTION);

				if (autoSelectLeftOfMe && previewIndex == index) {
					toSelect = col.getHeaderBrick();
				}
			}
			index++;
		}

		if (toSelect != null)
			selectBrick(toSelect);

		repaint();
	}

	public void selectGroup(Predicate<Pair<TablePerspective, Group>> filter, boolean allowSelectAll) {
		this.selectionMode = ESelectionMode.GROUP;
		// highlight all possibles
		for (BrickColumn col : stratomex.getBrickColumnManager().getBrickColumns()) {
			TablePerspective tablePerspective = col.getTablePerspective();
			if (allowSelectAll)
				changeHighlight(col.getHeaderBrick(), COLOR_POSSIBLE_SELECTION);
			for (GLBrick brick : col.getSegmentBricks()) {
				if (filter.apply(Pair.make(tablePerspective, brick.getTablePerspective().getRecordGroup())))
					changeHighlight(brick, COLOR_POSSIBLE_SELECTION);
			}
		}
		repaint();
	}

	@ListenTo(sendToMe = true)
	private void onHighlight(HighlightBrickEvent event) {
		BrickColumnManager manager = stratomex.getBrickColumnManager();
		BrickColumn brickColumn = manager.getBrickColumn(event.getStratification());
		if (brickColumn == null)
			return;

		Color c = event.isHighlight() ? event.getColor() : null;
		if (event.getGroup() == null) {
			changeHighlight(brickColumn.getHeaderBrick(), c);
		} else {
			Group g = event.getGroup();
			for (GLBrick brick : brickColumn.getSegmentBricks()) {
				if (g.equals(brick.getTablePerspective().getRecordGroup())) {
					changeHighlight(brick, c);
					break;
				}
			}
		}
	}

	private void repaint() {
		stratomex.updateLayout();
		stratomex.setDisplayListDirty();
	}

	/**
	 * @param brickId
	 * @return
	 */
	private GLBrick findBick(int brickId) {
		for (BrickColumn col : stratomex.getBrickColumnManager().getBrickColumns()) {
			if (col.getHeaderBrick().getID() == brickId)
				return col.getHeaderBrick();
			for (GLBrick brick : col.getSegmentBricks()) {
				if (brick.getID() == brickId)
					return brick;
			}
		}
		return null;
	}

	/**
	 * @param index
	 * @param independentOne
	 * @return
	 */
	private ElementLayout createTemplateElement(TablePerspective source, boolean independentOne) {
		createWizard(source, independentOne);
		ElementLayout l = ElementLayouts.wrap(wizard, 120);
		l.addBackgroundRenderer(new TemplateHighlightRenderer());
		l.addBackgroundRenderer(new WizardActionsLayoutRenderer(stratomex, this));

		return l;
	}

	private WizardElementLayout layout(ElementLayout body) {
		return new WizardElementLayout(body);
	}

	private void createWizard(TablePerspective source, boolean independentOne) {
		if (source == null) {
			wizard = AddWizardElementFactory.create(this, stratomex);
			wizardMode = EWizardMode.GLOBAL;
		} else if (independentOne) {
			wizard = AddWizardElementFactory.createIndepenent(this, stratomex, source);
			wizardMode = EWizardMode.INDEPENDENT;
		} else {
			wizard = AddWizardElementFactory.createDependent(this, stratomex, source);
			wizardMode = EWizardMode.DEPENDENT;
		}
		stratomex.registerEventListener(wizard);
		wizard.prepare();
	}

	@ListenTo(sendToMe = true)
	private void onAddEmptyColumn(AddNewColumnEvent event) {
		if (!wizardPreviews.isEmpty() || wizardElement != null) // only one at one time
			return;

		int index = 0;
		TablePerspective source = null;
		BrickColumnManager brickColumnManager = stratomex.getBrickColumnManager();
		switch (event.getMode()) {
		case DEPENDENT:
			for (BrickColumn col : brickColumnManager.getBrickColumns()) {
				if (col.getID() == event.getObjectId()) {
					source = col.getTablePerspective();
					break;
				}
				index++;
			}
			if (source == null)
				return;
			break;
		case INDEPENDENT:
			for (BrickColumn col : brickColumnManager.getBrickColumns()) {
				if (col.getID() == event.getObjectId()) {
					source = col.getTablePerspective();
					index -= 1; // left of
					break;
				}
				index++;
			}
			if (source == null)
				return;
			break;
		default:
			if (event.getObjectId() <= 0) {
				// left or first
				index = -1;
			} else {
				// right of
				BrickColumn col = brickColumnManager.getBrickColumnSpacers().get(event.getObjectId()).getLeftDimGroup();
				index = col == null ? -1 : brickColumnManager.getBrickColumns().indexOf(col);
			}
		}

		previewIndex = index;
		wizardElement = createTemplateElement(source, event.getMode() == EWizardMode.INDEPENDENT);
		wizardElementWrapper = layout(wizardElement);
		stratomex.relayout();
	}

	@ListenTo(sendToMe = true)
	private void onWizardAction(WizardActionsEvent event) {
		switch (event.getPickingType()) {
		case CONFIRM_PICKING_TYPE:
			if (wizardPreviews.isEmpty())
				return;

			// remove the preview buttons
			if (!wizardPreviews.isEmpty()) {
				BrickColumn wizardPreview = wizardPreviews.get(0);
				final Row layout = wizardPreview.getLayout();
				layout.clearForegroundRenderers(AddAttachedLayoutRenderer.class, GLContext.getCurrentGL().getGL2());
				layout.clearForegroundRenderers(WizardActionsLayoutRenderer.class, GLContext.getCurrentGL().getGL2());
				if (canHaveDependentColumns(wizardPreview))
					layout.addForeGroundRenderer(new AddAttachedLayoutRenderer(wizardPreview, this, false));
				if (canHaveIndependentColumns(wizardPreview))
					layout.addForeGroundRenderer(new AddAttachedLayoutRenderer(wizardPreview, this, true));
			}
			// reset
			done(true);
			stratomex.relayout();
			break;
		case CANCEL_PICKING_TYPE:
			if (wizardMode == EWizardMode.INDEPENDENT && !wizardPreviews.isEmpty()) {
				// restore the old unstratified dependend one
				updateDependentBrickColumn(null, wizardPreviews.get(0));
			}
			for (BrickColumn col : wizardPreviews) {
				stratomex.removeTablePerspective(col.getTablePerspective());
			}
			// reset
			done(false);
			stratomex.relayout();
			break;
		case BACK_PICKING_TYPE:
			if (wizard != null)
				wizard.goBack();
			break;
		}

		repaint();
	}

	/**
	 * listens to remove events done via the remove button and check if this was our template
	 *
	 * @param event
	 */
	@ListenTo
	private void onRemoveTablePerspective(RemoveTablePerspectiveEvent event) {
		if (event.getReceiver() != stratomex)
			return;
		// removed my template
		for (BrickColumn wizardPreview : wizardPreviews) {
			if (wizardPreview.getTablePerspective() == event.getTablePerspective())
				done(false);
		}
	}

	/**
	 * @param brickColumn
	 */
	@Override
	public void addedBrickColumn(BrickColumn brickColumn) {
		if (wizardPreviews.contains(brickColumn))
			return;

		Row layout = brickColumn.getLayout();
		if (canHaveDependentColumns(brickColumn))
			layout.addForeGroundRenderer(new AddAttachedLayoutRenderer(brickColumn, this, false));

		if (canHaveIndependentColumns(brickColumn))
			layout.addForeGroundRenderer(new AddAttachedLayoutRenderer(brickColumn, this, true));
	}

	/**
	 * determines whether a given BrickColumn can have dependent ones
	 *
	 * @param brickColumn
	 * @return
	 */
	private static boolean canHaveDependentColumns(BrickColumn brickColumn) {
		IBrickConfigurer b = brickColumn.getBrickConfigurer();
		if (b instanceof PathwayDataConfigurer) {
			return false;
		}
		if (b instanceof ClinicalDataConfigurer) {
			return false;
		}
		return true;
	}

	private static boolean canHaveIndependentColumns(BrickColumn brickColumn) {
		IBrickConfigurer b = brickColumn.getBrickConfigurer();
		if (b instanceof PathwayDataConfigurer) {
			return false; // TODO
		}
		if (b instanceof ClinicalDataConfigurer) {
			return b.getBrickSortingStrategy() instanceof NoSortingSortingStrategy;
		}
		return false;
	}

	/**
 *
 */
	private void done(boolean confirmed) {
		selectionMode = null;
		selectionCurrent = null;
		// clear highlights
		for (BrickColumn col : stratomex.getBrickColumnManager().getBrickColumns()) {
			col.setHighlightColor(BrickColumn.REVERT_COLOR);
			for (GLBrick brick : col.getSegmentBricks()) {
				brick.getLayout().clearBackgroundRenderers(FrameHighlightRenderer.class,
						GLContext.getCurrentGL().getGL2());
			}
		}

		// cleanup template
		if (wizardElement != null)
			cleanupWizardElement();

		wizardPreviews.clear();

		if (wizard != null) {
			wizard.done(confirmed);
			wizard.destroy(GLContext.getCurrent().getGL().getGL2());
			wizard = null;
		}
	}

	private void cleanupWizardElement() {
		wizardElement.setRenderer(null);
		wizardElementWrapper.destroy(GLContext.getCurrent().getGL().getGL2());
		wizardElement = null;
		wizardElementWrapper = null;
	}

	@ListenTo(sendToMe = true)
	private void onUpdatePreview(UpdateStratificationPreviewEvent event) {
		boolean wasOnTheFly = wizard == null;
		if (wizard == null) { // no wizard there to handle add a template column on the fly
			initIntermediateWizard();
			wizard = AddWizardElementFactory.createForStratification(this, stratomex);
			stratomex.registerEventListener(wizard);
			wizard.prepare();
		}
		wizard.onUpdate(event);
		if (wasOnTheFly && !wizardPreviews.isEmpty())
			onWizardAction(new WizardActionsEvent(CONFIRM_PICKING_TYPE));
	}

	private TablePerspective initIntermediateWizard() {
		final BrickColumnManager bcm = stratomex.getBrickColumnManager();
		BrickColumn selected = bcm.getActiveBrickColumn();
		TablePerspective selectedTP = selected == null ? null : selected.getTablePerspective();
		wizardMode = EWizardMode.GLOBAL;
		previewIndex = selected != null ? bcm.indexOfBrickColumn(selected) : bcm.getRightColumnStartIndex() - 1;
		return selectedTP;
	}

	@ListenTo(sendToMe = true)
	private void onUpdatePreview(UpdatePathwayPreviewEvent event) {
		boolean wasOnTheFly = wizard == null;
		if (wizard == null) { // no wizard there to handle add a template column on the fly
			initIntermediateWizard();
			wizard = AddWizardElementFactory.createForPathway(this, stratomex);
			stratomex.registerEventListener(wizard);
			wizard.prepare();
		}
		wizard.onUpdate(event);
		if (wasOnTheFly && !wizardPreviews.isEmpty())
			onWizardAction(new WizardActionsEvent(CONFIRM_PICKING_TYPE));

	}

	@ListenTo(sendToMe = true)
	private void onUpdateNumerical(UpdateNumericalPreviewEvent event) {
		boolean wasOnTheFly = wizard == null;
		if (wizard == null) { // no wizard there to handle add a template column on the fly
			initIntermediateWizard();
			wizard = AddWizardElementFactory.createForOther(this, stratomex);
			stratomex.registerEventListener(wizard);
			wizard.prepare();
		}
		wizard.onUpdate(event);
		if (wasOnTheFly && !wizardPreviews.isEmpty())
			onWizardAction(new WizardActionsEvent(CONFIRM_PICKING_TYPE));
	}

	/**
	 * @param columns
	 */
	@Override
	public void addColumns(List<BlockAdapter> columns) {
		if (wizardElement == null)
			return;
		if (previewIndex < 0)
			columns.add(0, new BlockAdapter(wizardElementWrapper));
		else {
			int index = previewIndex - stratomex.getBrickColumnManager().getCenterColumnStartIndex();
			index = Math.min(index + 1, columns.size());
			columns.add(index, new BlockAdapter(wizardElementWrapper));
		}
	}

	/**
	 * whether a template column exists or not
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return wizardElement == null;
	}

	@Override
	public boolean canShowDetailBrick() {
		return isEmpty() && (wizard == null);
	}

	public void replaceTemplate(TablePerspective with, IBrickConfigurer config, boolean extra, Color highlight) {
		List<Pair<Integer, BrickColumn>> added = null;
		final List<TablePerspective> withL = Collections.singletonList(with);

		if (highlight != null)
			delayedEvents.add(new HighlightBrickEvent(with, null, highlight).to(this));

		if (wizardElement != null) {
			assert !extra;
			BrickColumnManager bcm = stratomex.getBrickColumnManager();
			BrickColumn left = previewIndex < 0 ? null : bcm.getBrickColumns().get(
					bcm.getCenterColumnStartIndex() + previewIndex);
			added = stratomex.addTablePerspectives(withL, config, left, false);
			if (added.size() > 0) {
				cleanupWizardElement();
				if (wizardMode == EWizardMode.INDEPENDENT) {
					updateDependentBrickColumn(with, added.get(0).getSecond());
				}
				wizardPreviews.add(added.get(0).getSecond());
			} else {
				wizardPreviews.clear();
			}
		} else if (!wizardPreviews.isEmpty()) {
			if (extra) {
				if (wizardPreviews.size() == 1) { // add extra
					added = stratomex.addTablePerspectives(withL, config, wizardPreviews.get(0), true);
					if (added.size() > 0) {
						wizardPreviews.add(added.get(0).getSecond());
					}
				} else { // update extra
					BrickColumn extraPreview = wizardPreviews.get(0);
					added = stratomex.addTablePerspectives(withL, config, extraPreview, true);
					stratomex.removeTablePerspective(wizardPreviews.get(1).getTablePerspective());
					if (added.size() > 0) {
						wizardPreviews.set(1, added.get(0).getSecond());
					} else {
						wizardPreviews.remove(1);
					}
				}
			} else {
				BrickColumn wizardPreview = wizardPreviews.get(0);
				if (wizardPreview.getTablePerspective().equals(with)) // replace with itself
					return;
				added = stratomex.addTablePerspectives(withL, config, wizardPreview, true);
				stratomex.removeTablePerspective(wizardPreview.getTablePerspective());
				if (added.size() > 0) {
					if (wizardMode == EWizardMode.INDEPENDENT) {
						updateDependentBrickColumn(with, added.get(0).getSecond());
					}
					wizardPreviews.set(0, added.get(0).getSecond());
				} else {
					wizardPreviews.remove(0);
				}
			}

		} else if (stratomex.isDetailMode()) {
			return;
		} else {
			// create a preview on the fly
			added = stratomex.addTablePerspectives(withL, config, null, true);
			if (added.size() > 0) {
				wizardPreviews.add(added.get(0).getSecond());
			}
		}
		if (added != null && !added.isEmpty()) {
			wizardPreviews.get(0).getLayout()
					.clearForegroundRenderers(ALayoutRenderer.class, GLContext.getCurrentGL().getGL2());
			wizardPreviews.get(0).getLayout().addForeGroundRenderer(new WizardActionsLayoutRenderer(stratomex, this));
		}
	}

	/**
	 * updates the dependent brick column if an independent brick column is for it selected
	 *
	 * @param with
	 */
	private void updateDependentBrickColumn(TablePerspective with, BrickColumn new_) {
		BrickColumnManager bcm = stratomex.getBrickColumnManager();
		int index = bcm.indexOfBrickColumn(new_) + 1;
		if (index <= 0 || index >= bcm.getBrickColumns().size())
			return;
		BrickColumn toUpdate = bcm.getBrickColumns().get(index);
		TablePerspective from = toUpdate.getTablePerspective();

		IBrickConfigurer brickConfigurer = toUpdate.getBrickConfigurer();
		if (brickConfigurer instanceof ClinicalDataConfigurer) {
			ClinicalDataConfigurer configurer;
			TablePerspective to;
			if (with == null) { // restore original one
				configurer = new ClinicalDataConfigurer();
				configurer.setSortingStrategy(new NoSortingSortingStrategy());
				to = from.getDataDomain().getTablePerspective(
						from.getDataDomain().getTable().getDefaultRecordPerspective(false).getPerspectiveID(),
						from.getDimensionPerspective().getPerspectiveID());
			} else {
				to = asPerspective(with.getRecordPerspective(), from);
				configurer = ClinicalDataConfigurer.create(stratomex, with, to);
			}
			List<Pair<Integer, BrickColumn>> pairs = stratomex.addTablePerspectives(Lists.newArrayList(to), configurer,
					new_, true);
			if (with == null && canHaveIndependentColumns(pairs.get(0).getSecond())) { // readd add indepenent buttons
				pairs.get(0).getSecond().getLayout()
						.addForeGroundRenderer(new AddAttachedLayoutRenderer(pairs.get(0).getSecond(), this, true));
			}
			stratomex.removeTablePerspective(from);
		} else if (brickConfigurer instanceof PathwayDataConfigurer) {
			// TODO
			// TablePerspective t = asPerspective(with, pathway);
		}

	}

	public void replaceTemplate(ALayoutRenderer renderer) {
		if (wizardElement != null) {
			wizardElement.setRenderer(renderer);
			renderer.setLimits(wizardElement.getSizeScaledX(), wizardElement.getSizeScaledY()); // don't know why the
																								// element layout does
																								// it not by it own
		} else if (!wizardPreviews.isEmpty()) {
			ElementLayout new_ = ElementLayouts.wrap(renderer, 120);
			BrickColumn wizardPreview = wizardPreviews.get(0);
			previewIndex = stratomex.getBrickColumnManager().indexOfBrickColumn(wizardPreview) - 1;
			for (BrickColumn preview : wizardPreviews)
				stratomex.removeTablePerspective(preview.getTablePerspective());
			wizardPreviews.clear();
			wizardElement = new_;
			wizardElementWrapper = layout(wizardElement);
			new_.addBackgroundRenderer(new TemplateHighlightRenderer());
			new_.addForeGroundRenderer(new WizardActionsLayoutRenderer(stratomex, this));
		} else {
			ElementLayout new_ = ElementLayouts.wrap(renderer, 120);
			wizardElement = new_;
			wizardElementWrapper = layout(wizardElement);
			new_.addBackgroundRenderer(new TemplateHighlightRenderer());
			new_.addForeGroundRenderer(new WizardActionsLayoutRenderer(stratomex, this));
			stratomex.relayout();
		}
	}

	public void replaceOtherTemplate(Perspective underlying, TablePerspective numerical, boolean extra,
			Color highlight) {
		TablePerspective t = asPerspective(underlying, numerical);
		TablePerspective underlyingTP = findTablePerspective(underlying);
		if (underlyingTP == null)
			return;

		ClinicalDataConfigurer configurer = ClinicalDataConfigurer.create(stratomex, underlyingTP, t);
		replaceTemplate(t, configurer, extra, highlight);
	}

	public void replacePathwayTemplate(Perspective underlying, PathwayGraph pathway, boolean extra, Color highlight) {
		if (underlying == null) {
			replaceTemplate(new PrimitivePathwayRenderer(pathway, stratomex));
		} else {
			TablePerspective t = asPerspective(underlying, pathway);
			replaceTemplate(t, new PathwayDataConfigurer(), extra, highlight);
		}
	}

	private TablePerspective findTablePerspective(Perspective record) {
		for (TablePerspective p : stratomex.getTablePerspectives())
			if (p.getRecordPerspective() == record)
				return p;
		return null;
	}

	private static TablePerspective asPerspective(Perspective underlying, TablePerspective clinicalVariable) {
		Perspective dim = clinicalVariable.getDimensionPerspective();
		ATableBasedDataDomain dataDomain = (ATableBasedDataDomain) dim.getDataDomain();

		Perspective rec = null;

		for (String id : dataDomain.getRecordPerspectiveIDs()) {
			Perspective r = dataDomain.getTable().getRecordPerspective(id);
			if (r.getDataDomain().equals(underlying.getDataDomain())
					&& r.isLabelDefault() == underlying.isLabelDefault() && r.getLabel().equals(underlying.getLabel())) {
				rec = r;
				break;
			}
		}
		if (rec == null) { // not found create a new one
			rec = dataDomain.convertForeignPerspective(underlying);
			dataDomain.getTable().registerRecordPerspective(rec);
		}
		return dataDomain.getTablePerspective(rec.getPerspectiveID(), dim.getPerspectiveID(), false);
	}

	protected static TablePerspective asPerspective(Perspective record, PathwayGraph pathway) {
		PathwayDataDomain pathwayDataDomain = (PathwayDataDomain) DataDomainManager.get().getDataDomainByType(
				PathwayDataDomain.DATA_DOMAIN_TYPE);

		ATableBasedDataDomain dataDomain = (ATableBasedDataDomain) record.getDataDomain();
		Perspective dimension = dataDomain.getTable().getDefaultDimensionPerspective(false);
		for (PathwayTablePerspective p : pathwayDataDomain.getTablePerspectives()) {
			if (p.getPathway().equals(pathway) && p.getRecordPerspective().equals(record)
					&& p.getDimensionPerspective().equals(dimension))
				return p;
		}
		// not found create new one
		PathwayTablePerspective pathwayDimensionGroup = new PathwayTablePerspective(dataDomain, pathwayDataDomain,
				record, dimension, pathway);

		// pathwayDimensionGroup.setPrivate(true);
		pathwayDataDomain.addTablePerspective(pathwayDimensionGroup);

		return pathwayDimensionGroup;
	}

	public List<TablePerspective> getVisibleTablePerspectives() {
		return stratomex.getTablePerspectives();
	}

	public ALayoutRenderer createPreviewRenderer(PathwayGraph pathway) {
		return new PrimitivePathwayRenderer(pathway, stratomex);
	}

	public ALayoutRenderer createPreviewRenderer(TablePerspective tablePerspective) {
		// create a preview similar to the header
		EEmbeddingID embeddingID = selectEmbeddingID(tablePerspective);

		Set<String> remoteRenderedViewIDs = ViewManager.get().getRemotePlugInViewIDs(GLStratomex.VIEW_TYPE,
				embeddingID.id());

		MultiFormRenderer multiFormRenderer = new MultiFormRenderer(stratomex, true);
		List<TablePerspective> tablePerspectives = Lists.newArrayList(tablePerspective);

		String brickEventSpace = GeneralManager.get().getEventPublisher().createUniqueEventSpace();
		for (String viewID : remoteRenderedViewIDs) {
			multiFormRenderer.addPluginVisualization(viewID, GLStratomex.VIEW_TYPE, embeddingID.id(),
					tablePerspectives, brickEventSpace);
		}
		return multiFormRenderer;
	}

	private static EEmbeddingID selectEmbeddingID(TablePerspective tablePerspective) {
		EEmbeddingID embeddingID;
		if (tablePerspective instanceof PathwayTablePerspective)
			embeddingID = EEmbeddingID.PATHWAY_HEADER_BRICK;
		else if (DataDomainOracle.isClinical(tablePerspective.getDataDomain()) && hasIntegers(tablePerspective))
			embeddingID = EEmbeddingID.CLINICAL_HEADER_BRICK;
		else if (DataDomainOracle.isCategoricalDataDomain(tablePerspective.getDataDomain()))
			embeddingID = EEmbeddingID.CATEGORICAL_HEADER_BRICK;
		else
			embeddingID = EEmbeddingID.NUMERICAL_HEADER_BRICK;
		return embeddingID;
	}

	/**
	 * @param tablePerspective
	 * @return
	 */
	private static boolean hasIntegers(TablePerspective tablePerspective) {
		Table table = tablePerspective.getDataDomain().getTable();
		VirtualArray dva = tablePerspective.getDimensionPerspective().getVirtualArray();
		VirtualArray rva = tablePerspective.getRecordPerspective().getVirtualArray();
		if (dva.size() == 0 || rva.size() == 0)
			return false;
		return table.getRawDataType(dva.get(0), rva.get(0)) != EDataType.STRING;
	}

	@Override
	public Collection<? extends String> addEmptyStrings() {
		return Arrays.asList("To add a column showing a dataset", " click the \"+\" button at the top",
				"or use the LineUp or Data-View Integrator view", "",
				"Refer to http://help.caleydo.org for more information.");
	}

}
