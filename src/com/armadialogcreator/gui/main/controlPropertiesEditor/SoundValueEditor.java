package com.armadialogcreator.gui.main.controlPropertiesEditor;

import com.armadialogcreator.core.sv.SVSound;
import com.armadialogcreator.gui.fxcontrol.inputfield.ArmaStringChecker;
import com.armadialogcreator.gui.fxcontrol.inputfield.DoubleChecker;
import com.armadialogcreator.gui.fxcontrol.inputfield.InputField;
import com.armadialogcreator.lang.Lang;
import com.armadialogcreator.util.ReadOnlyValueObserver;
import com.armadialogcreator.util.ValueListener;
import com.armadialogcreator.util.ValueObserver;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ResourceBundle;

/**
 Created by Kayler on 07/13/2016.
 */
public class SoundValueEditor implements ValueEditor<SVSound> {

	protected InputField<ArmaStringChecker, String> inSoundName = new InputField<>(new ArmaStringChecker());
	protected InputField<DoubleChecker, Double> inDb = new InputField<>(new DoubleChecker());
	protected InputField<DoubleChecker, Double> inPitch = new InputField<>(new DoubleChecker());
	private final GridPane gridPaneEditors = new GridPane();

	private final StackPane masterPane = new StackPane(gridPaneEditors);
	private final ValueObserver<SVSound> valueObserver = new ValueObserver<>(null);

	public SoundValueEditor() {
		ResourceBundle bundle = Lang.ApplicationBundle();

		gridPaneEditors.addRow(0, new Label(bundle.getString("ValueEditors.SoundValueEditor.sound_name")), inSoundName);
		gridPaneEditors.addRow(1, new Label(bundle.getString("ValueEditors.SoundValueEditor.db") + " "), inDb);
		gridPaneEditors.addRow(2, new Label(bundle.getString("ValueEditors.SoundValueEditor.pitch")), inPitch);
		gridPaneEditors.getColumnConstraints().add(new ColumnConstraints(-1, -1, Double.MAX_VALUE, Priority.NEVER, HPos.LEFT, true));
		gridPaneEditors.getColumnConstraints().add(new ColumnConstraints(-1, -1, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));

		inSoundName.getValueObserver().addListener(new ValueListener<String>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<String> observer, String oldValue, String newValue) {
				valueObserver.updateValue(createValue());
			}
		});
		inDb.getValueObserver().addListener(new ValueListener<Double>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<Double> observer, Double oldValue, Double newValue) {
				valueObserver.updateValue(createValue());
			}
		});
		inPitch.getValueObserver().addListener(new ValueListener<Double>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<Double> observer, Double oldValue, Double newValue) {
				valueObserver.updateValue(createValue());
			}
		});
	}

	@Override
	public void submitCurrentData() {
		inSoundName.submitValue();
		inDb.submitValue();
		inPitch.submitValue();
	}

	@Override
	public SVSound getValue() {
		return valueObserver.getValue();
	}

	@Nullable
	private SVSound createValue() {
		if (inSoundName.getValue() == null) {
			return null;
		}
		if (inDb.getValue() == null) {
			return null;
		}
		if (inPitch.getValue() == null) {
			return null;
		}
		return new SVSound(inSoundName.getValue(), inDb.getValue(), inPitch.getValue());
	}

	@Override
	public void setValue(@Nullable SVSound val) {
		if (val == null) {
			inSoundName.setValue(null);
			inDb.setValue(null);
			inPitch.setValue(null);
		} else {
			inSoundName.setValue(val.getSoundName());
			inDb.setValue(val.getDb());
			inPitch.setValue(val.getPitch());
		}
	}

	@Override
	public @NotNull Node getRootNode() {
		return masterPane;
	}

	@Override
	public void focusToEditor() {
		if (inSoundName.getValue() == null) {
			inSoundName.requestFocus();
		} else if (inDb.getValue() == null) {
			inDb.requestFocus();
		} else if (inPitch.getValue() == null) {
			inPitch.requestFocus();
		} else {
			inSoundName.requestFocus();
		}
	}

	@NotNull
	@Override
	public ReadOnlyValueObserver<SVSound> getReadOnlyObserver() {
		return valueObserver.getReadOnlyValueObserver();
	}

	@Override
	public boolean displayFullWidth() {
		return true;
	}
}
