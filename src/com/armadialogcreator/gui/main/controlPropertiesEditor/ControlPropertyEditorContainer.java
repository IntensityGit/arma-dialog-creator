package com.armadialogcreator.gui.main.controlPropertiesEditor;

import com.armadialogcreator.ArmaDialogCreator;
import com.armadialogcreator.core.Macro;
import com.armadialogcreator.core.PropertyType;
import com.armadialogcreator.core.old.*;
import com.armadialogcreator.core.sv.SVRaw;
import com.armadialogcreator.core.sv.SerializableValue;
import com.armadialogcreator.core.sv.SerializableValueConversionException;
import com.armadialogcreator.gui.SimpleResponseDialog;
import com.armadialogcreator.gui.StageDialog;
import com.armadialogcreator.lang.Lang;
import com.armadialogcreator.util.UpdateListenerGroup;
import com.armadialogcreator.util.ValueListener;
import com.armadialogcreator.util.ValueObserver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ResourceBundle;
import java.util.function.Function;

import static com.armadialogcreator.gui.main.controlPropertiesEditor.ControlPropertyValueEditors.*;

/**
 @author Kayler
 @since 11/20/2016 */
class ControlPropertyEditorContainer extends HBox {
	private static final Font TOOLTIP_FONT = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 20d);
	private static final ResourceBundle bundle = Lang.getBundle("ControlPropertyEditorBundle");
	private static final ControlPropertyDocumentationProvider lookupDocProvider = new ControlPropertyDocumentationProvider();

	private final ControlClassOld controlClass;
	private final ControlProperty controlProperty;

	private final StackPane stackPanePropertyInput = new StackPane();
	private final MenuButton menuButtonOptions = new MenuButton();
	private MenuItem inheritanceMenuItem;
	private ControlPropertyValueEditor propertyValueEditor;
	private ControlPropertyUpdateListener controlPropertyUpdateListener;
	private ControlClassUpdateListener controlClassUpdateListener;

	private boolean hideIfInherited;
	private Node stackPaneTint;

	public ControlPropertyEditorContainer(@NotNull ControlClassOld controlClass, @NotNull ControlProperty property) {
		super(5);
		this.controlClass = controlClass;
		this.controlProperty = property;

		resetPropertyValueEditor();
		init();

		setAlignment(Pos.TOP_LEFT);
		setMaxWidth(Double.MAX_VALUE);
	}

	private void init() {
		placeTooltip(menuButtonOptions);

		final MenuItem miDisplayType = new MenuItem(bundle.getString("display_type"));
		final MenuItem miDefaultEditor = new MenuItem(bundle.getString("use_default_editor"));
		final MenuItem miResetToInitial = new MenuItem(bundle.getString("reset_to_initial"));
		final MenuItem miConvert = new MenuItem(bundle.getString("convert_value"));
		final MenuItem miMacro = new MenuItem(bundle.getString("set_to_macro"));
		inheritanceMenuItem = new MenuItem(
				controlProperty.isInherited() ? bundle.getString("override") :
						bundle.getString("inherit")
		);
		final MenuItem miClearValue = new MenuItem(bundle.getString("clear_value"));
		final CheckMenuItem miRaw = new CheckMenuItem(bundle.getString("raw"));
		menuButtonOptions.setText(controlProperty.getName());
		menuButtonOptions.getItems().setAll(
				miDisplayType,
				miDefaultEditor,
				new SeparatorMenuItem(),
				miResetToInitial,
				miMacro,
				miConvert,
				inheritanceMenuItem,
				new SeparatorMenuItem(),
				miRaw,
				miClearValue
		);
		HBox.setHgrow(menuButtonOptions, Priority.ALWAYS);

		getChildren().addAll(menuButtonOptions, new Label("="), stackPanePropertyInput);

		if (SerializableValue.getTypesCanConvertTo(controlProperty.getInitialPropertyType()).size() == 1) {
			menuButtonOptions.getItems().remove(miConvert);
		}

		controlClassUpdateListener = new ControlClassUpdateListener(controlClass) {
			@Override
			public void update(@NotNull UpdateListenerGroup<ControlClassUpdate> group, ControlClassUpdate data) {
				if (data instanceof ControlClassExtendUpdate) {
					ControlClassExtendUpdate update = (ControlClassExtendUpdate) data;
					inheritanceMenuItem.setVisible(update.getNewExtendClass() != null);
				}
			}
		};
		controlClass.getControlClassUpdateGroup().addListener(controlClassUpdateListener);

		Function<Void, Void> propertyInheritUpdate = (v) -> {
			boolean inherited = controlProperty.isInherited();
			updateContainerInheritanceTint();
			stackPanePropertyInput.setDisable(inherited);
			miDefaultEditor.setDisable(inherited);
			miResetToInitial.setDisable(inherited);
			miMacro.setDisable(inherited);
			miClearValue.setDisable(inherited);
			miRaw.setDisable(inherited);
			miConvert.setDisable(inherited || controlProperty.getValue() == null);

			if (inherited) {
				inheritanceMenuItem.setText(bundle.getString("override"));
			} else {
				inheritanceMenuItem.setText(bundle.getString("inherit"));
			}
			inheritanceMenuItem.setVisible(controlClass.getExtendClass() != null);
			hideIfInherited(ControlPropertyEditorContainer.this.hideIfInherited);
			return null;
		};

		controlPropertyUpdateListener = new ControlPropertyUpdateListener(controlProperty) {
			@Override
			public void update(@NotNull UpdateListenerGroup<ControlPropertyUpdate> group, ControlPropertyUpdate data) {
				if (data instanceof ControlPropertyInheritUpdate) {
					propertyInheritUpdate.apply(null);
					resetPropertyValueEditor();
				} else if (data instanceof ControlPropertyMacroUpdate) {
					ControlPropertyMacroUpdate macroUpdate = (ControlPropertyMacroUpdate) data;
					updateStackPanePropertyInputWithNewMode(macroUpdate.getNewMacro() != null ? ControlPropertyValueEditor.EditMode.MACRO : ControlPropertyValueEditor.EditMode.DEFAULT);
				} else if (data instanceof PreemptiveControlPropertyInheritUpdate) {
					// this is to prevent github issue https://github.com/kayler-renslow/arma-dialog-creator/issues/17
					// which is about the control property updating it's value to a type that the currentValueEditor()
					// isn't expecting and creating a class cast exception
					currentValueEditor().clearListeners();
				}
			}
		};
		controlProperty.getControlPropertyUpdateGroup().addListener(controlPropertyUpdateListener);

		miDisplayType.setOnAction(event -> {
			SerializableValue sv = controlProperty.getValue();
			String valueType = sv == null ? "`NULL`" : sv.getPropertyType().getDisplayName();
			MenuButtonPopup popup = new MenuButtonPopup(
					String.format(bundle.getString("type_f"), valueType)
			);
			popup.showPopup();
		});
		miResetToInitial.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				getControlProperty().setValue(getControlProperty().getDefaultValue());
			}
		});
		miDefaultEditor.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateStackPanePropertyInputWithNewMode(ControlPropertyValueEditor.EditMode.DEFAULT);
			}
		});
		miMacro.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateStackPanePropertyInputWithNewMode(ControlPropertyValueEditor.EditMode.MACRO);
			}
		});
		miConvert.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				PropertyType type;
				{
					ChooseNewPropertyTypeDialog dialog = new ChooseNewPropertyTypeDialog(controlProperty);
					dialog.show();
					type = dialog.getSelectedType();
				}
				if (type == null) {
					return;
				}
				SerializableValue value = controlProperty.getValue();
				if (value == null) {
					throw new IllegalStateException("shouldn't be able to convert a null value");
				}
				propertyValueEditor.clearListeners();
				try {
					controlProperty.setValue(
							SerializableValue.convert(ApplicationData.getManagerInstance(), value, type)
					);
				} catch (SerializableValueConversionException e) {
					ConvertValueDialog convertDialog = new ConvertValueDialog(value, type,
							ApplicationData.getManagerInstance().getGlobalExpressionEnvironment());
					convertDialog.show();
					SerializableValue newValue = convertDialog.getConvertedValue();
					if (!convertDialog.wasCancelled()) {
						controlProperty.setValue(newValue);
					}
				}
				resetPropertyValueEditor();
			}
		});
		inheritanceMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//if the inherited property type doesn't match the current editor type, there will be an exception
				propertyValueEditor.clearListeners();

				if (getControlProperty().isInherited()) {
					controlClass.overrideProperty(getControlProperty().getPropertyLookup());
				} else {
					boolean inherited = controlClass.inheritProperty(getControlProperty().getPropertyLookup());
					if (!inherited) {
						MenuButtonPopup popup = new MenuButtonPopup(bundle.getString("nothing_to_inherit"));
						popup.showPopup();
					}
				}
				resetPropertyValueEditor();
				propertyInheritUpdate.apply(null);
			}
		});
		miClearValue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (getControlProperty().getValue() == null) {
					propertyValueEditor.clearListeners();
					resetPropertyValueEditor();
					// This is necessary because a value editor may have a null value and the value editor could be
					// the wrong one due to an import error.
					//
					// So, if the value is null, this will reset the editor
					return;
				}
				if (getControlProperty().getMacro() != null) {
					if (!askClearMacro()) {
						return;
					}
				}

				SimpleResponseDialog dialog = new SimpleResponseDialog(
						ArmaDialogCreator.getPrimaryStage(),
						bundle.getString("ClearValuePopup.popup_title"),
						bundle.getString("ClearValuePopup.body"), true, true, false
				);
				dialog.setResizable(false);
				ResourceBundle appBundle = Lang.ApplicationBundle();
				dialog.getFooter().getBtnCancel().setText(appBundle.getString("Confirmation.no"));
				dialog.getFooter().getBtnOk().setText(appBundle.getString("Confirmation.yes"));
				dialog.setStageSize(300, 120);
				dialog.show();
				if (dialog.wasCancelled()) {
					return;
				}
				getControlProperty().setValue((SerializableValue) null);
			}
		});
		miRaw.setSelected(controlProperty.getValue() instanceof SVRaw);
		miRaw.setOnAction(event -> {
			propertyValueEditor.clearListeners();
			if (miRaw.isSelected()) {
				String val = controlProperty.getValue() == null ? "" : controlProperty.getValue().toString();
				controlProperty.setValue(new SVRaw(val, controlProperty.getPropertyType()));
			} else {
				if (controlProperty.getValue() instanceof SVRaw) {
					SVRaw raw = (SVRaw) controlProperty.getValue();
					try {
						controlProperty.setValue(raw.newSubstituteTypeValue(ApplicationData.getManagerInstance()));
					} catch (Exception e) {
						PropertyType newType = raw.getSubstituteType() == null ?
								controlProperty.getInitialPropertyType() :
								raw.getSubstituteType();
						ConvertValueDialog dialog = new ConvertValueDialog(raw, newType,
								ApplicationData.getManagerInstance().getGlobalExpressionEnvironment()
						);
						dialog.show();
						SerializableValue newValue = dialog.getConvertedValue();
						if (dialog.wasCancelled()) {
							//set the menuItem's selected state back to its previous state
							miRaw.setSelected(true);
							return;
						}
						controlProperty.setValue(newValue);
					}
				}
			}
			resetPropertyValueEditor();
		});
		if (controlProperty.getPropertyLookup().getOptions() != null) {
			menuButtonOptions.getItems().remove(miRaw);
		}

		if (controlProperty.getPropertyLookup() == ControlPropertyLookup.TYPE) {
			currentValueEditor().disableEditing(true);
			menuButtonOptions.getItems().clear();
			MenuItem miNoEdit = new MenuItem(bundle.getString("type_immutable"));
			miNoEdit.setDisable(true);
			menuButtonOptions.getItems().add(miNoEdit);
		}

		updateEditMode();
		propertyInheritUpdate.apply(null);
	}

	private void updateContainerInheritanceTint() {
		if (controlProperty.isInherited()) {
			if (stackPaneTint == null) {
				stackPaneTint = new StackPane();
				stackPaneTint.setStyle("-fx-background-color:rgba(0, 132, 180, 0.23)");
				stackPanePropertyInput.getChildren().add(stackPaneTint);
			} else {
				if (!stackPanePropertyInput.getChildren().contains(stackPaneTint)) {
					stackPanePropertyInput.getChildren().add(stackPaneTint);
				}
			}
		} else {
			stackPanePropertyInput.getChildren().removeIf(node -> node == stackPaneTint);
			stackPaneTint = null;
		}
	}

	/**
	 Will invoke {@link #updateStackPanePropertyInputWithNewMode(ControlPropertyValueEditor.EditMode)}
	 based upon the current property's value and macro
	 */
	private void updateEditMode() {
		if (getControlProperty().getMacro() != null) {
			updateStackPanePropertyInputWithNewMode(ControlPropertyValueEditor.EditMode.MACRO);
		} else {
			updateStackPanePropertyInputWithNewMode(ControlPropertyValueEditor.EditMode.DEFAULT);
		}
	}

	/**
	 Will update the {@link #stackPanePropertyInput}
	 to present the correct editor ({@link #currentValueEditor()} or a Macro editor).

	 @param mode the mode to set to
	 */
	@SuppressWarnings("unchecked")
	private void updateStackPanePropertyInputWithNewMode(@NotNull ControlPropertyValueEditor.EditMode mode) {
		if (mode == ControlPropertyValueEditor.EditMode.MACRO) {
			MacroGetterButton<? extends SerializableValue> macroGetterButton = new MacroGetterButton(currentValueEditor().getMacroPropertyType(), currentValueEditor().getControlProperty().getMacro());

			macroGetterButton.getChosenMacroValueObserver().updateValue(getControlProperty().getMacro());

			stackPanePropertyInput.getChildren().set(0, macroGetterButton);
			macroGetterButton.getChosenMacroValueObserver().addListener(new ValueListener() {
				@Override
				public void valueUpdated(@NotNull ValueObserver observer, Object oldValue, Object newValue) {
					Macro m = (Macro) newValue;
					currentValueEditor().getControlProperty().setValueToMacro(m);
				}
			});
		} else {
			if (controlProperty.getMacro() != null) {
				if (!askClearMacro()) {
					return;
				}
				controlProperty.setValueToMacro(null);
			}
			stackPanePropertyInput.getChildren().set(0, currentValueEditor().getRootNode());
			currentValueEditor().setToMode(mode);
		}
	}

	private boolean askClearMacro() {
		SimpleResponseDialog dialog = new SimpleResponseDialog(
				ArmaDialogCreator.getPrimaryStage(),
				bundle.getString("RemoveMacroDialog.dialog_title"),
				bundle.getString("RemoveMacroDialog.body"),
				true, true, false
		);
		dialog.getFooter().getBtnCancel().setText(bundle.getString("Confirmation.no"));
		dialog.getFooter().getBtnOk().setText(bundle.getString("Confirmation.yes"));
		dialog.show();
		return !dialog.wasCancelled();
	}

	public void unlink() {
		propertyValueEditor.clearListeners();
		controlClass.getControlClassUpdateGroup().removeListener(this.controlClassUpdateListener);
		getControlProperty().getControlPropertyUpdateGroup().removeListener(this.controlPropertyUpdateListener);
	}

	public void link() {
		propertyValueEditor.initListeners();
		propertyValueEditor.refresh();
		controlClass.getControlClassUpdateGroup().addListener(this.controlClassUpdateListener);
		getControlProperty().getControlPropertyUpdateGroup().addListener(this.controlPropertyUpdateListener);
		updateEditMode();
	}

	@NotNull
	public ControlPropertyValueEditor currentValueEditor() {
		return propertyValueEditor;
	}

	/**
	 Resets the the current value editor. The new value editor will be what {@link #constructNewPropertyValueEditor()}
	 returns.
	 <p>
	 Before invoking this method, be sure to clear the listeners of {@link #currentValueEditor()} with
	 {@link ControlPropertyValueEditor#clearListeners()}.
	 */
	private void resetPropertyValueEditor() {
		if (propertyValueEditor != null) {
			propertyValueEditor.clearListeners();
		}
		propertyValueEditor = constructNewPropertyValueEditor();
		HBox.setHgrow(stackPanePropertyInput, propertyValueEditor.displayFullWidth() ?
				Priority.ALWAYS : Priority.NEVER
		);
		stackPanePropertyInput.getChildren().setAll(propertyValueEditor.getRootNode());
		if (controlProperty.getValue() instanceof SVRaw) {
			if (controlProperty.getPropertyLookup().getOptions() == null) {
				stackPanePropertyInput.setPadding(new Insets(1));
				stackPanePropertyInput.setBorder(
						new Border(
								new BorderStroke(
										Color.DARKORANGE,
										BorderStrokeStyle.DASHED,
										CornerRadii.EMPTY,
										BorderStroke.THIN
								)
						)
				);
			}
		} else {
			stackPanePropertyInput.setBorder(null);
			stackPanePropertyInput.setPadding(Insets.EMPTY);
		}

		updateEditMode();
		updateContainerInheritanceTint();
	}

	/**
	 Constructs a new {@link ControlPropertyValueEditor}. If the {@link #controlProperty}'s value is null,
	 the editor that will be returned is one relevant for {@link ControlProperty#getInitialPropertyType()}

	 @return node that holds the controls to input data.
	 */
	@NotNull
	private ControlPropertyValueEditor constructNewPropertyValueEditor() {
		ControlPropertyLookupConstant lookup = controlProperty.getPropertyLookup();
		if (lookup.getOptions() != null && lookup.getOptions().length > 0) {
			return new ControlPropertyOptionEditor(controlClass, controlProperty);
		}
		PropertyType propertyType = controlProperty.getPropertyType() == null ? controlProperty.getInitialPropertyType() : controlProperty.getPropertyType();
		switch (propertyType) {
			case Int:
				return new IntegerEditor(controlClass, controlProperty);
			case Float:
				return new FloatEditor(controlClass, controlProperty);
			case ControlStyle:
				return new ControlStyleEditor(controlClass, controlProperty);
			case Boolean:
				return new BooleanChoiceBoxEditor(controlClass, controlProperty);
			case String:
				return new StringEditor(controlClass, controlProperty);
			case Array:
				return new ArrayEditor(controlClass, controlProperty);
			case Color:
				return new ColorArrayEditor(controlClass, controlProperty);
			case Sound:
				return new SoundEditor(controlClass, controlProperty);
			case Font:
				return new FontChoiceBoxEditor(controlClass, controlProperty);
			case FileName:
				return new FileNameEditor(controlClass, controlProperty);
			case Image:
				return new ImageEditor(controlClass, controlProperty);
			case HexColorString:
				return new HexColorEditor(controlClass, controlProperty);
			case Texture:
				return new StringEditor(controlClass, controlProperty);
			case SQF:
				return new SQFEditor(controlClass, controlProperty);
			case Raw:
				return new RawEditor(controlClass, controlProperty);
		}
		throw new IllegalStateException("Should have made a match");
	}

	@NotNull
	public ControlProperty getControlProperty() {
		return controlProperty;
	}

	public void hideIfInherited(boolean hide) {
		this.hideIfInherited = hide;
		boolean visible = !(hide && getControlProperty().isInherited());
		setVisible(visible);
		setManaged(visible);
	}

	private Tooltip getTooltip() {
		String tooltip = lookupDocProvider.getDocumentation(this.controlProperty);
		StringBuilder sb = new StringBuilder(tooltip.length());
		int len = 0;
		for (int i = 0; i < tooltip.length(); i++) {
			char c = tooltip.charAt(i);
			sb.append(c);
			len++;
			if (len >= 60 && Character.isWhitespace(c)) {
				len = 0;
				sb.append('\n');
			}
		}
		Tooltip tp = new Tooltip(sb.toString());
		tp.setFont(TOOLTIP_FONT);
		return tp;
	}

	/**
	 Places tooltip on n

	 @param n Node to place tooltip on
	 */
	private void placeTooltip(@NotNull Node n) {
		Tooltip.install(n, getTooltip());
	}


	private static class ChooseNewPropertyTypeDialog extends StageDialog<VBox> {

		private final ComboBox<PropertyType> comboBoxType = new ComboBox<>();

		public ChooseNewPropertyTypeDialog(@NotNull ControlProperty property) {
			super(ArmaDialogCreator.getPrimaryStage(), new VBox(10), bundle.getString("ConvertValueDialog.popup_title"), true, true, false);

			Label lbl = new Label(String.format(bundle.getString("ConvertValueDialog.body_f"), property.getName()));
			lbl.setWrapText(true);
			myRootElement.getChildren().add(lbl);

			PropertyType fromType;
			if (property.getValue() == null) {
				fromType = property.getInitialPropertyType();
			} else {
				fromType = property.getPropertyType();
			}

			for (PropertyType type : PropertyType.values()) {
				if (SerializableValue.isConvertible(fromType, type)) {
					comboBoxType.getItems().add(type);
				}
			}
			myRootElement.getChildren().add(comboBoxType);
			comboBoxType.getSelectionModel().select(property.getPropertyType());

			Button btnUseInitial = new Button(bundle.getString("ConvertValueDialog.use_initial"));
			btnUseInitial.setOnAction((e) -> comboBoxType.getSelectionModel().select(property.getInitialPropertyType()));
			myRootElement.getChildren().add(btnUseInitial);
			btnUseInitial.setTooltip(new Tooltip(bundle.getString("ConvertValueDialog.use_initial_tooltip")));


			setStageSize(420, 180);
			setResizable(false);
		}

		@Nullable
		public PropertyType getSelectedType() {
			return wasCancelled() ? null : comboBoxType.getValue();
		}
	}

	private class MenuButtonPopup extends Popup {
		public MenuButtonPopup(@NotNull String text) {
			Label lbl = new Label(text);
			StackPane container = new StackPane(lbl);
			container.setBackground(new Background(new BackgroundFill(
					Color.DODGERBLUE, CornerRadii.EMPTY, Insets.EMPTY)
			));
			lbl.setFont(Font.font(15));
			lbl.setTextFill(Color.WHITE);
			container.setPadding(new Insets(4));
			this.getContent().add(container);
		}

		public void showPopup() {
			Control ownerNode = menuButtonOptions;
			Point2D p = ownerNode.localToScreen(0, -ownerNode.getHeight());
			this.setAutoHide(true);
			show(ownerNode, p.getX(), p.getY());
		}
	}

}
