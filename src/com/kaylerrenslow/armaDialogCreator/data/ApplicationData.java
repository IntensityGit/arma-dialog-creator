package com.kaylerrenslow.armaDialogCreator.data;

import com.kaylerrenslow.armaDialogCreator.arma.util.PositionCalculator;
import com.kaylerrenslow.armaDialogCreator.control.Macro;
import com.kaylerrenslow.armaDialogCreator.expression.Env;
import com.kaylerrenslow.armaDialogCreator.expression.SimpleEnv;
import com.kaylerrenslow.armaDialogCreator.expression.Value;
import com.kaylerrenslow.armaDialogCreator.main.ArmaDialogCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 @author Kayler
 Holds data that aren't specific to the current Project, but rather the entire application itself
 Created on 06/07/2016. */
public class ApplicationData {
	
	private Project currentProject;
	
	public void initApplicationData(@NotNull Project project) {
		this.currentProject = project;
	}
	
	private final SimpleEnv globalEnv = new SimpleEnv() {
		@Override
		public @Nullable Value getValue(String identifier) {
			Value v = super.getValue(identifier);
			if (v == null) {
				v = getCurrentProject().getMacroRegistry().getMacroValue(identifier);
			}
			return v;
		}
	};
	
	@NotNull
	public Project getCurrentProject() {
		return currentProject;
	}
	
	public void setCurrentProject(@NotNull Project currentProject) {
		this.currentProject = currentProject;
	}
	
	/**
	 Get the {@link Env} instance used for converting identifiers that match any Macro instances key retrieved from {@link Macro#getKey()}. The identifiers can also be
	 {@link PositionCalculator#SAFE_ZONE_X},
	 {@link PositionCalculator#SAFE_ZONE_Y},
	 {@link PositionCalculator#SAFE_ZONE_W},
	 {@link PositionCalculator#SAFE_ZONE_H},
	 */
	public Env getGlobalExpressionEnvironment() {
		//update the environment
		globalEnv.put(PositionCalculator.SAFE_ZONE_X, new Value.NumVal(ArmaDialogCreator.getCanvasView().getCurrentResolution().getSafeZoneX()));
		globalEnv.put(PositionCalculator.SAFE_ZONE_Y, new Value.NumVal(ArmaDialogCreator.getCanvasView().getCurrentResolution().getSafeZoneY()));
		globalEnv.put(PositionCalculator.SAFE_ZONE_W, new Value.NumVal(ArmaDialogCreator.getCanvasView().getCurrentResolution().getSafeZoneW()));
		globalEnv.put(PositionCalculator.SAFE_ZONE_H, new Value.NumVal(ArmaDialogCreator.getCanvasView().getCurrentResolution().getSafeZoneH()));
		
		return globalEnv;
	}
}
