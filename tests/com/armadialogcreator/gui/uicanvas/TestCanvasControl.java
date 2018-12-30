package com.armadialogcreator.gui.uicanvas;

import com.armadialogcreator.util.DataContext;
import com.armadialogcreator.util.UpdateListenerGroup;
import com.armadialogcreator.util.ValueObserver;
import org.jetbrains.annotations.NotNull;

/**
 @author Kayler
 @since 06/18/2017 */
public class TestCanvasControl extends SimpleCanvasComponent implements CanvasControl<TestCanvasControl> {
	private final ValueObserver<CanvasDisplay<TestCanvasControl>> displayObserver = new ValueObserver<>(null);
	private final ValueObserver<ControlHolder<TestCanvasControl>> holderObserver = new ValueObserver<>(null);
	private String name;

	/**
	 @param name a unique name for the control to be identified in a test
	 */
	public TestCanvasControl(@NotNull String name) {
		super(0, 0, 0, 0);
		this.name = name;
	}

	@NotNull
	@Override
	public CanvasComponent getRenderer() {
		return this;
	}

	@Override
	public void resolutionUpdate(@NotNull Resolution newResolution) {

	}

	@NotNull
	@Override
	public ValueObserver<CanvasDisplay<TestCanvasControl>> getDisplayObserver() {
		return displayObserver;
	}

	@NotNull
	@Override
	public ValueObserver<ControlHolder<TestCanvasControl>> getHolderObserver() {
		return holderObserver;
	}

	@NotNull
	@Override
	public UpdateListenerGroup<TestCanvasControl> getRenderUpdateGroup() {
		return null;
	}

	@NotNull
	@Override
	public DataContext getUserData() {
		return null;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "TestCanvasControl{" +
				"name='" + name + '\'' +
				'}';
	}
}
