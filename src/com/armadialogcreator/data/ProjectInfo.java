
package com.armadialogcreator.data;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 @author Kayler
 @since 11/23/2016 */
public class ProjectInfo {
	private final String projectName;
	private final File projectXmlFile;
	private final File projectDirectory;
	private final Workspace workspace;

	public ProjectInfo(@NotNull String projectName, @NotNull String projectDirectoryName, @NotNull Workspace workspace) {
		this.projectName = projectName;
		this.projectDirectory = workspace.getFileForName(Project.makeProjectNameSafe(projectDirectoryName) + File.separator);
		this.projectXmlFile = new File(projectDirectory.getPath() + File.separator + Project.PROJECT_SAVE_FILE_NAME);
		this.workspace = workspace;
	}

	@NotNull
	public Workspace getWorkspace() {
		return workspace;
	}

	@NotNull
	public File getProjectXmlFile() {
		return projectXmlFile;
	}

	@NotNull
	public String getProjectName() {
		return projectName;
	}

	@NotNull
	public File getProjectDirectory() {
		return projectDirectory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ProjectInfo that = (ProjectInfo) o;

		if (!projectName.equals(that.projectName)) {
			return false;
		}
		return projectDirectory.equals(that.projectDirectory);
	}

	public String toString() {
		return projectName;
	}

}
