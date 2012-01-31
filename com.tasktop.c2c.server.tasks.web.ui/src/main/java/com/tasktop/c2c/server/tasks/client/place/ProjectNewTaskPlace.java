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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.tasks.client.TaskPageMappings;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationAction;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationResult;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskAction;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskResult;

public class ProjectNewTaskPlace extends AbstractProjectTaskBatchingPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace, WindowTitlePlace {

	public static class Tokenizer implements PlaceTokenizer<ProjectNewTaskPlace> {

		@Override
		public ProjectNewTaskPlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projId = pathArgs.getString(Path.PROJECT_ID);
			Integer parentTaskId = pathArgs.getInteger(PARENT_TASK_ID);

			if (parentTaskId != null) {
				return createNewSubtaskPlace(projId, parentTaskId);
			}

			// We're just creating a standard new task.
			return createNewTaskPlace(projId);

		}

		@Override
		public String getToken(ProjectNewTaskPlace place) {
			return place.getToken();
		}
	}

	public static final String PARENT_TASK_ID = "parent";

	private Integer parentId;
	private Task parent;
	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
	private RepositoryConfiguration repositoryConfiguration;

	public static ProjectNewTaskPlace createNewTaskPlace(String projectId) {
		return new ProjectNewTaskPlace(projectId, null);
	}

	public static ProjectNewTaskPlace createNewSubtaskPlace(String projectId, Integer parentId) {
		return new ProjectNewTaskPlace(projectId, parentId);
	}

	private ProjectNewTaskPlace(String projectId, Integer parentId) {
		super(projectId);
		this.parentId = parentId;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	public RepositoryConfiguration getRepositoryConfiguration() {
		return repositoryConfiguration;
	}

	@Override
	public String getWindowTitle() {
		return "New Task - " + project.getName();
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);
		if (parentId != null) {
			tokenMap.put(PARENT_TASK_ID, parentId.toString());
		}

		return TaskPageMappings.ProjectNewTask.getUrlForNamedArgs(tokenMap);
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetRepositoryConfigurationAction(projectId));
		if (this.parentId != null) {
			actions.add(new GetTaskAction(projectId, parentId));
		}
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		repositoryConfiguration = getResult(GetRepositoryConfigurationResult.class).get();
		if (this.parentId != null) {
			parent = getResult(GetTaskResult.class).get();
		}
		createBreadcrumbs(project, null);
		onPlaceDataFetched();
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public Section getSection() {
		return Section.TASKS;
	}

	private void createBreadcrumbs(Project project, Task task) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectTasksPlace.createDefaultPlace(project.getIdentifier()).getHistoryToken(),
				"Tasks"));
		String placeBreadcrumbText = "New Task";
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), placeBreadcrumbText));
	}

	/**
	 * @return the parent
	 */
	public Task getParent() {
		return parent;
	}
}
