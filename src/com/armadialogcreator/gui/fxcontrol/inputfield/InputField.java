package com.armadialogcreator.gui.fxcontrol.inputfield;

import com.armadialogcreator.lang.Lang;
import com.armadialogcreator.util.ValueListener;
import com.armadialogcreator.util.ValueObserver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 Base class for a text field control that checks if the inputted data is correct and returns the data as an object with type V

 @author Kayler
 @since 05/31/2016. */
public class InputField<C extends InputFieldDataChecker<V>, V> extends StackPane {
	private static final String BAD_FIELD = "bad-input-text-field";
	private static final String DATA_NEEDS_SUBMITION = "-fx-background-color:green";
	private static final String DATA_SUBMITTED = "";

	private final C dataChecker;
	private final ValueObserver<V> observer = new ValueObserver<>(null);
	private final TextField textField = new TextField();
	private final Button button = new Button();
	private final ErrorMsgPopup errorMsgPopup = new ErrorMsgPopup(this);
	private final Button btnSubmit = new Button("");
	private final HBox hboxTextField = new HBox(2, textField, btnSubmit);

	private @Nullable String valueString = null;
	private boolean valid = false;
	private boolean buttonState = true;
	private String errMsg;
	private boolean isError;
	private boolean autoSubmit = false;

