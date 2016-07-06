package com.kaylerrenslow.armaDialogCreator.gui.fx.main.popup.newControl;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 @author Kayler
 Controller class for NewControlPopup
 Created on 07/06/2016. */
public class NewControlPopupController {

	@FXML
	public TextField tfClassName;
	@FXML
	public Button btnEditProperty;
	@FXML
	public Button btnRemoveProperty;
	@FXML
	public VBox vbProperties;
	@FXML
	public SplitMenuButton mbtnAddProperty;
	@FXML
	public MenuItem miAddCustomProperty;

	@FXML
	public TextArea taPreviewSample;

	@FXML
	public Button btnCancel;
	@FXML
	public Button btnOk;
//
//	@Override
//	public void initialize(URL location, ResourceBundle resources) {
//
//	}
}
