package com.kaylerrenslow.armaDialogCreator.gui.fx.main;

import com.kaylerrenslow.armaDialogCreator.gui.fx.control.treeView.EditableTreeView;
import com.kaylerrenslow.armaDialogCreator.gui.fx.control.treeView.TreeStructure;
import com.kaylerrenslow.armaDialogCreator.gui.fx.main.editor.UICanvasConfiguration;
import com.kaylerrenslow.armaDialogCreator.gui.fx.main.treeview.TreeItemEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 Created by Kayler on 05/20/2016.
 */
public interface CanvasView {

	@NotNull
	UICanvasConfiguration getConfiguration();

	/**
	 Set the background image of the canvas

	 @param imgPath the path to the image
	 */
	void setCanvasBackgroundToImage(@Nullable String imgPath);

	/** Fetches the new ui colors and repaints the canvas */
	void updateCanvas();

	/**
	 Update the Absolute region box. For each parameter: -1 to leave unchanged, 0 for false, 1 for true

	 @param alwaysFront true if the region should always be rendered last, false if it should be rendered first
	 @param showing true the region is showing, false if not
	 */
	void updateAbsRegion(int alwaysFront, int showing);
	
	void setTreeStructure(boolean backgroundTree, TreeStructure<TreeItemEntry> treeStructure);

	@NotNull
	TreeStructure<? extends TreeItemEntry> getMainControlsTreeStructure();

	@NotNull
	TreeStructure<? extends TreeItemEntry> getBackgroundControlsTreeStructure();

	@NotNull
	EditableTreeView<? extends TreeItemEntry> getMainControlTreeView();

	@NotNull
	EditableTreeView<? extends TreeItemEntry> getBackgroundControlTreeView();
}
