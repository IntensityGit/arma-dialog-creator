package com.armadialogcreator.gui.main.stringtable;

import com.armadialogcreator.ArmaDialogCreator;
import com.armadialogcreator.core.stringtable.KnownLanguage;
import com.armadialogcreator.core.stringtable.Language;
import com.armadialogcreator.core.stringtable.StringTable;
import com.armadialogcreator.core.stringtable.StringTableKey;
import com.armadialogcreator.gui.SimpleResponseDialog;
import com.armadialogcreator.gui.StageDialog;
import com.armadialogcreator.gui.fxcontrol.DownArrowMenu;
import com.armadialogcreator.gui.main.popup.NameTextFieldDialog;
import com.armadialogcreator.img.icons.ADCIcons;
import com.armadialogcreator.lang.Lang;
import com.armadialogcreator.util.NotNullValueListener;
import com.armadialogcreator.util.NotNullValueObserver;
import com.armadialogcreator.util.ValueListener;
import com.armadialogcreator.util.ValueObserver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 @author Kayler
 @since 12/24/2016 */
class StringTableKeyEditorPane extends StackPane {
	private final ResourceBundle bundle = Lang.getBundle("StringTableBundle");
	private final LanguageSelectionPane languagePane;
	private final String noKeySelected = bundle.getString("StringTableEditorPopup.Tab.Edit.no_selected_key");
	private final Label lblKeyId = new Label();
	private final Menu menuAddLanguage;
	private final Menu menuRemoveLanguage;
	private final FlowPane paneContent;
	private StringTableKey key;
	private List<StringTableKey> existingKeys;

