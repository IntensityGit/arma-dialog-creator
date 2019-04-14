package com.armadialogcreator.gui.main.actions.mainMenu.view;

import com.armadialogcreator.gui.main.popup.CanvasViewColorsPopup;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 Created by Kayler on 05/20/2016.
 */
public class ViewColorsAction implements EventHandler<ActionEvent>{
	@Override
	public void handle(ActionEvent event) {
		CanvasViewColorsPopup popup = new CanvasViewColorsPopup();
		popup.show();
	}
}
