package com.armadialogcreator.control;

import com.armadialogcreator.expression.Env;
import org.jetbrains.annotations.NotNull;

/**
 @author Kayler
 @since 06/29/2017 */
public class TestArmaControlRenderer extends ArmaControlRenderer {

	public TestArmaControlRenderer(@NotNull ArmaControl control, @NotNull ArmaResolution resolution, @NotNull Env env) {
		super(control, resolution, env);
	}

	@Override
	public void requestRender() {

	}
}