	public StringTableKeyEditorPane(@NotNull StringTable table, @Nullable Language defaultPreviewLanguage) {

		StringTableValueEditor taValue = new StringTableValueEditor(this);
		taValue.setWrapText(true);

		languagePane = new LanguageSelectionPane(defaultPreviewLanguage);
		languagePane.getChosenLanguageObserver().addListener(new ValueListener<Language>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<Language> observer, @Nullable Language oldValue, @Nullable Language selected) {
				if (key == null || selected == null) {
					taValue.replaceText("");
				} else {
					String s = key.getLanguageTokenMap().get(selected);
					if (s == null) {
						Map.Entry<Language, String> entry = key.getFirstLanguageTokenEntry();
						if (entry == null || entry.getValue() == null) {
							s = "";
						} else {
							s = entry.getValue();
						}
					}

					taValue.replaceText(s);
				}
			}

		});

		paneContent = new FlowPane(10, 10);
		getChildren().add(paneContent);

		lblKeyId.setFont(Font.font(15));

		menuAddLanguage = new Menu(bundle.getString("StringTableEditorPopup.Tab.Edit.add_language"));
		menuRemoveLanguage = new Menu(bundle.getString("StringTableEditorPopup.Tab.Edit.remove_language"));

		MenuItem miEditPackage = new MenuItem(bundle.getString("StringTableEditorPopup.Tab.Edit.edit_package"));
		miEditPackage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (key == null) {
					return;
				}
				NameTextFieldDialog dialog = new NameTextFieldDialog(
						String.format(bundle.getString("StringTableEditorPopup.Tab.Edit.edit_package_popup_title_f"), key.getId()),
						bundle.getString("StringTableEditorPopup.Tab.Edit.new_package_name"),
						bundle.getString("StringTable.no_package")
				);
				dialog.setInputText(key.getPath().getPackageName());
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				String packageName = dialog.getInputText() == null ? null : (dialog.getInputText().length() == 0 ? null : dialog.getInputText());
				key.getPath().setPackageName(packageName);
			}
		});
		MenuItem miEditContainer = new MenuItem(bundle.getString("StringTableEditorPopup.Tab.Edit.edit_container"));
		miEditContainer.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (key == null) {
					return;
				}
				ContainerEditorDialog dialog = new ContainerEditorDialog(ArmaDialogCreator.getPrimaryStage(), key,
						table);
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				key.getPath().getContainers().setAll(dialog.getContainers());
			}
		});
		MenuItem miRenameId = new MenuItem(bundle.getString("StringTableEditorPopup.Tab.Edit.rename"));
		miRenameId.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (key == null) {
					return;
				}
				NameTextFieldDialog dialog = new NameTextFieldDialog(
						miRenameId.getText(),
						bundle.getString("StringTableEditorPopup.Tab.Edit.new_id")
				);
				dialog.setInputText(key.getId());
				dialog.inputTextProperty().addListener(new ChangeListener<String>() {
					boolean added;
					final String badInput = "bad-input-text-field";

					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						newValue = newValue != null ? newValue : "";
						if (!StringTableKey.idIsProper(newValue)) {
							if (!added) {
								added = true;
								dialog.getTextField().getStyleClass().add(badInput);
								dialog.getCanOkProperty().setValue(false);
							}
						} else {
							added = false;
							dialog.getTextField().getStyleClass().remove(badInput);
							dialog.getCanOkProperty().setValue(true);
						}
					}
				});
				dialog.show();
				if (dialog.wasCancelled() || dialog.getInputText() == null || dialog.getInputText().equals(key.getId())) {
					return;
				}

				for (StringTableKey key : existingKeys) {
					if (key.getId().equals(dialog.getInputText())) {
						new KeyAlreadyExistsDialog(key).show();
						dialog.show();
						return;
					}
				}
				key.setId(dialog.getInputText());
			}
		});

		HBox hboxKey = new HBox(5, new DownArrowMenu(menuAddLanguage, menuRemoveLanguage, new SeparatorMenuItem(), miEditPackage, miEditContainer, miRenameId), lblKeyId);
		hboxKey.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(lblKeyId, Priority.ALWAYS);
		VBox vboxLeft = new VBox(10, hboxKey, languagePane);
		vboxLeft.setFillWidth(true);

		taValue.minWidthProperty().bind(paneContent.widthProperty());

		paneContent.getChildren().addAll(vboxLeft, taValue);


		setKey(null, null);
	}

	/**
	 Set the key that is being edited

	 @param key key to edit
	 @param existingKeys existing keys (will be used to check if new key name isn't duplicate), or null if <code>key</code> is null
	 @throws NullPointerException     if <code>key</code> is not null but table is
	 @throws IllegalArgumentException if <code>key</code> doesn't exist in <code>table</code>
	 */
	public void setKey(@Nullable StringTableKey key, @Nullable List<StringTableKey> existingKeys) {
		this.key = key;
		this.existingKeys = existingKeys;
		languagePane.setToKey(key);
		if (key == null) {
			lblKeyId.setText(noKeySelected);
		} else {
			if (existingKeys == null) {
				throw new NullPointerException("existingKeys is null when key isn't");
			}
			if (!existingKeys.contains(key)) {
				throw new IllegalArgumentException("key doesn't exist in existingKeys");
			}

			lblKeyId.setText(key.getId());
			key.getIdObserver().addListener(new NotNullValueListener<>() {
				@Override
				public void valueUpdated(@NotNull NotNullValueObserver<String> observer, @NotNull String oldValue, @NotNull String newValue) {
					lblKeyId.setText(newValue);
				}
			});
		}
		handleKeyDownMenus();
		setDisable(key == null);
	}

	@NotNull
	public LanguageSelectionPane getLanguagePane() {
		return languagePane;
	}

	@Nullable
	public StringTableKey getKey() {
		return key;
	}

	/** Get the pane that holds the text area and key label and stuff */
	@NotNull
	public FlowPane getPaneContent() {
		return paneContent;
	}

	private void handleKeyDownMenus() {
		menuRemoveLanguage.getItems().clear();
		menuAddLanguage.getItems().clear();
		if (key == null) {
			return;
		}
		for (Language language : KnownLanguage.values()) {
			boolean found = false;
			MenuItem miLanguage = new MenuItem(language.getName());
			miLanguage.setUserData(language);

			for (Map.Entry<Language, String> entry : key.getLanguageTokenMap().entrySet()) {
				if (entry.getKey().equals(language)) {
					found = true;
					menuRemoveLanguage.getItems().add(miLanguage);
					miLanguage.setOnAction(new LanguageMenuItemEvent(true, key, language, miLanguage, menuAddLanguage, menuRemoveLanguage));
					break;
				}
			}
			if (!found) {
				menuAddLanguage.getItems().add(miLanguage);
				miLanguage.setOnAction(new LanguageMenuItemEvent(false, key, language, miLanguage, menuAddLanguage, menuRemoveLanguage));
			}
		}
	}

	private class LanguageMenuItemEvent implements EventHandler<ActionEvent> {
		private final StringTableKey key;
		private final Language language;
		private final MenuItem miLanguage;
		private final Menu menuAddLanguage;
		private final Menu menuRemoveLanguage;
		private boolean added;

		public LanguageMenuItemEvent(boolean added, @NotNull StringTableKey key, @NotNull Language language, @NotNull MenuItem miLanguage, @NotNull Menu menuAddLanguage, @NotNull Menu
				menuRemoveLanguage) {
			this.added = added;
			this.key = key;
			this.language = language;
			this.miLanguage = miLanguage;
			this.menuAddLanguage = menuAddLanguage;
			this.menuRemoveLanguage = menuRemoveLanguage;
		}

		@Override
		public void handle(ActionEvent event) {
			boolean added = !this.added;
			if (added) {
				menuAddLanguage.getItems().remove(miLanguage);
				menuRemoveLanguage.getItems().add(miLanguage);
				key.getLanguageTokenMap().put(language, "");
			} else {
				SimpleResponseDialog dialog = new SimpleResponseDialog(
						ArmaDialogCreator.getPrimaryStage(),
						bundle.getString("StringTableEditorPopup.Tab.Edit.remove_language"),
						String.format(bundle.getString("StringTableEditorPopup.Tab.Edit.remove_language_confirmation_f"), language.getName()),
						true, true, false
				);
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				menuRemoveLanguage.getItems().remove(miLanguage);
				menuAddLanguage.getItems().add(miLanguage);
				key.getLanguageTokenMap().remove(language);
			}

			this.added = added;

		}
	}

	private class ContainerEditorDialog extends StageDialog<FlowPane> {

		private final List<ComboBox<String>> containerComboBoxes = new ArrayList<>();
		private final StringTable table;

		public ContainerEditorDialog(@NotNull Stage parentStage, @NotNull StringTableKey key,
									 @NotNull StringTable table) {
			super(parentStage, new FlowPane(5, 10), null, true, true, false);
			this.table = table;
			setTitle(
					String.format(
							bundle.getString("StringTableEditorPopup.Tab.Edit.edit_container_popup_title_f"),
							key.getId()
					)
			);
			int level = 0;
			for (String container : key.getPath().getContainers()) {
				ComboBox<String> comboBox = getComboBox(level);
				containerComboBoxes.add(comboBox);
				comboBox.getSelectionModel().select(container);
				level++;
			}

			HBox hboxInsertRemove;
			{
				Button btnRemove = new Button("", new ImageView(ADCIcons.ICON_MINUS));
				Button btnInsert = new Button("", new ImageView(ADCIcons.ICON_PLUS));
				hboxInsertRemove = new HBox(5, btnRemove, btnInsert);

				btnRemove.setOnAction(e -> {
					String value = containerComboBoxes.get(containerComboBoxes.size() - 1).getValue();
					//if value is null or empty, just remove it without confirmation
					if (value != null && value.trim().length() != 0) {
						SimpleResponseDialog dialog = new SimpleResponseDialog(
								parentStage,
								bundle.getString("StringTableEditorPopup.Tab.Edit.remove_container_confirm_title"),
								String.format(
										bundle.getString("StringTableEditorPopup.Tab.Edit.remove_container_confirm_f"),
										value
								),
								true, true, false
						);
						dialog.show();
						if (dialog.wasCancelled()) {
							return;
						}
					}
					ComboBox<String> removed = containerComboBoxes.remove(containerComboBoxes.size() - 1);
					btnRemove.setDisable(containerComboBoxes.size() == 0);
					ContainerEditorDialog.this.myRootElement.getChildren().remove(removed);
				});

				btnInsert.setOnAction(e -> {
					btnRemove.setDisable(false);
					ComboBox<String> cb = getComboBox(containerComboBoxes.size());
					containerComboBoxes.add(cb);
					ObservableList<Node> children = ContainerEditorDialog.this.myRootElement.getChildren();
					children.add(
							//insert just before the hbox
							children.indexOf(hboxInsertRemove),
							cb
					);
				});

				btnRemove.setDisable(containerComboBoxes.size() == 0);
			}

			for (ComboBox<String> comboBox : containerComboBoxes) {
				myRootElement.getChildren().add(comboBox);
			}

			myRootElement.getChildren().add(hboxInsertRemove);
		}

		@NotNull
		private ComboBox<String> getComboBox(int level) {
			ComboBox<String> comboBox = new ComboBox<>();
			for (StringTableKey keyAtLevel : table.getKeys()) {
				List<String> containersForKey = keyAtLevel.getPath().getContainers();
				String containerAtLevel = level >= containersForKey.size() ?
						null : containersForKey.get(level);
				if (containerAtLevel == null) {
					continue;
				}
				if (comboBox.getItems().contains(containerAtLevel)) {
					continue;
				}
				comboBox.getItems().add(containerAtLevel);
			}
			comboBox.setEditable(true);
			return comboBox;
		}

		/** @return the new containers list that the key should use */
		@NotNull
		public List<String> getContainers() {
			List<String> ret = new ArrayList<>(containerComboBoxes.size());
			for (ComboBox<String> cb : containerComboBoxes) {
				ret.add(cb.getValue());
			}
			return ret;
		}
	}

}
