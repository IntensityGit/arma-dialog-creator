package com.kaylerrenslow.armaDialogCreator.data.xml;

import com.kaylerrenslow.armaDialogCreator.data.DataKeys;
import com.kaylerrenslow.armaDialogCreator.data.Project;
import com.kaylerrenslow.armaDialogCreator.data.ProjectInfo;
import com.kaylerrenslow.armaDialogCreator.gui.fxcontrol.treeView.TreeStructure;
import com.kaylerrenslow.armaDialogCreator.gui.main.treeview.TreeItemEntry;
import com.kaylerrenslow.armaDialogCreator.main.Lang;
import com.kaylerrenslow.armaDialogCreator.util.DataContext;
import com.kaylerrenslow.armaDialogCreator.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;

/**
 Loads a project from a .xml save file. When the xml is loaded, a {@link ProjectVersionLoader} is designated to do the rest of the xml loading.

 @author Kayler
 @since 07/28/2016. */
public class ProjectXmlLoader extends XmlLoader {

	private final String saveVersion;


	protected ProjectXmlLoader(@NotNull File xmlFile, @Nullable DataContext context, Key<?>... keys) throws XmlParseException {
		super(xmlFile, context, keys);
		saveVersion = document.getDocumentElement().getAttribute("save-version").trim();
	}

	/**
	 Parses the given file and returns the result with the Project instance.

	 @param info project information
	 @param context must contain keys: {@link DataKeys#ARMA_RESOLUTION}, {@link DataKeys#ENV}
	 @return result
	 @throws XmlParseException when the file could not be properly parsed
	 */
	@NotNull
	public static ProjectParseResult parseProjectXmlFile(@NotNull ProjectInfo info, @NotNull DataContext context) throws XmlParseException {
		ProjectXmlLoader loader = new ProjectXmlLoader(info.getProjectXmlFile(), context, DataKeys.ENV, DataKeys.ARMA_RESOLUTION);
		ProjectVersionLoader versionLoader = getVersionLoader(info, loader);
		versionLoader.parseDocument();
		return new ProjectParseResult(versionLoader.project, versionLoader.treeStructureMain, versionLoader.treeStructureBg, loader.getErrors());
	}

	/**
	 Parses the given file and returns the result with the project information.

	 @param projectSaveXml file that contains the project save xml
	 @return result
	 @throws XmlParseException when the file could not be properly parsed
	 */
	@NotNull
	public static ProjectPreviewParseResult previewParseProjectXmlFile(@NotNull File projectSaveXml) throws XmlParseException {
		ProjectPreviewLoaderVersion1 versionLoader = new ProjectPreviewLoaderVersion1(projectSaveXml);
		versionLoader.parseDocument();
		return new ProjectPreviewParseResult(new ProjectInfo(versionLoader.getProjectName(), projectSaveXml, projectSaveXml.getParentFile()), versionLoader.getErrors());
	}


	private static ProjectVersionLoader getVersionLoader(@NotNull ProjectInfo info, @NotNull ProjectXmlLoader loader) throws XmlParseException {
		switch (loader.saveVersion) {
			case "1":
				return new ProjectLoaderVersion1(info, loader);
			default:
				throw new XmlParseException(Lang.ApplicationBundle().getString("XmlParse.ProjectLoad.not_a_project_save"));
		}
	}

	public static class ProjectParseResult extends XmlLoader.ParseResult {

		private final Project project;
		private final TreeStructure<TreeItemEntry> treeStructureMain;
		private final TreeStructure<TreeItemEntry> treeStructureBg;

		private ProjectParseResult(Project project, TreeStructure<TreeItemEntry> treeStructureMain, TreeStructure<TreeItemEntry> treeStructureBg, ArrayList<ParseError> errors) {
			super(errors);
			this.project = project;
			this.treeStructureMain = treeStructureMain;
			this.treeStructureBg = treeStructureBg;
		}

		@NotNull
		public TreeStructure<TreeItemEntry> getTreeStructureBg() {
			return treeStructureBg;
		}

		@NotNull
		public TreeStructure<TreeItemEntry> getTreeStructureMain() {
			return treeStructureMain;
		}

		@NotNull
		public Project getProject() {
			return project;
		}

	}

	public static class ProjectPreviewParseResult {
		private final ArrayList<ParseError> errors;
		private final ProjectInfo projectInfo;

		public ProjectPreviewParseResult(@NotNull ProjectInfo projectInfo, @NotNull ArrayList<ParseError> errors) {
			this.projectInfo = projectInfo;
			this.errors = errors;
		}

		@NotNull
		public ArrayList<ParseError> getErrors() {
			return errors;
		}

		@NotNull
		public ProjectInfo getProjectInfo() {
			return projectInfo;
		}

	}
}