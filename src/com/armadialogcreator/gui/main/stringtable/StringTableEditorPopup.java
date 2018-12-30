package com.armadialogcreator.gui.main.stringtable;

import com.armadialogcreator.arma.stringtable.*;
import com.armadialogcreator.gui.fxcontrol.SearchTextField;
import com.armadialogcreator.gui.img.ADCImages;
import com.armadialogcreator.gui.popup.GenericResponseFooter;
import com.armadialogcreator.gui.popup.SimpleResponseDialog;
import com.armadialogcreator.gui.popup.StagePopup;
import com.armadialogcreator.main.ArmaDialogCreator;
import com.armadialogcreator.main.HelpUrls;
import com.armadialogcreator.main.Lang;
import com.armadialogcreator.util.BrowserUtil;
import com.armadialogcreator.util.KeyValue;
import com.armadialogcreator.util.ValueListener;
import com.armadialogcreator.util.ValueObserver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 @author Kayler
 @since 12/14/2016 */
public class StringTableEditorPopup extends StagePopup<VBox> {

	private final StringTableEditorTabPane tabPane;

	/** String to be used for when {@link StringTableKeyPath#noPackageName()} is true */
	private final String noPackageName,
	/** String to be used for when {@link StringTableKeyPath#noContainer()} is true */
	noContainerName;

	private StringTable table;

	private final LinkedList<ListChangeListener<StringTableKey>> listenersToRemoveFromTable = new LinkedList<>();
	private final ResourceBundle bundle = Lang.getBundle("StringTableBundle");

