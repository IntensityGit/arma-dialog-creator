package com.armadialogcreator.gui.fxcontrol.treeView;


import com.armadialogcreator.ExceptionHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class EditableTreeCellFactory<Tv, Td extends TreeItemData> extends TreeCell<Td> {
	/** How long it takes for the current hovered tree item that is expandable for it to expand and show its children. Value is in milliseconds. */
	private static final long WAIT_DURATION_TREE_VIEW_FOLDER = 500;
	private static final Color COLOR_TREE_VIEW_DRAG = Color.ORANGE;

	private static final String HOVER_TREE_CELL = "hover-tree-cell";

	private final TreeCellSelectionUpdate treeCellSelectionUpdate;
	private final EditableTreeView<Tv, Td> treeView;

	private TextField textField;

	private long waitStartTime = 0;

	/** TreeItem's being dragged */
	// must be static since the factory is created for each cell since dragging takes place
	// over more than once cell
	private static final List<TreeItem<? extends TreeItemData>> dragging = new ArrayList<>();
	private static TreeCell<?> hoveredChildParent; //static for the same reasons as dragging
	private Effect cellEffect;

	private enum MouseTreeCellLocation {
		TOP, MIDDLE, BOTTOM, UNKNOWN
	}

	private static MouseTreeCellLocation location = MouseTreeCellLocation.UNKNOWN;

	EditableTreeCellFactory(@NotNull EditableTreeView<Tv, Td> treeView, @Nullable TreeCellSelectionUpdate treeCellSelectionUpdate) {
		this.treeCellSelectionUpdate = treeCellSelectionUpdate;
		this.treeView = treeView;
		this.setEditable(true);
		// first method called when the user clicks and drags a tree item
		TreeCell<Td> myTreeCell = this;

		//		addDragListeners(treeView, myTreeCell);
	}

	private void addDragListeners(@NotNull EditableTreeView<Tv, Td> treeView, TreeCell<Td> myTreeCell) {
		this.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				try {
					doHandle(event);
				} catch (Exception e) {
					ExceptionHandler.getInstance().uncaughtException(e);
				}
			}

			private void doHandle(MouseEvent event) {
				if (getTreeItem() == null) {
					event.consume();
					return;
				}
				// disables right clicking to start drag and drop
				if (event.isSecondaryButtonDown()) {
					event.consume();
					return;
				}

				EditableTreeView view = (EditableTreeView) getTreeView();

				Dragboard dragboard = view.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content;
				dragging.add(getTreeItem());
				content = new ClipboardContent();
				content.putString("");// don't remove. drag and drop doesn't work if this doesn't happen for a strange reason
				dragboard.setContent(content);

				event.consume();
			}
		});
		this.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				try {
					doHandle(event);
				} catch (Exception e) {
					ExceptionHandler.getInstance().uncaughtException(e);
				}
			}

			private void doHandle(DragEvent event) {
				// not dragging on the tree
				if (getTreeItem() == null || getTreeItem().getValue() == null || getTreeView().getSelectionModel().isEmpty()) {
					return;
				}
				// trying to drag into itself
				if (getTreeItem().getValue().equals(getTreeView().getSelectionModel().getSelectedItem().getValue())) {
					return;
				}

				Iterator<TreeItem<? extends TreeItemData>> iter = EditableTreeCellFactory.dragging.iterator();
				while (iter.hasNext()) {
					TreeItem<? extends TreeItemData> dragging = iter.next();
					// if tree item can have children, we need to make sure it doesn't drag itself into its children
					if (dragging.getValue().canHaveChildren()) {
						if (TreeUtil.hasDescendant(dragging, getTreeItem())) {
							iter.remove();
							continue;
						}
					}
					final Point2D sceneCoordinates = EditableTreeCellFactory.this.localToScene(0d, 0d);
					final double cellHeight = EditableTreeCellFactory.this.getHeight();
					final double margin = cellHeight * 0.3;
					final double effectHeight = 2.0;
					final double innerShadowEffectRadius = 1.0;

					// get the y coordinate within the control
					final double y = event.getSceneY() - (sceneCoordinates.getY());

					final boolean canHaveChildren = getTreeItem().getValue().canHaveChildren();
					//near the center
					if (y > margin && y < cellHeight - margin && canHaveChildren) {
						// auto expands the hovered tree item if it has children after a period of time.
						// timer is reset when the mouse moves away from the current hovered item
						if (!getTreeItem().isExpanded()) {
							if (waitStartTime == 0) {
								waitStartTime = System.currentTimeMillis();
							} else {
								long now = System.currentTimeMillis();
								// wait a while before the tree item is expanded
								if (waitStartTime + WAIT_DURATION_TREE_VIEW_FOLDER <= now) {
									getTreeItem().setExpanded(true);
									waitStartTime = 0;
								}
							}
						}
						hoveredChildParent = myTreeCell;
						cellEffect = null;
						if (!hoveredChildParent.getStyleClass().contains(HOVER_TREE_CELL)) { //don't add multiple times
							hoveredChildParent.getStyleClass().add(HOVER_TREE_CELL);
						}
						location = MouseTreeCellLocation.MIDDLE;
					} else if (!canHaveChildren || y <= margin) { //near the top of cell
						if (location != MouseTreeCellLocation.TOP) {
							cellEffect = new InnerShadow(innerShadowEffectRadius, 0, effectHeight, COLOR_TREE_VIEW_DRAG);
						}
						location = MouseTreeCellLocation.TOP;
					} else if (y >= cellHeight - margin) {//near bottom of cell
						if (location != MouseTreeCellLocation.BOTTOM) {
							cellEffect = new InnerShadow(innerShadowEffectRadius, 0, -effectHeight, COLOR_TREE_VIEW_DRAG);
						}
						location = MouseTreeCellLocation.BOTTOM;
					} else {
						cellEffect = null;
						location = MouseTreeCellLocation.UNKNOWN;
					}
					if (location != MouseTreeCellLocation.MIDDLE) {
						clearChildParentStyle();
					}
					setEffect(cellEffect);
					// when it reaches this point, the tree item is okay to move
					event.acceptTransferModes(TransferMode.MOVE);
				}

			}

		});
		this.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				try {
					doHandle(event);
				} catch (Exception e) {
					ExceptionHandler.getInstance().uncaughtException(e);
				}
			}

			private void doHandle(DragEvent event) {
				event.acceptTransferModes(TransferMode.MOVE);
				for (TreeItem<? extends TreeItemData> draggingItem : EditableTreeCellFactory.dragging) {
					TreeItem<Td> dragging = (TreeItem<Td>) draggingItem;
					switch (location) {
						case TOP: //intentional fall through
						case MIDDLE: {
							if (location == MouseTreeCellLocation.MIDDLE && getTreeItem().getValue().canHaveChildren()) {
								treeView.moveTreeItem(dragging, getTreeItem(), getTreeItem().getChildren().size());
							} else {
								int index = getTreeItem().getParent().getChildren().indexOf(getTreeItem());
								treeView.moveTreeItem(dragging, getTreeItem().getParent(), index);
							}
							break;
						}
						case BOTTOM: {
							int index = getTreeItem().getParent().getChildren().indexOf(getTreeItem());
							treeView.moveTreeItem(dragging, getTreeItem().getParent(), index + 1);
							break;
						}
						case UNKNOWN: {
							break;
						}
					}
				}
				clearChildParentStyle();
				EditableTreeCellFactory.dragging.clear();
				getTreeView().getSelectionModel().clearSelection();
			}

		});
		this.setOnDragExited(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				try {
					doHandle();
				} catch (Exception e) {
					ExceptionHandler.getInstance().uncaughtException(e);
				}
			}

			private void doHandle() {
				if (getTreeItem() == null) {
					// this happens when something is still being dragged but it isn't over the tree
					return;
				}

				waitStartTime = 0;
				setEffect(null);
				setBorder(null);
			}

		});
	}

	private static void clearChildParentStyle() {
		if (hoveredChildParent != null) {
			hoveredChildParent.getStyleClass().remove(HOVER_TREE_CELL);
			hoveredChildParent = null;
		}
	}

	EditableTreeCellFactory<Tv, Td> getNewInstance() {
		return new EditableTreeCellFactory<>(this.treeView, this.treeCellSelectionUpdate);
	}

	@Override
	public void startEdit() {
		if (getTreeItem() == null) {
			return;
		}
		super.startEdit();
		if (textField == null) {
			createTextField();
		}
		setText(null);
		setGraphic(textField);
		textField.requestFocus();
		textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getTreeItem().getValue().getText());
		setGraphic(getTreeItem().getValue().getGraphic());
		textField = null;
	}

	@Override
	protected void updateItem(Td node, boolean empty) {
		super.updateItem(node, empty);
		// this adds a textfield to the tree item to get a new name
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				if (textField != null) {
					setText(textField.getText());
					getTreeItem().getValue().setText(textField.getText());
					textField = null;
				} else {
					setText(getItem().getText());
				}
				setGraphic(getTreeItem().getValue().getGraphic());
			}
		}
		if (node != null) {
			node.setText(getText());

			node.getStyleClass().addListener((ListChangeListener<? super String>) c -> {
				while (c.next()) {
					if (c.wasAdded()) {
						for (int i = c.getFrom(); i < c.getTo(); i++) {
							getStyleClass().add(c.getList().get(i));
						}
					} else if (c.wasRemoved()) {
						getStyleClass().removeAll(c.getRemoved());
					}
				}
			});
			node.getTextObserver().addListener((observable, oldValue, newValue) -> {
				setText(newValue);
			});
		}
	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					commitEdit(getItem());
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
				}
			}
		});
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean focused) {
				if (!focused) {
					cancelEdit();
				}
			}
		});

	}

	@Override
	public void updateSelected(boolean selected) {
		super.updateSelected(selected);
		if (getTreeItem() == null) {
			if (treeCellSelectionUpdate != null) {
				treeCellSelectionUpdate.selectionUpdate();
			}
			return;
		}
		if (treeCellSelectionUpdate != null) {
			treeCellSelectionUpdate.selectionUpdate();
		}
	}

	private String getString() {
		return getItem() == null ? "" : getItem().toString();
	}

}
