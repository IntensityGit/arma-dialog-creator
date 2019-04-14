package com.armadialogcreator.gui.fxcontrol;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

/**
 Created by Kayler on 10/22/2016.
 */
public class SimpleImageContainer extends ImageView implements ImageContainer {
	public SimpleImageContainer() {
	}

	public SimpleImageContainer(String url) {
		super(url);
	}

	public SimpleImageContainer(Image image) {
		super(image);
	}

	@NotNull
	@Override
	public ImageContainer copy() {
		return new SimpleImageContainer(getImage());
	}

	@NotNull
	@Override
	public Node getNode() {
		return this;
	}
}