	public StringTableEditorPopup(@NotNull StringTable table, @NotNull StringTableWriter writer, @NotNull StringTableParser parser) {
		super(ArmaDialogCreator.getPrimaryStage(), new VBox(0), null);

		setTitle(bundle.getString("StringTableEditorPopup.popup_title"));

		this.table = table;

		noPackageName = bundle.getString("StringTable.no_package");
		noContainerName = bundle.getString("StringTable.no_container");

		Button btnInsert = new Button("", new ImageView(ADCImages.ICON_PLUS));
		btnInsert.setTooltip(new Tooltip(bundle.getString("StringTableEditorPopup.Tab.Edit.insert_key_tooltip")));
		btnInsert.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				NewStringTableKeyDialog dialog = new NewStringTableKeyDialog(getTable());
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				EditTab editTab = tabPane.getEditTab();
				StringTableKeyDescriptor newKey = editTab.addNewKey(dialog.getKey());
				getTable().getKeys().add(dialog.getKey());
				editTab.getListView().getSelectionModel().select(newKey);
				editTab.getListView().scrollTo(newKey);
			}
		});
		Button btnRemove = new Button("", new ImageView(ADCImages.ICON_MINUS));
		btnRemove.setTooltip(new Tooltip(bundle.getString("StringTableEditorPopup.Tab.Edit.remove_key_tooltip")));
		btnRemove.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ListView<StringTableKeyDescriptor> listView = tabPane.getEditTab().getListView();
				StringTableKeyDescriptor selected = listView.getSelectionModel().getSelectedItem();
				if (selected == null) {
					return;
				}
				SimpleResponseDialog dialog = new SimpleResponseDialog(
						ArmaDialogCreator.getPrimaryStage(),
						btnRemove.getTooltip().getText(),
						String.format(bundle.getString("StringTableEditorPopup.Tab.Edit.remove_key_popup_body_f"), selected.getKey().getId()),
						true, true, false
				);
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				tabPane.getEditTab().removeKey(selected);
			}
		});

		tabPane = new StringTableEditorTabPane(this, table, btnRemove.disableProperty(), this);

		btnRemove.setDisable(tabPane.getEditTab().getListView().getSelectionModel().isEmpty());
		Button btnRefresh = new Button("", new ImageView(ADCImages.ICON_REFRESH));
		btnRefresh.setTooltip(new Tooltip(bundle.getString("StringTableEditorPopup.ToolBar.reload_tooltip")));
		btnRefresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				SimpleResponseDialog dialog = new SimpleResponseDialog(
						ArmaDialogCreator.getPrimaryStage(),
						bundle.getString("StringTableEditorPopup.ToolBar.reload_popup_title"),
						bundle.getString("StringTableEditorPopup.ToolBar.reload_popup_body"),
						true, true, false
				);
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				try {
					getTable().setTo(parser.createStringTableInstance());
					tabPane.setToTable(getTable());
				} catch (IOException e) {
					new SimpleResponseDialog(
							ArmaDialogCreator.getPrimaryStage(),
							bundle.getString("Error.couldnt_refresh_short"),
							bundle.getString("Error.couldnt_refresh") + "\n" + e.getMessage(),
							false, true, false
					).show();
				}

			}
		});
		Button btnSave = new Button("", new ImageView(ADCImages.ICON_SAVE));
		btnSave.setTooltip(new Tooltip(bundle.getString("StringTableEditorPopup.ToolBar.save_tooltip")));
		btnSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				SimpleResponseDialog dialog = new SimpleResponseDialog(
						myStage, bundle.getString("SaveDialog.dialog_title"),
						bundle.getString("SaveDialog.body"),
						true,
						true,
						false
				);
				dialog.setStageSize(420, 150);
				dialog.setResizable(false);
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				try {
					writer.writeTable(getTable());
				} catch (IOException e) {
					new SimpleResponseDialog(
							ArmaDialogCreator.getPrimaryStage(),
							bundle.getString("Error.couldnt_save_short"),
							bundle.getString("Error.couldnt_save") + "\n" + e.getMessage(),
							false, true, false
					).show();
				}
			}
		});

		btnRemove.disabledProperty();
		myRootElement.getChildren().add(new ToolBar(btnRefresh, btnSave, new Separator(Orientation.VERTICAL), btnInsert, btnRemove));
		myRootElement.getChildren().add(tabPane);

		GenericResponseFooter responseFooter = getBoundResponseFooter(false, true, true);
		VBox.setMargin(responseFooter, new Insets(10));
		myRootElement.getChildren().addAll(new Separator(Orientation.HORIZONTAL), responseFooter);
		VBox.setVgrow(tabPane, Priority.ALWAYS);

		setStageSize(720, 480);

	}

	@Override
	protected void help() {
		BrowserUtil.browse(HelpUrls.STRING_TABLE_EDITOR);
	}

	/** Get the table that is being edited */
	@NotNull
	public StringTable getTable() {
		return table;
	}

	@Override
	protected void closing() {
		clearListeners();
		super.closing();
	}

	private void clearListeners() {
		for (ListChangeListener<StringTableKey> listener : listenersToRemoveFromTable) {
			table.getKeys().removeListener(listener);
		}
		listenersToRemoveFromTable.clear();
	}

	private class StringTableEditorTabPane extends TabPane {
		private final ValueObserver<Language> previewLanguageObserver = new ValueObserver<>(KnownLanguage.Original);
		private final StringTableEditorPopup popup;
		private final BooleanProperty disableRemove;
		private final StringTableEditorPopup editorPopup;
		private EditTab editTab;

		public StringTableEditorTabPane(@NotNull StringTableEditorPopup popup, @NotNull StringTable table, @NotNull BooleanProperty disableRemove, @NotNull StringTableEditorPopup editorPopup) {
			this.popup = popup;
			this.disableRemove = disableRemove;
			this.editorPopup = editorPopup;
			setToTable(table);
		}

		public void setToTable(@NotNull StringTable table) {
			getTabs().clear();
			popup.clearListeners();

			editTab = new EditTab(table, previewLanguageObserver, editorPopup);
			editTab.getListView().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StringTableKeyDescriptor>() {
				@Override
				public void changed(ObservableValue<? extends StringTableKeyDescriptor> observable, StringTableKeyDescriptor oldValue, StringTableKeyDescriptor selected) {
					disableRemove.setValue(selected == null);
				}
			});
			getTabs().add(editTab);
			getTabs().add(new ConfigTab(popup, table, previewLanguageObserver));
			getTabs().add(new GraphsTab(popup, table));
		}

		@NotNull
		public EditTab getEditTab() {
			return editTab;
		}
	}

	private class ConfigTab extends Tab { //set xml things like project name attribute (project=root tag of stringtable.xml)

		public ConfigTab(@NotNull StringTableEditorPopup popup, @NotNull StringTable table, @NotNull ValueObserver<Language> previewLanguageObserver) {
			super(bundle.getString("StringTableEditorPopup.Tab.Config.tab_title"));
			VBox root = new VBox(10);
			root.setPadding(new Insets(10));
			root.setFillWidth(true);
			setContent(root);
			setGraphic(new ImageView(ADCImages.ICON_GEAR));
			setClosable(false);

			ComboBox<Language> comboBoxLanguage = new ComboBox<>();
			comboBoxLanguage.getItems().addAll(KnownLanguage.values());
			comboBoxLanguage.getSelectionModel().select(previewLanguageObserver.getValue());
			comboBoxLanguage.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Language>() {
				@Override
				public void changed(ObservableValue<? extends Language> observable, Language oldValue, Language newValue) {
					previewLanguageObserver.updateValue(newValue);
				}
			});
			Label lblPreviewLanguage = new Label(bundle.getString("StringTableEditorPopup.Tab.Config.preview_language"), comboBoxLanguage);
			lblPreviewLanguage.setContentDisplay(ContentDisplay.RIGHT);
			root.getChildren().add(lblPreviewLanguage);

			Label lblSize = new Label(String.format(bundle.getString("StringTableEditorPopup.Tab.Config.number_of_keys_f"), table.getKeys().size()));
			root.getChildren().add(lblSize);

			ListChangeListener<StringTableKey> keysListener = new ListChangeListener<StringTableKey>() {
				@Override
				public void onChanged(Change<? extends StringTableKey> c) {
					lblSize.setText(String.format(bundle.getString("StringTableEditorPopup.Tab.Config.number_of_keys_f"), table.getKeys().size()));
				}
			};
			popup.listenersToRemoveFromTable.add(keysListener);
			table.getKeys().addListener(keysListener);
		}


	}

	private class EditTab extends Tab {
		private final Comparator<StringTableKeyDescriptor> comparator = new Comparator<StringTableKeyDescriptor>() {
			@Override
			public int compare(StringTableKeyDescriptor o1, StringTableKeyDescriptor o2) {
				return o1.getKey().getId().compareToIgnoreCase(o2.getKey().getId());
			}
		};

		private final ObservableList<StringTableKeyDescriptor> listViewItemList;
		private ValueObserver<Language> previewLanguageObserver;
		private StringTableEditorPopup editorPopup;

		private final List<StringTableKeyDescriptor> allItems = new LinkedList<>();
		private final ListView<StringTableKeyDescriptor> lvMatch = new ListView<>();
		private final StringTableKeyEditorPane editorPane;


		public EditTab(@NotNull StringTable table, @NotNull ValueObserver<Language> previewLanguageObserver, @NotNull StringTableEditorPopup editorPopup) {
			super(bundle.getString("StringTableEditorPopup.Tab.Edit.tab_title"));

			listViewItemList = FXCollections.observableList(new ArrayList<>(), new Callback<StringTableKeyDescriptor, javafx.beans.Observable[]>() {
				public javafx.beans.Observable[] call(StringTableKeyDescriptor param) {
					return new javafx.beans.Observable[]{
							param.getKey().getLanguageTokenMap(),
							param.getKey().getIdObserver(),
							previewLanguageObserver,
							param.getKey().getPath()
					};
				}
			}); //for some reason, can't have a LinkedList as the underlying list implementation if we want the list view to update the displayed cell text automatically
			this.previewLanguageObserver = previewLanguageObserver;
			this.editorPopup = editorPopup;

			previewLanguageObserver.addListener(new ValueListener<Language>() {
				@Override
				public void valueUpdated(@NotNull ValueObserver<Language> observer, @Nullable Language oldValue, @Nullable Language newValue) {
					for (StringTableKeyDescriptor descriptor : allItems) {
						descriptor.setPreviewLanguage(newValue);
					}
				}
			});

			editorPane = new StringTableKeyEditorPane(table, KnownLanguage.Original);

			lvMatch.setPlaceholder(new Label(bundle.getString("StringTableEditorPopup.Tab.Edit.Search.no_match")));
			lvMatch.setStyle("-fx-font-family:monospace");
			for (StringTableKey key : table.getKeys()) {
				addNewKey(key);
			}

			lvMatch.setItems(listViewItemList);
			lvMatch.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StringTableKeyDescriptor>() {
				@Override
				public void changed(ObservableValue<? extends StringTableKeyDescriptor> observable, StringTableKeyDescriptor oldValue, StringTableKeyDescriptor selected) {
					if (selected != null) {
						editorPane.setKey(selected.getKey(), table.getKeys());
					} else {
						editorPane.setKey(null, table.getKeys());
					}
				}
			});

			SearchTextField tfSearch = new StringTableSearchField(lvMatch, allItems);

			VBox vbRoot = new VBox(10, tfSearch, editorPane, lvMatch);
			VBox.setVgrow(lvMatch, Priority.ALWAYS);
			vbRoot.setFillWidth(true);
			vbRoot.setPadding(new Insets(10));
			setContent(vbRoot);
			setClosable(false);

		}

		@NotNull
		public ListView<StringTableKeyDescriptor> getListView() {
			return lvMatch;
		}

		/**
		 Use this instead of adding to {@link ListView#getItems()} with {@link #getListView()}

		 @return the key that was added
		 */
		public StringTableKeyDescriptor addNewKey(@NotNull StringTableKey key) {
			StringTableKeyDescriptor descriptor = new StringTableKeyDescriptor(key, editorPopup.noPackageName, editorPopup.noContainerName);
			descriptor.setPreviewLanguage(previewLanguageObserver.getValue());
			allItems.add(descriptor);
			listViewItemList.add(descriptor);
			listViewItemList.sort(comparator);
			allItems.sort(comparator);
			return descriptor;
		}

		/** Use this instead of removing from {@link ListView#getItems()} with {@link #getListView()} */
		public void removeKey(@NotNull StringTableKey key) {
			StringTableKeyDescriptor match = null;
			for (StringTableKeyDescriptor descriptor : allItems) {
				if (descriptor.getKey().equals(key)) {
					match = descriptor;
					break;
				}
			}
			if (match == null) {
				return;
			}
			removeKey(match);
		}

		public void removeKey(@NotNull StringTableKeyDescriptor key) {
			allItems.remove(key);
			listViewItemList.remove(key);
		}
	}

	private class GraphsTab extends Tab {
		private final StringTable table;

		private final CategoryAxis xAxis = new CategoryAxis();
		private final NumberAxis yAxis = new NumberAxis(0, 0, 0);
		private final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
		private final XYChart.Series<String, Number> series = new XYChart.Series<>();

		public GraphsTab(@NotNull StringTableEditorPopup popup, @NotNull StringTable table) {
			this.table = table;

			ListChangeListener<StringTableKey> keyListener = new ListChangeListener<StringTableKey>() {
				@Override
				public void onChanged(Change<? extends StringTableKey> c) {
					updateGraph();
				}
			};
			popup.listenersToRemoveFromTable.add(keyListener);
			table.getKeys().addListener(keyListener);

			setText(bundle.getString("StringTableEditorPopup.Tab.Graph.tab_title"));

			setClosable(false);

			initContent();

			updateGraph();
		}

		private void initContent() {
			VBox root = new VBox(10);
			setContent(root);

			root.getChildren().add(chart);

			chart.setTitle(bundle.getString("StringTableEditorPopup.Tab.Graph.graph_label"));

			xAxis.setLabel(bundle.getString("StringTableEditorPopup.Tab.Graph.x_axis"));
			yAxis.setLabel(bundle.getString("StringTableEditorPopup.Tab.Graph.y_axis"));

			series.setName(bundle.getString("StringTableEditorPopup.Tab.Graph.series_label"));
		}

		private void updateGraph() {
			chart.getData().clear();

			ArrayList<KeyValue<String, Integer>> usedLanguages = new ArrayList<>();

			final int numKeys = table.getKeys().size();

			for (StringTableKey key : table.getKeys()) {
				for (Map.Entry<Language, String> entry : key.getLanguageTokenMap().entrySet()) {
					String langName = entry.getKey().getName();
					boolean found = false;
					for (KeyValue<String, Integer> usedLanguage : usedLanguages) {
						if (usedLanguage.getKey().equals(langName)) {
							found = true;
							usedLanguage.setValue(usedLanguage.getValue() + 1);
							break;
						}
					}
					if (!found) {
						usedLanguages.add(new KeyValue<>(langName, 1));
					}
				}
			}


			ObservableList<String> languages = FXCollections.observableArrayList();
			double max = 0;

			series.getData().clear();

			for (KeyValue<String, Integer> usedLanguage : usedLanguages) {
				String langName = usedLanguage.getKey();
				languages.add(langName);
				int v = usedLanguage.getValue();
				max = Math.max(max, v);

				series.getData().add(new XYChart.Data<>(langName, v));

			}

			xAxis.setCategories(languages);

			yAxis.setTickUnit(Math.floor(max / 4));
			yAxis.setUpperBound(numKeys);
			yAxis.setLowerBound(0);

			chart.getData().add(series);

		}
	}


}
