package org.caleydo.view.bookmark;

import java.util.Iterator;

import javax.media.opengl.GL;

import org.caleydo.core.data.mapping.IDCategory;
import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.VABasedSelectionManager;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.data.BookmarkEvent;
import org.caleydo.core.manager.event.data.RemoveBookmarkEvent;
import org.caleydo.core.manager.event.view.SelectionCommandEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.util.collection.UniqueList;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.ContextMenu;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.container.BookmarkContextMenuItemContainer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.view.bookmark.GLBookmarkView.PickingIDManager;

/**
 * <p>
 * Base class for bookmark containers. A bookmark container is a container for
 * bookmarks belonging to a specific category, as specified in
 * {@link EIDCategory}. Therefore each container has to be uniquely associated
 * with a category.
 * </p>
 * <p>
 * Bookmark containers are no independent views, but depend on
 * {@link GLBookmarkView} for all the public interfaces to the rest of the
 * system.
 * </p>
 * <p>
 * The bookmark container holds a list of {@link ABookmark}s which are free to
 * some degree on how to display the information, for example by using text or
 * simple visualizations.
 * </p>
 * <p>
 * Every bookmark container holds ist own selection manager which it has to use
 * to manage the selections of its items. The selections are synchronized with
 * the rest of the system.
 * </p>
 * <p>
 * Bookmark containers do not allow duplicate entries - every element can be
 * present only once.
 * </p>
 * 
 * @author Alexander Lex
 */
abstract class ABookmarkContainer<SelectionManagerType extends VABasedSelectionManager<?, ?, ?, ?>> {

	/** The category of the container */
	IDCategory category;
	/** The type the container uses to internally store the data */
	IDType internalIDType;
	/** The dimensions (height, width, position, etc.) of the whole container */
	Dimensions dimensions;
	/** The name displayed as the heading in the sidebar */
	String categoryName;
	/**
	 * The list of bookmarks - each bookmark is unique, the ordering is relevant
	 */
	UniqueList<ABookmark> bookmarkItems;
	/** Reference to the text renderer created by {@link GLBookmarkView} */
	CaleydoTextRenderer textRenderer;
	/**
	 * Reference to the internal picking id manger created by
	 * {@link GLBookmarkView}
	 */
	PickingIDManager pickingIDManager;

	/**
	 * The selection manager, that manages whether a particular element is
	 * selected in the bookmark list. It is a member of the abstract base class,
	 * but has to be created by the implementing instance.
	 */
	SelectionManagerType selectionManager;

	/**
	 * The creating and managing instance of this class. We need access to it
	 * here, because it provides all the view-specific facilities such as
	 * context menu etc.
	 */
	GLBookmarkView manager;

	/**
	 * Constructor
	 * 
	 * @param manager
	 *            The gl view managing the container.
	 * @param category
	 *            Every category in {@link EIDCategory} can have one bookmark
	 *            container, therefore you need to specify the concrete
	 *            category. The category should be specified by the concrete
	 *            subclass and therefore not be part of its constructor.
	 * @param internalIDType
	 *            the id type the container uses to internally store the
	 *            bookmarks
	 */
	ABookmarkContainer(GLBookmarkView manager, IDCategory category, IDType internalIDType) {
		this.internalIDType = internalIDType;
		this.manager = manager;
		this.category = category;
		this.categoryName = category.getCategoryName();
		this.pickingIDManager = manager.getPickingIDManager();
		this.textRenderer = manager.getTextRenderer();
		dimensions = new Dimensions();
	}

	/**
	 * Returns the dimensions {@link GLBookmarkView} needs to place the
	 * containers
	 * 
	 * @return
	 */
	Dimensions getDimensions() {
		return dimensions;
	}

	/**
	 * Returns the category of the container
	 * 
	 * @return
	 */
	IDCategory getCategory() {
		return category;
	}

	/**
	 * Renders the heading for the category and the items.
	 * 
	 * @param gl
	 */
	void render(GL gl) {

		float yOrigin = dimensions.getYOrigin();
		dimensions.setHeight(0);

		dimensions.increaseHeight(BookmarkRenderStyle.CONTAINER_HEADING_SIZE);
		yOrigin -= BookmarkRenderStyle.CONTAINER_HEADING_SIZE;

		// render heading

		RenderingHelpers.renderText(gl, textRenderer, categoryName,
				dimensions.getXOrigin() + BookmarkRenderStyle.SIDE_SPACING, yOrigin,
				GeneralRenderStyle.SMALL_FONT_SCALING_FACTOR);

		for (ABookmark item : bookmarkItems) {

			item.getDimensions().setOrigins(BookmarkRenderStyle.SIDE_SPACING, yOrigin);
			item.getDimensions().setWidth(
					dimensions.getWidth() - 2 * BookmarkRenderStyle.SIDE_SPACING);
			yOrigin -= item.getDimensions().getHeight();

			float[] highlightColor = null;

			if (selectionManager.checkStatus(SelectionType.MOUSE_OVER, item.getID())) {
				highlightColor = SelectionType.MOUSE_OVER.getColor();
			} else if (selectionManager
					.checkStatus(SelectionType.SELECTION, item.getID())) {
				highlightColor = SelectionType.SELECTION.getColor();

			}
			int pickingID = pickingIDManager.getPickingID(this, item.getID());
			gl.glPushName(pickingID);

			item.render(gl);

			if (highlightColor != null) {

				float xOrigin = item.getDimensions().getXOrigin();
				float width = item.getDimensions().getWidth();
				float height = item.getDimensions().getHeight()
						- BookmarkRenderStyle.FRAME_SPACING;

				gl.glColor3fv(highlightColor, 0);
				gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex3f(xOrigin, yOrigin, 0);
				gl.glVertex3f(xOrigin + width, yOrigin, 0);
				gl.glVertex3f(xOrigin + width, yOrigin + height, 0);
				gl.glVertex3f(xOrigin, yOrigin + height, 0);
				gl.glEnd();
			}
			gl.glPopName();
			dimensions.increaseHeight(item.getDimensions().getHeight());
		}

		// GLHelperFunctions.drawPointAt(gl, 0, dimensions.getHeight(), 0);
	}

