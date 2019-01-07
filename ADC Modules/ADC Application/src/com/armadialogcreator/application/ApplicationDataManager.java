package com.armadialogcreator.application;

import com.armadialogcreator.util.ListObserver;
import com.armadialogcreator.util.UpdateListenerGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 @author K
 @since 01/03/2019 */
public class ApplicationDataManager {
	private static final ApplicationDataManager instance = new ApplicationDataManager();

	private final ListObserver<ApplicationData> applicationDataList = new ListObserver<>(new ArrayList<>());
	private final List<ApplicationStateSubscriber> subs = new ArrayList<>();
	private final UpdateListenerGroup<ApplicationState> applicationStateUpdateGroup = new UpdateListenerGroup<>();

	private volatile Project project;
	private volatile Workspace workspace;

	@NotNull
	public ListObserver<ApplicationData> getApplicationDataList() {
		return applicationDataList;
	}

	@NotNull
	public static ApplicationDataManager getInstance() {
		return instance;
	}

	@NotNull
	public Workspace getCurrentWorkspace() {
		if (workspace == null) {
			throw new IllegalStateException("workspace never initialized");
		}
		return workspace;
	}

	@NotNull
	public Project getCurrentProject() {
		if (project == null) {
			throw new IllegalStateException("project never initialized");
		}
		return project;
	}

	protected void setProject(@NotNull Project project) {
		this.project = project;
	}

	protected void setWorkspace(@NotNull Workspace workspace) {
		this.workspace = workspace;
	}

	public void addStateSubscriber(@NotNull ApplicationStateSubscriber sub) {
		subs.add(sub);
	}

	@NotNull
	protected List<ApplicationStateSubscriber> getApplicationStateSubs() {
		return subs;
	}

	/** A way of subscribing to state changes without needing to implement {@link ApplicationStateSubscriber} */
	@NotNull
	public UpdateListenerGroup<ApplicationState> getApplicationStateUpdateGroup() {
		return applicationStateUpdateGroup;
	}
}