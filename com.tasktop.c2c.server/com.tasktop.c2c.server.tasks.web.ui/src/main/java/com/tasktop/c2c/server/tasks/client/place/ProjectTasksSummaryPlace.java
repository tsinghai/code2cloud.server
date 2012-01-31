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
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.tasks.client.TaskPageMappings;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskSummaryAction;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskSummaryResult;

/**
 * @author Ryan Slobojan (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class ProjectTasksSummaryPlace extends AbstractProjectTaskBatchingPlace {

	public static final String COMPONENT = "componentId";
	public static final String RELEASE = "release";

	public static class Tokenizer implements PlaceTokenizer<ProjectTasksSummaryPlace> {

		@Override
		public ProjectTasksSummaryPlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projectId = pathArgs.getString(Path.PROJECT_ID);
			Integer productId = pathArgs.getInteger(ProjectTasksSummaryListPlace.PRODUCT);

			// At least one of these 2 will be defined, and possibly both will.
			String release = pathArgs.getString(RELEASE);
			Integer componentId = pathArgs.getInteger(COMPONENT);

			if (StringUtils.hasText(release)) {
				// We have a release - now check to see if we also have a component.
				if (componentId == null) {
					// No component ID, so just generate a release list
					return createPlaceForRelease(projectId, productId, release);
				} else {
					// Both a release and component ID.
					return createPlaceForComponentAndRelease(projectId, productId, componentId, release);
				}
			} else {
				// No release, so we must have only a component
				return createPlaceForComponent(projectId, productId, componentId);
			}
		}

		@Override
		public String getToken(ProjectTasksSummaryPlace place) {
			return place.getToken();
		}
	}

	public static ProjectTasksSummaryPlace createPlaceForComponent(String projectId, Integer productId,
			Integer componentId) {
		return new ProjectTasksSummaryPlace(projectId, productId, componentId, null);
	}

	public static ProjectTasksSummaryPlace createPlaceForComponentAndRelease(String projectId, Integer productId,
			Integer componentId, String release) {
		return new ProjectTasksSummaryPlace(projectId, productId, componentId, release);
	}

	public static ProjectTasksSummaryPlace createPlaceForRelease(String projectId, Integer productId, String release) {
		return new ProjectTasksSummaryPlace(projectId, productId, null, release);
	}

	private ProjectTasksSummaryPlace(String projectId, Integer productId, Integer componentId, String release) {
		super(projectId);
		this.productId = productId;
		this.componentId = componentId;
		this.release = release;
	}

	private Integer productId;
	private Integer componentId;
	private String release;
	private List<Task> tasks = new ArrayList<Task>();
	private List<Breadcrumb> breadcrumbs;

	public Integer getProductId() {
		return productId;
	}

	public Integer getComponentId() {
		return componentId;
	}

	public String getMilestone() {
		return release;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);
		tokenMap.put(ProjectTasksSummaryListPlace.PRODUCT, String.valueOf(productId));

		if (componentId != null) {
			tokenMap.put(COMPONENT, String.valueOf(componentId));
		}

		if (StringUtils.hasText(release)) {
			tokenMap.put(RELEASE, release);
		}

		return TaskPageMappings.ProjectTaskSummary.getUrlForNamedArgs(tokenMap);
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		Criteria productCriteria = new ColumnCriteria(TaskFieldConstants.PRODUCT_FIELD, Criteria.Operator.EQUALS,
				productId);
		NaryCriteria fullQuery = new NaryCriteria(Criteria.Operator.AND, productCriteria);

		if (componentId != null) {
			Criteria componentCriteria = new ColumnCriteria(TaskFieldConstants.COMPONENT_FIELD,
					Criteria.Operator.EQUALS, this.componentId);
			fullQuery.addSubCriteria(componentCriteria);
		}

		if (release != null) {
			Criteria milestoneCriteria = new ColumnCriteria(TaskFieldConstants.MILESTONE_FIELD,
					Criteria.Operator.EQUALS, release);
			fullQuery.addSubCriteria(milestoneCriteria);
		}

		// FIXME - arbitrary limit 25000?
		QueryRequest queryRequest = new QueryRequest(new Region(0, 25000), new SortInfo(
				TaskFieldConstants.TASK_ID_FIELD, SortInfo.Order.ASCENDING));

		actions.add(new GetTaskSummaryAction(projectId, fullQuery.toQueryString(), queryRequest));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		tasks = getResult(GetTaskSummaryResult.class).get();
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectTasksPlace.createDefaultPlace(project.getIdentifier()).getHistoryToken(),
				"Tasks"));

	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Task Summary", project.getName());
	}
}
