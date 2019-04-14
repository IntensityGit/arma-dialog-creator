package com.armadialogcreator.gui.main.fxControls;

import com.armadialogcreator.ArmaDialogCreator;
import com.armadialogcreator.gui.StageDialog;
import com.armadialogcreator.gui.fxcontrol.SearchTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 Used for displaying items separated by categories and allowing the user to choose which item they want.

 @author Kayler
 @since 11/13/2016. */
public class ChooseItemDialog<V> extends StageDialog<VBox> {

	private final List<ItemCategoryTab<V>> itemCategoryTabs;
	private final TabPane tabPane = new TabPane();
	private V selectedItem;
	private String searchText;
	private final Label lblHeaderTitle;

	public ChooseItemDialog(@NotNull ItemCategory<V>[] categories, @NotNull Iterable<V> allItems, @Nullable String dialogTitle,
							@Nullable String headerTitle) {
		super(ArmaDialogCreator.getPrimaryStage(), new VBox(5), dialogTitle, true, true, false);
		myRootElement.setMinWidth(720d);
		myStage.setResizable(false);

		lblHeaderTitle = new Label(headerTitle);
		itemCategoryTabs = new ArrayList<>(categories.length);
		for (ItemCategory<V> category : categories) {
			itemCategoryTabs.add(new ItemCategoryTab<>(category, allItems));
		}

		initRootElement();
	}

	protected void setHeaderTitle(@NotNull String headerTitle) {
		lblHeaderTitle.setText(headerTitle);
	}

	private void initRootElement() {
		myRootElement.setPadding(new Insets(10));

		lblHeaderTitle.setFont(Font.font(15d));

		SearchTextField searchField = initializeSearchBox();
		myRootElement.getChildren().add(new BorderPane(null, null, searchField, null, lblHeaderTitle));
		myRootElement.getChildren().add(new Separator(Orientation.HORIZONTAL));

		final ChangeListener<? super V> selectedItemListener = new ChangeListener<V>() {
			@Override
			public void changed(ObservableValue<? extends V> observable, V oldValue, V newValue) {
				ChooseItemDialog.this.btnOk.setDisable(newValue == null);
				ChooseItemDialog.this.selectedItem = newValue;
				getSelectedTab().getCategory().newItemSelected(selectedItem);
			}
		};
		for (ItemCategoryTab<V> tab : itemCategoryTabs) {
			tabPane.getTabs().add(tab);
			tab.getListView().getSelectionModel().selectedItemProperty().addListener(selectedItemListener);
		}

		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab selected) {
				newCategorySelected(getSelectedTab());
			}
		});


		//force update selected tab
		tabPane.getSelectionModel().selectLast();
		tabPane.getSelectionModel().selectFirst();

		getSelectedTab().getListView().getSelectionModel().selectFirst();

		myRootElement.getChildren().add(tabPane);

		myStage.sizeToScene();
	}

	@NotNull
	private SearchTextField initializeSearchBox() {
		SearchTextField tfSearch = new SearchTextField();
		tfSearch.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				getSelectedTab().limitToSearch(newValue);
				searchText = newValue;
			}
		});

		return tfSearch;
	}

	/** A new category tab was selected. Default implementation is nothing. */
	protected void newCategorySelected(@NotNull ItemCategoryTab<V> selected) {
		selected.limitToSearch(searchText);
	}

	@SuppressWarnings("unchecked")
	protected ItemCategoryTab<V> getSelectedTab() {
		return (ItemCategoryTab<V>) tabPane.getSelectionModel().getSelectedItem();
	}

	@Override
	protected void cancel() {
		selectedItem = null;
		super.cancel();
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {
		selectedItem = null;
		super.onCloseRequest(event);
	}

	/** Return the item chosen. If null, no item was chosen. */
	@Nullable
	public V getChosenItem() {
		return selectedItem;
	}

	protected static class ItemCategoryTab<V> extends Tab {
		private final ItemCategory<V> category;
		private final ListView<V> listView;
		private final LinkedList<V> removed = new LinkedList<>();
		private final Comparator<V> comparator = new ListViewComparator<>();

		public ItemCategoryTab(@NotNull ItemCategory<V> category, @NotNull Iterable<V> allItemsFromMasterCategory) {
			super(category.categoryDisplayName());
			this.category = category;
			List<V> allItemsInCategory = new LinkedList<>();
			for (V v : allItemsFromMasterCategory) {
				if (category.itemInCategory(v)) {
					allItemsInCategory.add(v);
				}
			}

			listView = new ListView<>();

			final Label lblListView = new Label(category.availableItemsDisplayText(), listView);
			lblListView.setContentDisplay(ContentDisplay.BOTTOM);

			final HBox root = new HBox(10, lblListView);
			root.setPadding(new Insets(5));
			final Node categoryNode = category.getMiscCategoryNode();
			if (categoryNode != null) {
				root.getChildren().add(categoryNode);
				HBox.setHgrow(categoryNode, Priority.ALWAYS);
			}
			setContent(root);
			setClosable(false);

			listView.setPlaceholder(new Label(category.noItemsPlaceholderText()));
			listView.setMinWidth(250d);
			for (V v : allItemsInCategory) {
				listView.getItems().add(v);
			}

		}

		@NotNull
		public ListView<V> getListView() {
			return listView;
		}

		@NotNull
		public ItemCategory<V> getCategory() {
			return category;
		}

		@NotNull
		public List<V> getAllItemsInCategory() {
			return listView.getItems();
		}

		public void limitToSearch(@Nullable String searchWord) {
			searchWord = searchWord != null ? searchWord.toUpperCase() : "";
			if (searchWord.length() == 0) {
				while (removed.size() > 0) {
					getListView().getItems().add(removed.removeFirst());
				}
				return;
			}

			for (int i = 0; i < getListView().getItems().size(); ) {
				V v = getListView().getItems().get(i);
				if (!v.toString().contains(searchWord)) {
					removed.add(v);
					getListView().getItems().remove(i);
					continue;
				}
				i++;
			}
			getListView().getItems().sort(comparator);
		}

	}

	public interface ItemCategory<V> {
		/** Returns a String that is presentable to the user that is the name of the category */
		@NotNull
		String categoryDisplayName();

		/** Returns a String that is presentable to the user that is used when no items are in the given category */
		@NotNull
		String noItemsPlaceholderText();

		/** Returns a String that is presentable to the user that is placed above the {@link ListView} that holds all items for this category */
		@NotNull
		String availableItemsDisplayText();

		/** Return true if the given item is inside this category, false otherwise */
		boolean itemInCategory(@NotNull V item);

		/** Get a {@link Node} instance to be placed inside a {@link Tab}'s content node alongside the {@link ListView} */
		@Nullable
		Node getMiscCategoryNode();

		/** Invoked when an item is selected. This may be used to update the {@link #getMiscCategoryNode()} */
		void newItemSelected(@Nullable V item);
	}

	private static class ListViewComparator<V> implements Comparator<V> {

		@Override
		public int compare(V o1, V o2) {
			if (o1 == null) {
				return 1;
			}
			if (o2 == null) {
				return -1;
			}
			return o1.toString().compareTo(o2.toString());
		}
	}
}