	/**
	 Creates a new InputField (TextField with additional features). The InputField has two states: "Input State" and "Button State".
	 <br><b>"Input State":</b>
	 <ul>
	 <li>A normal {@link TextField} is used as the underlying text input. Whatever {@link InputFieldDataChecker#getTypeName()} returns is what will be passed in {@link TextField#setPromptText(String)}</li>
	 <li>User can enter any text. When enter key is pressed, the text is checked to see if valid (via {@link InputFieldDataChecker#errorMsgOnData(String)}). If not valid, the text field will turn red
	 and an error popup will appear with the error message and the {@link ValueObserver} will not be notified of a value update.</li>
	 <li>If the inner TextField instance loses focus, the TextField has no data (no text), and {@link InputFieldDataChecker#allowEmptyData()} is false, and {@link InputFieldDataChecker#getDefaultValue()}==null, the text field will enter Button State.<br>
	 The scenario where {@link InputFieldDataChecker#getDefaultValue()}!=null, TextField has no data, and the TextField loses focus, the InputField will not enter the Button State and the
	 {@link ValueObserver#updateValue(Object)} will be set to {@link InputFieldDataChecker#getDefaultValue()}.
	 {@link InputFieldDataChecker#getDefaultValue()} will only be used for when the InputField/TextField loses focus.
	 If there can be no data (no text), the InputField will stay in Input State.</li>
	 </ul>
	 <br><b>"Button State":</b>
	 <ul>
	 <li>The Button State is used to show that no text has been inputted in the TextField and that the InputField can't have "" has valid input.</li>
	 <li>The InputField will become a {@link Button} and {@link InputFieldDataChecker#getTypeName()} will be the text inside the button. The user's mouse will become {@link Cursor#TEXT} when hovered over the button.
	 When the button is pressed, the InputField will enter Input State</li>
	 </ul>
	 The InputField will update it's value when:<br>
	 <ul>
	 <li>When submit button is pressed and the data is valid</li>
	 <li>Pressing the 'enter' key and the data is valid</li>
	 <li>no data entered and text field loses focus and data is not allowed to be empty</li>
	 </ul>

	 @param dataChecker the {@link InputFieldDataChecker} instance to use
	 */
	public InputField(@NotNull C dataChecker) {
		this.dataChecker = dataChecker;

		setAlignment(Pos.CENTER_LEFT);
		hboxTextField.setAlignment(Pos.CENTER_LEFT);

		HBox.setHgrow(textField, Priority.ALWAYS);
		btnSubmit.setPrefWidth(10d);
		btnSubmit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//do not update value observer since value may be invalid
				submitValue();
			}
		});
		btnSubmit.setTooltip(new Tooltip(Lang.FxControlBundle().getString("InputField.submit_btn_tooltip")));

		textField.setStyle("-fx-background-radius:0px");

		EventHandler<KeyEvent> keyEvent = new EventHandler<javafx.scene.input.KeyEvent>() {
			@Override
			public void handle(javafx.scene.input.KeyEvent event) {
				if (autoSubmit) {
					String t = textField.getText();
					if (dataChecker.errorMsgOnData(t) == null) {
						V v = dataChecker.parse(t);
						if (v != null && v.equals(observer.getValue())) {
							error(false);
							return;
						}
					}
					submitValue();
					return;
				}
				if (event.getCode() == KeyCode.ENTER) {
					//do not update value observer since value may be invalid
					submitValue();
				}
			}
		};
		this.textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (valueString != null && newValue.equals(valueString)) {
					valueSubmitted(true);
				} else {
					valueSubmitted(false);
				}
			}
		});
		setPromptText(dataChecker.getTypeName());
		setTooltip(new Tooltip(dataChecker.getTypeName()));
		this.setOnKeyReleased(keyEvent);
		this.setOnKeyTyped(keyEvent);

		textField.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (isError) {
					errorMsgPopup.showPopup();
				}
			}
		});
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean focused) {
				if (!focused && (getText().length() == 0 && !InputField.this.dataChecker.allowEmptyData())) {
					if (InputField.this.dataChecker.getDefaultValue() != null) {
						getValueObserver().updateValue(InputField.this.dataChecker.getDefaultValue());
					} else {
						clear();
					}
					return;
				}
				if (focused) {
					setToButton(false);
					return;
				}
			}
		});

		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setText("");
				setToButton(false);
				if (InputField.this.dataChecker.allowEmptyData()) { //show that the field can be submitted as is
					valueSubmitted(false);
				}
			}
		});

		button.setCursor(Cursor.TEXT);

		button.setMaxWidth(Double.MAX_VALUE);
		getChildren().add(button);

		observer.addListener(new ValueListener<V>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<V> observer, V oldValue, V newValue) {
				setValue(newValue);
			}
		});
	}

	/** If dataSubmitted==true, the submit button will turn to it's default color. if dataSubmitted==false, will turn to green to indicate changes need to be submitted */
	private void valueSubmitted(boolean dataSubmitted) {
		btnSubmit.setStyle(dataSubmitted ? DATA_SUBMITTED : DATA_NEEDS_SUBMITION);
	}

	/**
	 Creates an InputField with the value set to defaultValue

	 @see #InputField(InputFieldDataChecker)
	 */
	public InputField(@NotNull C fieldDataChecker, @Nullable V defaultValue) {
		this(fieldDataChecker);
		setValue(defaultValue);
	}

	public InputField(@NotNull C fieldDataChecker, @Nullable V defaultValue, boolean autoSubmit) {
		this(fieldDataChecker);
		this.autoSubmit = autoSubmit;
		setValue(defaultValue);
		if (autoSubmit) {
			hboxTextField.getChildren().remove(btnSubmit);
		}
	}

	/**
	 Get the text parsed and converted into type V.
	 This will only return whatever the generic type E outputs from {@link InputFieldDataChecker#parse(String)}.
	 If no text was inputted and the InputFieldDataChecker doesn't allow empty data, will return null.
	 Also, if the InputField is in Button State, will return null.
	 @return the parsed text
	 */
	@Nullable
	public V getValue() {
		if (buttonState) {
			return null;
		}
		if (!valid) {
			return null;
		}
		return dataChecker.parse(this.getText());
	}

	/** Return true if the data inside the text field is valid, false otherwise. If the InputField is in the Button State, will return false. */
	public boolean hasValidData() {
		return !buttonState && valid;
	}

	/**
	 Set the value from an object. The text in the control is set to whatever {@link Object#toString()} returns. If value given is null, will set field to Button State.
	 No matter what is passed, the internal {@link ValueObserver} instance will be notified of the value update.
	 */
	public void setValue(@Nullable V value) {
		observer.updateValue(value);
		if (value == null) {
			clear();
			valid = false;
			return;
		}
		valueString = value.toString();
		this.setText(valueString);
		valueSubmitted(true);
		setToButton(false);
		valid = true;
		error(false);
	}

	/**
	 Set the value from text. If text==null, the InputField will be set to the Button State and the InputField's {@link ValueObserver#getValue()} will be null.<br>
	 If text != null, the text will be checked to see if valid (via {@link InputFieldDataChecker#errorMsgOnData(String)}.<br>
	 If the text is valid, the value will be set to whatever {@link InputFieldDataChecker#parse(String)} returns with text passed as the parameter.<br>
	 If the text is not valid, the inner {@link TextField} instance will turn red and a popup will appear with the error message and the {@link ValueObserver} will update value to null
	 */
	public void setValueFromText(@Nullable String text) {
		setValueFromText(text, true, true);
	}

	private void setValueFromText(@Nullable String text, boolean updateCursor, boolean updateValueIfNotValid) {
		if (text == null) {
			setValue(null);
			return;
		}
		checkIfValid(text);
		int cursorPosition = textField.getCaretPosition();
		IndexRange selection = textField.getSelection();
		if (valid) {
			setText(text);
			setValue(getValue());
		} else {
			valueString = null;
			setToButton(false);
			if (updateValueIfNotValid) {
				observer.updateValue(null);
			}
			error(true);
			setText(text);
		}
		if (updateCursor) {
			textField.positionCaret(cursorPosition);
			textField.selectRange(selection.getStart(), selection.getEnd());
		}
	}

	/** @see TextField#getCaretPosition() */
	public int getCaretPosition() {
		return textField.getCaretPosition();
	}

	/** @see TextField#getSelection() */
	public IndexRange getSelection() {
		return textField.getSelection();
	}

	/** @see TextField#positionCaret(int) */
	public void positionCaret(int position) {
		textField.positionCaret(position);
	}

	/** @see TextField#selectRange(int, int) */
	public void selectRange(int start, int end) {
		textField.selectRange(start, end);
	}

	/** @see TextField#selectAll() */
	public void selectAll() {
		textField.selectAll();
	}

	/** @return the value observer */
	@NotNull
	public ValueObserver<V> getValueObserver() {
		return observer;
	}

	/** Clears the text, the error (also hides the error popup), and sets the value to null. Then sets the InputField to Button State. */
	public void clear() {
		valueString = null;
		valueSubmitted(true);
		setText("");
		observer.updateValue(null);
		error(false);
		setToButton(true);
	}

	/** Sets the text without updating the error state or value observer. If text is null, will be put into button state. */
	public void setText(@Nullable String text) {
		if (text == null) {
			clear();
		} else {
			textField.setText(text);
		}
	}

	/** Get the inputted text, or empty String if in Button State (may also not be in Button State and the text is actually just ""). */
	@NotNull
	public String getText() {
		if (buttonState) {
			return "";
		}
		if (textField.getText() == null) {
			textField.setText("");
		}
		return textField.getText();
	}

	/** Set the prompt text of the text field and the text of the button */
	public void setPromptText(String text) {
		textField.setPromptText(text);
		button.setText(text);
	}

	/** Set the tooltip for the inner {@link TextField} and {@link Button} */
	public void setTooltip(Tooltip tooltip) {
		textField.setTooltip(tooltip);
		button.setTooltip(tooltip);
	}

	private boolean checkIfValid(@NotNull String text) {
		errMsg = dataChecker.errorMsgOnData(text);
		valid = (errMsg == null);
		return valid;
	}

	/** Set the state of the InputField. if toButton==true, will set to Button state. If toButton == false, will set to TextField state. */
	public void setToButton(boolean toButton) {
		if (buttonState == toButton) {
			return;
		}
		buttonState = toButton;
		if (toButton) {
			getChildren().remove(hboxTextField);
			getChildren().add(button);
		} else {
			getChildren().remove(button);
			getChildren().add(hboxTextField);
			textField.requestFocus();
		}
	}

	@Override
	public void requestFocus() {
		if (buttonState) {
			button.requestFocus();
		} else {
			textField.requestFocus();
		}
	}

	/**
	 If e==true:
	 <ul>
	 <li>set text field background color to red</li>
	 <li>show error popup with message</li>
	 </ul>
	 If e==false:
	 <ul>
	 <li>set text field background color to normal</li>
	 <li>hide error popup</li>
	 </ul>
	 */
	private void error(boolean e) {
		this.isError = e;
		if (e) {
			textField.getStyleClass().add(BAD_FIELD);
			errorMsgPopup.setMsg(errMsg);
			errorMsgPopup.showPopup();
		} else {
			textField.getStyleClass().removeAll(BAD_FIELD);
			errorMsgPopup.hide();
		}

		this.applyCss();
	}

	/** A programmatically way of submitting the data (will do the same operations as actually pressing the submit button) */
	public void submitValue() {
		setValueFromText(getText(), true, false);
	}

	/**
	 Set the graphic, which appears on the left side of the text field

	 @param graphic the graphic, or null to remove it
	 */
	public void setGraphic(@Nullable Node graphic) {
		if (hboxTextField.getChildren().get(0) == textField) {
			if (graphic == null) {
				return;
			}
			hboxTextField.getChildren().add(0, graphic);
		} else {
			if (graphic == null) {
				hboxTextField.getChildren().remove(0);
				return;
			}
			hboxTextField.getChildren().set(0, graphic);
		}

	}


	private static class ErrorMsgPopup extends Popup {
		private final InputField inputField;
		private Label lblMsg = new Label();

		public ErrorMsgPopup(@NotNull InputField inputField) {
			this.inputField = inputField;
			StackPane stackPane = new StackPane(lblMsg);
			stackPane.setPadding(new Insets(5));
			stackPane.setAlignment(Pos.BOTTOM_CENTER);
			stackPane.setStyle("-fx-background-color:red;");
			lblMsg.setTextFill(Color.WHITE);
			this.setAutoHide(true);

			getContent().add(stackPane);
		}

		public void setMsg(@NotNull String msg) {
			this.lblMsg.setText(msg);
		}

		public void showPopup() {
			Point2D p = inputField.textField.localToScreen(0, -inputField.textField.getHeight());
			show(inputField, p.getX(), p.getY());
		}
	}
}
