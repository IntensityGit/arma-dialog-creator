package com.armadialogcreator.control;

import com.armadialogcreator.canvas.UINode;
import com.armadialogcreator.util.NotNullValueObserver;
import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 02/06/2019 */
public class FolderUINode extends StructureUINode {
	private final NotNullValueObserver<String> folderNameObserver;

	public FolderUINode(@NotNull String folderName) {
		folderNameObserver = new NotNullValueObserver<>(folderName);
	}

	@NotNull
	public NotNullValueObserver<String> getFolderNameObserver() {
		return folderNameObserver;
	}

	@NotNull
	public String getFolderName() {
		return folderNameObserver.getValue();
	}

	@Override
	@NotNull
	public UINode deepCopy() {
		FolderUINode node = new FolderUINode(folderNameObserver.getValue());
		node.setParentNode(getParentNode());
		return node;
	}
}
