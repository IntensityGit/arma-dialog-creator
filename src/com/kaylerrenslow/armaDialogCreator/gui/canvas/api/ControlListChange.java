/*
 * Copyright (c) 2016 Kayler Renslow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * The software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. in no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the software.
 */

package com.kaylerrenslow.armaDialogCreator.gui.canvas.api;

import org.jetbrains.annotations.NotNull;

/**
 Created by Kayler on 08/12/2016.
 */
public class ControlListChange<C extends Control> {
	private final ControlList<C> modifiedList;
	private ControlAdd<C> added;
	private ControlRemove<C> removed;
	private ControlMove<C> moved;
	private ControlSet<C> set;
	private ChangeType changeType = null;

	public ControlListChange(ControlList<C> modifiedList) {
		this.modifiedList = modifiedList;
	}

	@NotNull
	public ChangeType getChangeType() {
		return changeType;
	}

	void setSet(ControlSet<C> set) {
		this.set = set;
		checkType();
		changeType = ChangeType.SET;
	}

	void setAdded(ControlAdd<C> added) {
		this.added = added;
		checkType();
		changeType = ChangeType.ADD;
	}

	void setRemoved(ControlRemove<C> removed) {
		this.removed = removed;
		checkType();
		changeType = ChangeType.REMOVE;
	}

	void setMoved(ControlMove<C> move) {
		this.moved = move;
		checkType();
		changeType = ChangeType.MOVE;
	}

	private void checkType() {
		if (changeType != null) {
			throw new IllegalStateException("only one changeType is allowed at once");
		}
	}

	/** Get the list that had the change */
	@NotNull
	public ControlList<C> getModifiedList() {
		return modifiedList;
	}

	public boolean wasSet() {
		return changeType == ChangeType.SET;
	}

	public boolean wasAdded() {
		return changeType == ChangeType.ADD;
	}

	public boolean wasRemoved() {
		return changeType == ChangeType.REMOVE;
	}

	public boolean wasMoved() {
		return changeType == ChangeType.MOVE;
	}

	public ControlSet<C> getSet() {
		return set;
	}

	public ControlAdd<C> getAdded() {
		return added;
	}

	public ControlRemove<C> getRemoved() {
		return removed;
	}

	public ControlMove<C> getMoved() {
		return moved;
	}
}