	/**
	 * Handles the picking events and triggers selection events
	 * 
	 * @param pickingMode
	 *            for example mouse-over or clicked
	 * @param iExternalID
	 *            the id specified when calling
	 *            {@link PickingIDManager#getPickingID(ABookmarkContainer, int)}
	 *            Internal to the specific BookmarkContainer
	 */
	void handleEvents(EPickingType ePickingType, EPickingMode pickingMode,
			Integer iExternalID, Pick pick) {
		SelectionType selectionType;
		switch (ePickingType) {

		case BOOKMARK_ELEMENT:

			switch (pickingMode) {
			case CLICKED:
				selectionType = SelectionType.SELECTION;
				break;
			case MOUSE_OVER:
				selectionType = SelectionType.MOUSE_OVER;
				break;
			case RIGHT_CLICKED:
				selectionType = SelectionType.SELECTION;

				BookmarkContextMenuItemContainer bookmarkContextMenuItemContainer = new BookmarkContextMenuItemContainer();
				bookmarkContextMenuItemContainer.setID(internalIDType, iExternalID);
				ContextMenu contextMenu = manager.getContextMenu();
				contextMenu.addItemContanier(bookmarkContextMenuItemContainer);

				if (manager.isRenderedRemote()) {
					contextMenu.setLocation(pick.getPickedPoint(), manager
							.getParentGLCanvas().getWidth(), manager.getParentGLCanvas()
							.getHeight());
					contextMenu.setMasterGLView(manager);
				}
				break;

			default:
				return;
			}
			selectionManager.clearSelection(selectionType);
			selectionManager.addToType(selectionType, iExternalID);

			SelectionCommand command = new SelectionCommand(ESelectionCommandType.CLEAR,
					selectionType);
			SelectionCommandEvent commandEvent = new SelectionCommandEvent();
			commandEvent.setSender(this);
			commandEvent.setIDCategory(category);
			commandEvent.setSelectionCommand(command);
			GeneralManager.get().getEventPublisher().triggerEvent(commandEvent);

			ISelectionDelta selectionDelta = selectionManager.getDelta();
			SelectionUpdateEvent event = new SelectionUpdateEvent();
			event.setSender(this);
			event.setSelectionDelta((SelectionDelta) selectionDelta);
			GeneralManager.get().getEventPublisher().triggerEvent(event);
			break;

		case BOOKMARK_CONTAINER_HEADING:

			break;
		}

	}

	/**
	 * Handles new bookmarks. Uses the information in the event.
	 * 
	 * @param <IDDataType>
	 *            The data type of the id, typically Integer or String
	 * @param event
	 *            The event containing the information about the new bookmark to
	 *            be added.
	 */
	abstract <IDDataType> void handleNewBookmarkEvent(BookmarkEvent<IDDataType> event);

	/**
	 * Handles the removal of bookmarks by using the informatinon in the event.
	 * 
	 * @param <IDDataType>
	 *            The data type of the id, typically Integer or String
	 * @param event
	 *            The event containing the information about the bookmark to be
	 *            removed.
	 */
	<IDDataType> void handleRemoveBookmarkEvent(RemoveBookmarkEvent<IDDataType> event) {
		Integer id = null;
		for (IDDataType tempID : event.getBookmarks()) {
			if (tempID instanceof Integer) {
				id = (Integer) tempID;
			} else
				throw new IllegalStateException("Can not handle strings for experiments");

			Iterator<ABookmark> iterator = bookmarkItems.iterator();

			while (iterator.hasNext()) {
				if (iterator.next().getID() == id) {
					iterator.remove();
					selectionManager.remove(id);
				}
			}

		}
	}

	/**
	 * Handles updates to the selections coming from external sources
	 * 
	 * @param selectionDelta
	 *            the information about the updates
	 */
	void handleSelectionUpdate(ISelectionDelta selectionDelta) {
		selectionManager.setDelta(selectionDelta);
	}

	/**
	 * Handles updates of the selection manager triggered by external sources.
	 * 
	 * @param selectionCommand
	 *            the information what to do with the selection manager
	 */
	void handleSelectionCommand(SelectionCommand selectionCommand) {
		selectionManager.executeSelectionCommand(selectionCommand);
	}
}
