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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place;

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
import com.tasktop.c2c.server.profile.web.client.place.ProjectAdminPlace;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamSummary;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectTeamAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectTeamResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;

public class ProjectAdminTeamPlace extends ProjectAdminPlace {

	public static class Tokenizer implements PlaceTokenizer<ProjectAdminTeamPlace> {

		@Override
		public ProjectAdminTeamPlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			return createPlace(pathArgs.getString(Path.PROJECT_ID));
		}

		@Override
		public String getToken(ProjectAdminTeamPlace place) {
			return place.getToken();
		}
	}

	public static ProjectAdminTeamPlace createPlace(String projectId) {
		return new ProjectAdminTeamPlace(projectId);
	}

	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
	private ProjectTeamSummary projectTeamSummary;

	private ProjectAdminTeamPlace(String projectId) {
		super(projectId);
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectIdentifer);

		return PageMappings.ProjectAdminTeam.getUrlForNamedArgs(tokenMap);
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectTeamAction(projectIdentifer));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		projectTeamSummary = getResult(GetProjectTeamResult.class).get();
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	@Override
	public Project getProject() {
		return project;
	}

	public ProjectTeamSummary getProjectTeamSummary() {
		return projectTeamSummary;
	}

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(
				ProjectAdminSettingsPlace.createPlace(project.getIdentifier()).getHistoryToken(), "Settings"));

		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Team"));
	}
}
