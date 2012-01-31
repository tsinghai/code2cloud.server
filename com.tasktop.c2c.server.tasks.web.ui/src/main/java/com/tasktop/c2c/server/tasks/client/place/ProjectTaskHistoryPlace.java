/*******************************************************************************
 * Copyright (c) 2010, 2012 Tasktop Technologies
 * Copyright (c) 2010, 2011 SpringSource, a division of VMware
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 ******************************************************************************/
package com.tasktop.c2c.server.tasks.client.place;

import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskAction;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskHistoryAction;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskHistoryResult;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskResult;

/**
 * @author jtyrrell
 * @author jhickey
 * 
 */
public class ProjectTaskHistoryPlace extends AbstractProjectTaskBatchingPlace {

	private static final String TASK = "taskId";

	public static PageMapping ProjectTaskHistory = new PageMapping(new ProjectTaskHistoryPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/task/{" + TASK + ":Integer}/history");

	public static class Tokenizer implements PlaceTokenizer<ProjectTaskHistoryPlace> {

		@Override
		public ProjectTaskHistoryPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			return createPlace(pathArgs.getString(Path.PROJECT_ID), pathArgs.getInteger(TASK));
		}

		@Override
		public String getToken(ProjectTaskHistoryPlace place) {
			return place.getToken();
		}
	}

	private Integer taskId;
	private Task task;
	private List<TaskActivity> taskActivity;
	private List<Breadcrumb> breadcrumbs;

	public static ProjectTaskHistoryPlace createPlace(String projectId, Integer taskId) {
		return new ProjectTaskHistoryPlace(projectId, taskId);
	}

	protected ProjectTaskHistoryPlace(String projectId, Integer taskId) {
		super(projectId);
		this.taskId = taskId;
	}

	@Override
	public String getWindowTitle() {
		return "History of " + task.getTaskType() + " " + task.getId() + " - " + project.getName() + " - "
				+ WindowTitleBuilder.PRODUCT_NAME;
	}

	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);
		tokenMap.put(TASK, String.valueOf(taskId));

		return ProjectTaskHistory.getUrlForNamedArgs(tokenMap);
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public Task getTask() {
		return task;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetTaskAction(projectId, taskId));
		actions.add(new GetTaskHistoryAction(projectId, taskId));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		task = getResult(GetTaskResult.class).get();
		taskActivity = getResult(GetTaskHistoryResult.class).get();

		createBreadcrumbs(project, task);
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project, Task task) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectTasksPlace.createDefaultPlace(project.getIdentifier()).getHistoryToken(),
				"Tasks"));
		breadcrumbs.add(new Breadcrumb(ProjectTaskPlace.createPlace(project.getIdentifier(), task.getId())
				.getHistoryToken(), task.getTaskType() + " #" + task.getId()));
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "History"));
	}

	/**
	 * @return the taskActivity
	 */
	public List<TaskActivity> getTaskActivity() {
		return taskActivity;
	}

}
