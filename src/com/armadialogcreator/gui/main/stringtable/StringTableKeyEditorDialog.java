package com.armadialogcreator.gui.main.stringtable;

import com.armadialogcreator.arma.stringtable.StringTable;
import com.armadialogcreator.arma.stringtable.StringTableKey;
import com.armadialogcreator.gui.popup.StageDialog;
import com.armadialogcreator.main.ArmaDialogCreator;
import com.armadialogcreator.main.Lang;
import javafx.geometry.Orientation;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

/**
 @author Kayler
 @since 12/29/2016 */
public class StringTableKeyEditorDialog extends StageDialog<VBox> {
	private final StringTableKey copyKey;
	private StringTableKey editKey;

	public StringTableKeyEditorDialog(@NotNull StringTable ownerTable, @NotNull StringTableKey editKey) {
		super(
				ArmaDialogCreator.getPrimaryStage(),
				new VBox(5),
				String.format(Lang.ApplicationBundle().getString("Popups.StringTableKeyEditor.popup_title_f"), editKey.getId()),
				true, true, false
		);

		this.editKey = editKey;
		copyKey = editKey.deepCopy();
		setResizable(false);
		StringTableKeyEditorPane editorPane = new StringTableKeyEditorPane(ownerTable, copyKey.getDefaultLanguage());
		editorPane.getPaneContent().setPrefHeight(200);
		editorPane.getPaneContent().setOrientation(Orientation.VERTICAL);
		editorPane.setKey(copyKey, ownerTable.getKeys());
		myRootElement.getChildren().add(editorPane);
	}

	@Override
	protected void ok() {
		editKey.setTo(copyKey);
		super.ok();
	}
}
