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
package com.tasktop.c2c.server.profile.web.ui.client.place;

import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AbstractBatchFetchingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamSummary;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectTeamAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectTeamResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class ProjectTeamPlace extends AbstractBatchFetchingPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace {

	public static class Tokenizer implements PlaceTokenizer<ProjectTeamPlace> {

		@Override
		public ProjectTeamPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			return createPlace(pathArgs.getString(Path.PROJECT_ID));
		}

		@Override
		public String getToken(ProjectTeamPlace place) {
			return place.getToken();
		}
	}

	private String projectId;
	private Project project;
	private ProjectTeamSummary projectTeamSummary;
	private List<Breadcrumb> breadcrumbs;

	protected ProjectTeamPlace(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public Section getSection() {
		return Section.TEAM;
	}

	public ProjectTeamSummary getProjectTeamSummary() {
		return projectTeamSummary;
	}

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	public String getToken() {
		return "";
	}

	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		return PageMappings.ProjectTeam.getUrlForNamedArgs(tokenMap);
	}

	public static ProjectTeamPlace createPlace(String projectId) {
		return new ProjectTeamPlace(projectId);
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectAction(projectId));
		actions.add(new GetProjectTeamAction(projectId));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		project = getResult(GetProjectResult.class).get();
		projectTeamSummary = getResult(GetProjectTeamResult.class).get();
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Team"));
	}
}
