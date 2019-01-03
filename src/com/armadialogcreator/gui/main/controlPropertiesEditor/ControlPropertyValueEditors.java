package com.armadialogcreator.gui.main.controlPropertiesEditor;

import com.armadialogcreator.ArmaDialogCreator;
import com.armadialogcreator.control.*;
import com.armadialogcreator.control.sv.*;
import com.armadialogcreator.gui.fxcontrol.inputfield.ExpressionChecker;
import com.armadialogcreator.gui.fxcontrol.inputfield.InputFieldDataChecker;
import com.armadialogcreator.gui.fxcontrol.inputfield.RawChecker;
import com.armadialogcreator.lang.Lang;
import com.armadialogcreator.util.ReadOnlyValueListener;
import com.armadialogcreator.util.ReadOnlyValueObserver;
import com.armadialogcreator.util.ValueListener;
import com.armadialogcreator.util.ValueObserver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 All editors for {@link ControlPropertiesEditorPane}

 @author Kayler
 @since 01/29/2017 */
class ControlPropertyValueEditors {
	/** Used for when a set amount of options are available (uses radio button group for option selecting) */
	static class ControlPropertyOptionEditor extends FlowPane implements ControlPropertyValueEditor {
		private final ControlProperty controlProperty;
		private ToggleGroup toggleGroup;
		private List<RadioButton> radioButtons;
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				setEditorValue(newValue);
			}
		};

		private final ChangeListener<? super Toggle> toggleGroupListener = new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (newValue == null) {
					controlProperty.setValue((SerializableValue) null);
				} else {
					controlProperty.setValue(new SVRaw(newValue.getUserData().toString(), controlProperty.getInitialPropertyType()));
				}

			}
		};

		ControlPropertyOptionEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			super(10, 5);
			setAlignment(Pos.CENTER_LEFT);
			setPadding(new Insets(2));
			setPrefWrapLength(0); //wrap whenever

			this.controlProperty = controlProperty;
			ControlPropertyLookup lookup = (ControlPropertyLookup) controlProperty.getPropertyLookup();
			toggleGroup = new ToggleGroup();
			RadioButton radioButton, toSelect = null;
			boolean validData = controlProperty.getValue() != null;
			if (lookup.getOptions() == null) {
				throw new IllegalStateException("options shouldn't be null");
			}
			radioButtons = new ArrayList<>(lookup.getOptions().length);
			for (ControlPropertyOption option : lookup.getOptions()) {
				if (option == null) {
					throw new IllegalStateException("option shouldn't be null");
				}
				radioButton = new RadioButton(option.displayName);
				radioButton.setUserData(option.value);
				radioButton.setTooltip(new Tooltip(option.description));
				radioButton.setToggleGroup(toggleGroup);
				getChildren().add(radioButton);
				radioButtons.add(radioButton);
				if (validData && controlProperty.getValue().toString().equals(option.value)) {
					toSelect = radioButton;
				}
			}

			if (toSelect != null) {
				toggleGroup.selectToggle(toSelect);
			}
			initListeners();
		}

		private void setEditorValue(@Nullable SerializableValue newValue) {
			if (newValue == null) {
				toggleGroup.selectToggle(null);
				return;
			}
			for (Toggle toggle : toggleGroup.getToggles()) {
				if (controlProperty.getValue() == null) {
					continue;
				}
				if (toggle.getUserData().equals(controlProperty.getValue().toString())) {
					toggleGroup.selectToggle(toggle);
					return;
				}
			}
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
			getChildren().clear();
			if (mode == EditMode.DEFAULT) {
				getChildren().addAll(radioButtons);
			}
		}

		@NotNull
		@Override
		public Node getRootNode() {
			return this;
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Raw;
		}

		@Override
		public void clearListeners() {
			toggleGroup.selectedToggleProperty().removeListener(toggleGroupListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			toggleGroup.selectedToggleProperty().addListener(toggleGroupListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setEditorValue(controlProperty.getValue());
		}

		@Override
		public boolean displayFullWidth() {
			return true;
		}
	}

	static class FileNameEditor extends FileNameValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVFileName> editorValueListener = new ReadOnlyValueListener<SVFileName>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVFileName> observer, SVFileName oldValue, SVFileName newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, SerializableValue oldValue, SerializableValue newValue) {
				setValue((SVFileName) newValue);
			}
		};

		public FileNameEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;
			setValue((SVFileName) controlProperty.getValue());
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.ControlStyle;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVFileName) controlProperty.getValue());
		}
	}

	static class ControlStyleEditor extends ControlStyleValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVControlStyleGroup> editorValueListener = new ReadOnlyValueListener<SVControlStyleGroup>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVControlStyleGroup> observer, SVControlStyleGroup oldValue, SVControlStyleGroup newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, SerializableValue oldValue, SerializableValue newValue) {
				setValue((SVControlStyleGroup) newValue);
			}
		};

		public ControlStyleEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			if (control.getSpecProvider() instanceof AllowedStyleProvider) {
				AllowedStyleProvider specProvider = (AllowedStyleProvider) control.getSpecProvider();
				ArrayList<ControlStyle> moveAfterSeparator = new ArrayList<>();
				menuButton.getItems().clear();
				ControlStyle[] allowedStyles = specProvider.getAllowedStyles();
				for (ControlStyle style : ControlStyle.values()) {
					boolean match = false;
					for (ControlStyle allowed : allowedStyles) {
						if (style == allowed) {
							menuButton.getItems().add(allowed);
							match = true;
							continue;
						}
					}
					if (!match) {
						moveAfterSeparator.add(style);
					}
				}
				menuButton.addMenu(Lang.ApplicationBundle().getString("ControlPropertyValueEditors.unused_styles"), moveAfterSeparator);

				for (ControlStyle style : menuButton.getItems()) {
					menuButton.bindTooltip(style, style.documentation);
				}
			}
			this.controlProperty = controlProperty;
			setValue((SVControlStyleGroup) controlProperty.getValue());
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public Node getRootNode() {
			return this;
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.ControlStyle;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVControlStyleGroup) controlProperty.getValue());
		}
	}

	/**
	 Used for when the input is in a text field. The InputField class also allows for input verifying so that if something entered is wrong, the user will be notified.
	 Used for {@link SVDouble}, {@link SVInteger}, {@link SVString}, {@link SVExpression}
	 */
	@SuppressWarnings("unchecked")
	private static abstract class InputFieldEditor<C extends SerializableValue> extends InputFieldValueEditor<C> implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final PropertyType macroType;
		private final ReadOnlyValueListener<C> editorValueListener = new ReadOnlyValueListener<C>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<C> observer, C oldValue, C newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				if (controlProperty.getValue() != null) {
					inputField.setToButton(false);
					inputField.setValue((C) controlProperty.getValue());
				} else {
					setValue(null);
				}
			}
		};

		InputFieldEditor(@NotNull PropertyType macroType, @NotNull ControlClass control, @NotNull ControlProperty
				controlProperty, InputFieldDataChecker checker, @Nullable String promptText) {
			super(checker);
			this.macroType = macroType;

			this.controlProperty = controlProperty;

			inputField.setValue((C) controlProperty.getValue());
			if (promptText != null) {
				inputField.setPromptText(promptText);
			}
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return macroType;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((C) controlProperty.getValue());
		}
	}

	static class StringEditor extends InputFieldEditor<SVString> {
		StringEditor(ControlClass control, ControlProperty controlProperty) {
			super(PropertyType.String, control, controlProperty, new SVArmaStringChecker(),
					Lang.LookupBundle().getString("PropertyType.string"));
		}
	}

	static class FloatEditor extends InputFieldEditor<SVExpression> {
		FloatEditor(ControlClass control, ControlProperty controlProperty) {
			super(PropertyType.Float, control, controlProperty,
					new ExpressionChecker(ArmaDialogCreator.getApplicationData().getGlobalExpressionEnvironment(), ExpressionChecker.TYPE_FLOAT),
					Lang.LookupBundle().getString("PropertyType.float")
			);
		}
	}

	static class IntegerEditor extends InputFieldEditor<SVExpression> {
		IntegerEditor(ControlClass control, ControlProperty controlProperty) {
			super(PropertyType.Int, control, controlProperty,
					new ExpressionChecker(ArmaDialogCreator.getApplicationData().getGlobalExpressionEnvironment(), ExpressionChecker.TYPE_INT),
					Lang.LookupBundle().getString("PropertyType.int")
			);
		}
	}

	static class RawEditor extends InputFieldEditor<SVRaw> {
		RawEditor(ControlClass control, ControlProperty controlProperty) {
			super(PropertyType.Raw, control, controlProperty,
					new RawChecker(controlProperty.getInitialPropertyType()),
					Lang.LookupBundle().getString("PropertyType.raw")
			);
		}
	}

	static class SQFEditor extends InputFieldEditor<SVString> {
		SQFEditor(ControlClass control, ControlProperty controlProperty) {
			super(PropertyType.SQF, control, controlProperty,
					new SQFDataChecker(),
					"SQF"
			);
		}
	}

	/**
	 Used for when control property requires color input.
	 Use this only when the ControlProperty's value is of type {@link SVColorArray}
	 */
	static class ColorArrayEditor extends ColorArrayValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVColorArray> valueEditorListener = new ReadOnlyValueListener<SVColorArray>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVColorArray> observer, SVColorArray oldValue, SVColorArray newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				setValue(((SVColorArray) controlProperty.getValue()));
			}
		};

		ColorArrayEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;
			SVColorArray value = (SVColorArray) controlProperty.getValue();
			setValue(value);
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Color;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(valueEditorListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(valueEditorListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVColorArray) controlProperty.getValue());
		}
	}

	/**
	 Used for when control property requires color input.
	 Use this only when the ControlProperty's value is of type {@link SVHexColor}
	 */
	static class HexColorEditor extends HexColorValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVHexColor> valueEditorListener = new ReadOnlyValueListener<SVHexColor>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVHexColor> observer, SVHexColor oldValue,
									 SVHexColor newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				setValue((SVHexColor) controlProperty.getValue());
			}
		};

		HexColorEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;
			boolean validData = controlProperty.getValue() != null;
			if (validData) {
				SVHexColor value = (SVHexColor) controlProperty.getValue();
				setValue(value);
			} else {
				setValue(null);
			}
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.HexColorString;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(valueEditorListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(valueEditorListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVHexColor) controlProperty.getValue());
		}
	}

	/**
	 Used for boolean control properties
	 Use this editor for when the ControlProperty's value is of type {@link SVBoolean}
	 */
	static class BooleanChoiceBoxEditor extends BooleanValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVBoolean> editorValueListener = new ReadOnlyValueListener<SVBoolean>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVBoolean> observer, SVBoolean oldValue, SVBoolean newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				choiceBox.setValue(newValue == null ? null : ((SVBoolean) newValue).isTrue());
			}
		};

		BooleanChoiceBoxEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;

			boolean validData = controlProperty.getValue() != null;
			if (validData) {
				choiceBox.setValue(controlProperty.getBooleanValue());
			}
			initListeners();

		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Boolean;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVBoolean) controlProperty.getValue());
		}
	}

	/**
	 Used for control properties that require more than one input
	 This editor will use {@link SVArray} as the ControlProperty's value type
	 */
	@SuppressWarnings("unchecked")
	static class ArrayEditor extends ArrayValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVArray> editorValueListener = new ReadOnlyValueListener<SVArray>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVArray> observer, SVArray oldValue, SVArray newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				setValue((SVArray) newValue);
			}
		};

		ArrayEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;

			setValue((SVArray) controlProperty.getValue());
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Array;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVArray) controlProperty.getValue());
		}
	}

	/**
	 Used for control property font picking
	 Used for ControlProperty instances where it's value is {@link SVFont}
	 */
	static class FontChoiceBoxEditor extends FontValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVFont> editorValueListener = new ReadOnlyValueListener<SVFont>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVFont> observer, SVFont oldValue, SVFont newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				comboBox.setValue((SVFont) controlProperty.getValue());
			}
		};

		FontChoiceBoxEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;

			setValue((SVFont) controlProperty.getValue());
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Font;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVFont) controlProperty.getValue());
		}
	}

	/**
	 Used for control property image editing
	 Used for ControlProperty instances where it's value is {@link SVImage}
	 */
	static class ImageEditor extends ImageValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVImage> editorValueListener = new ReadOnlyValueListener<SVImage>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVImage> observer, SVImage oldValue, SVImage newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				ImageEditor.this.setValue((SVImage) controlProperty.getValue());
			}
		};

		ImageEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;

			setValue((SVImage) controlProperty.getValue());
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Image;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVImage) controlProperty.getValue());
		}
	}

	/** Use this editor for {@link SVSound} ControlProperty values */
	static class SoundEditor extends SoundValueEditor implements ControlPropertyValueEditor {

		private final ControlProperty controlProperty;
		private final ReadOnlyValueListener<SVSound> editorValueListener = new ReadOnlyValueListener<SVSound>() {
			@Override
			public void valueUpdated(@NotNull ReadOnlyValueObserver<SVSound> observer, SVSound oldValue, SVSound newValue) {
				controlProperty.setValue(newValue);
			}
		};
		private final ValueListener<SerializableValue> controlPropertyListener = new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				setValue((SVSound) newValue);
			}
		};

		public SoundEditor(@NotNull ControlClass control, @NotNull ControlProperty controlProperty) {
			this.controlProperty = controlProperty;
			setValue((SVSound) controlProperty.getValue());
			initListeners();
		}

		@NotNull
		@Override
		public ControlProperty getControlProperty() {
			return controlProperty;
		}

		@Override
		public void setToMode(@NotNull EditMode mode) {
		}

		@NotNull
		@Override
		public PropertyType getMacroPropertyType() {
			return PropertyType.Sound;
		}

		@Override
		public void clearListeners() {
			getReadOnlyObserver().removeListener(editorValueListener);
			controlProperty.getValueObserver().removeListener(controlPropertyListener);
		}

		@Override
		public void initListeners() {
			getReadOnlyObserver().addListener(editorValueListener);
			controlProperty.getValueObserver().addListener(controlPropertyListener);
		}

		@Override
		public void refresh() {
			setValue((SVSound) controlProperty.getValue());
		}
	}
}
