package com.armadialogcreator.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 Created by Kayler on 07/05/2016.
 */
public class UpdateListenerGroup<T> {
	private final List<UpdateGroupListener<T>> updateListeners = new LinkedList<>();
	private final LinkedList<UpdateListenerGroup<T>> chain = new LinkedList<>();
	private final List<UpdateGroupListener<T>> newListeners = new ArrayList<>();
	private final List<UpdateGroupListener<T>> removeListeners = new ArrayList<>();
	private boolean iterating = false; //prevent CoModificationException

	public void clearListeners() {
		updateListeners.clear();
		newListeners.clear();
		chain.clear();
	}

	/** Use this for when the T doesn't matter */
	public static final class NoData {
		private NoData() {
		}
	}

	public static final NoData NoDataInstance = new NoData();


	/** Will add the given listener. If the listener has already been added, will do nothing (no duplicates allowed). */
	public void addListener(@NotNull UpdateGroupListener<T> listener) {
		if (updateListeners.contains(listener)) {
			return;
		}
		if (iterating) {
			newListeners.add(listener);
		} else {
			updateListeners.add(listener);
		}
	}

	public boolean removeListener(@NotNull UpdateGroupListener<T> listener) {
		if (iterating) {
			int ind = updateListeners.indexOf(listener);
			if (ind < 0) {
				return false;
			}
			UpdateGroupListener<T> existing = updateListeners.get(ind);
			removeListeners.add(existing);
			return true;
		}
		return updateListeners.remove(listener);
	}

	public void update(@NotNull T data) {
		iterating = true;
		for (UpdateGroupListener<T> updateListener : updateListeners) {
			updateListener.update(this, data);
		}
		for (UpdateListenerGroup<T> group : chain) {
			group.update(data);
		}
		iterating = false;
		updateListeners.addAll(newListeners);
		updateListeners.removeAll(removeListeners);
		removeListeners.clear();
		newListeners.clear();
	}


	/**
	 Chain this group and the given group together. Whenever this group gets an update via {@link #update(Object)}, the provided group will also receive the update.

	 @param updateGroup group to chain
	 @see #unchain(UpdateListenerGroup)
	 */
	public void chain(@NotNull UpdateListenerGroup<T> updateGroup) {
		chain.add(updateGroup);
	}

	/**
	 Remove the given group from the chain

	 @param updateGroup group to unchain
	 @see #chain(UpdateListenerGroup)
	 */
	public void unchain(@NotNull UpdateListenerGroup<T> updateGroup) {
		chain.remove(updateGroup);
	}
}
