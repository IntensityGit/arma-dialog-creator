package com.kaylerrenslow.armaDialogCreator.gui.fx.control;

import com.kaylerrenslow.armaDialogCreator.main.Lang;
import com.kaylerrenslow.armaDialogCreator.util.ReadOnlyValueObserver;
import com.kaylerrenslow.armaDialogCreator.util.ValueObserver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 Created by Kayler on 10/19/2016.
 */
public class ComboBoxMenuButton<V> extends StackPane {

	private final MenuButton menuButton = new MenuButton();
	private final ValueObserver<V> selectedItemObserver = new ValueObserver<>(null);
	private final String placeholderText;
	private final Node placeholderGraphic;
	private final boolean allowClear;
	private boolean separatorAdded = false;

	@SafeVarargs
	public ComboBoxMenuButton(String placeholderText, Node placeholderGraphic, CBMBMenuItem<V>... items) {
		this(true, placeholderText, placeholderGraphic, items);
	}

	@SafeVarargs
	public ComboBoxMenuButton(boolean allowClear, String placeholderText, Node placeholderGraphic, CBMBMenuItem<V>... items) {
		this(allowClear, placeholderText, placeholderGraphic);
		if (allowClear) {
			addSeparator();
		}
		for (CBMBMenuItem<V> item : items) {
			addItem(item);
		}
	}

	@SafeVarargs
	public ComboBoxMenuButton(String placeholderText, Node placeholderGraphic, CBMBGroupMenu<V>... classGroups) {
		this(true, placeholderText, placeholderGraphic, classGroups);
	}

	@SafeVarargs
	public ComboBoxMenuButton(boolean allowClear, String placeholderText, Node placeholderGraphic, CBMBGroupMenu<V>... classGroups) {
		this(allowClear, placeholderText, placeholderGraphic);
		if (allowClear && classGroups.length > 0) {
			addSeparator();
		}
		for (CBMBGroupMenu<V> group : classGroups) {
			addItem(group);
		}
	}

	private void addSeparator() {
		menuButton.getItems().add(new SeparatorMenuItem());
		separatorAdded = true;
	}

	public ComboBoxMenuButton(String placeholderText, Node placeholderGraphic) {
		this(true, placeholderText, placeholderGraphic);
	}

	public ComboBoxMenuButton(boolean allowClear, String placeholderText, Node placeholderGraphic) {
		this.placeholderText = placeholderText;
		this.placeholderGraphic = placeholderGraphic;
		this.allowClear = allowClear;
		getChildren().add(menuButton);

		if (allowClear) {
			initializeClearMenuItem();

		}
		clearSelectedValue();
		menuButton.setMnemonicParsing(false);
		menuButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

	}

	private void setupMenuItem(final CBMBMenuItem<V> item) {
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chooseItem(item);
				item.actionEvent(event);
			}
		});
	}

	private void initializeClearMenuItem() {
		final MenuItem clearMenuItem = new MenuItem(Lang.FxControlBundle().getString("ComboBoxMenuButton.select_none"));
		clearMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clearSelectedValue();
			}
		});
		menuButton.getItems().add(clearMenuItem);
	}

	public void addItem(@NotNull CBMBMenuItem<V> item) {
		if (allowClear && !separatorAdded) {
			addSeparator();
		}
		menuButton.getItems().add(item);
		setupMenuItem(item);
	}

	public void addItem(@NotNull CBMBGroupMenu<V> group) {
		if (allowClear && !separatorAdded) {
			addSeparator();
		}
		menuButton.getItems().add(group);
		for (CBMBMenuItem<V> item : group.getCbmbMenuItems()) {
			setupMenuItem(item);
		}
	}


	public void clearSelectedValue() {
		selectedItemObserver.updateValue(null);
		menuButton.setText(placeholderText);
		menuButton.setGraphic(placeholderGraphic);
	}

	public void chooseItem(@Nullable V value) {
		if (value == null) {
			clearSelectedValue();
			return;
		}
		if (value == selectedItemObserver.getValue()) {
			return;
		}
		for (MenuItem menuItem : menuButton.getItems()) {
			if (menuItem instanceof CBMBMenuItem) {
				CBMBMenuItem<V> item = (CBMBMenuItem<V>) menuItem;
				if (item.getValue() == value) {
					chooseItem(item);
					return;
				}
				continue;
			}
			if (menuItem instanceof CBMBGroupMenu) {
				CBMBGroupMenu<V> groupMenu = (CBMBGroupMenu<V>) menuItem;
				for (CBMBMenuItem<V> item : groupMenu.getCbmbMenuItems()) {
					if (item.getValue() == value) {
						chooseItem(item);
						return;
					}
				}
			}
		}
	}

	public void chooseItem(@Nullable CBMBMenuItem<V> menuItem) {
		if (menuItem == null) {
			clearSelectedValue();
			return;
		}
		ImageContainer container = menuItem.imageContainer;
		if (container != null) {
			menuButton.setGraphic(container.copy().getNode());
		} else {
			menuButton.setGraphic(null);
		}
		menuButton.setText(menuItem.getText());
		selectedItemObserver.updateValue(menuItem.getValue());
	}

	@NotNull
	public ReadOnlyValueObserver<V> getSelectedItemObserver() {
		return selectedItemObserver.getReadOnlyValueObserver();
	}



}
