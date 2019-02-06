package com.armadialogcreator.core.old;

import com.armadialogcreator.util.ValueObserver;
import org.jetbrains.annotations.NotNull;

/**
 Used when {@link ControlClassOld#setClassName(String)} is invoked, or when {@link ControlClassOld#getClassNameObserver()} is updated
 via {@link ValueObserver#updateValue(Object)}

 @author Kayler
 @since 11/16/16 */
public class ControlClassRenameUpdate implements ControlClassUpdate {
	private ControlClassOld updated;
	private final String oldName;
	private final String newName;

	/**
	 @param updated the updated {@link ControlClassOld}
	 @param oldName the old class name
	 @param newName the new class name
	 */
	public ControlClassRenameUpdate(@NotNull ControlClassOld updated, @NotNull String oldName, @NotNull String newName) {
		this.updated = updated;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	@NotNull
	public ControlClassOld getOwnerControlClass() {
		return updated;
	}

	/** Get the old class name */
	@NotNull
	public String getOldName() {
		return oldName;
	}

	/** Get the new class name */
	@NotNull
	public String getNewName() {
		return newName;
	}
}
