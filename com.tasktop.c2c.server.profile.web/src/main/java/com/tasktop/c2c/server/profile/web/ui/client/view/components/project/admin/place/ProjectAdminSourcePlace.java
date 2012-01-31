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
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectAdminPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectScmRepositoriesAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectScmRepositoriesResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;

public class ProjectAdminSourcePlace extends ProjectAdminPlace implements BreadcrumbPlace {

	public static class Tokenizer implements PlaceTokenizer<ProjectAdminSourcePlace> {

		@Override
		public ProjectAdminSourcePlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projId = pathArgs.getString(Path.PROJECT_ID);

			return createPlace(projId);
		}

		@Override
		public String getToken(ProjectAdminSourcePlace place) {
			return place.getToken();
		}
	}

	public static ProjectAdminSourcePlace createPlace(String projectId) {
		return new ProjectAdminSourcePlace(projectId);
	}

	private List<ScmRepository> repositories;
	private String gitBaseUrl;
	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();

	private ProjectAdminSourcePlace(String projectId) {
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

		return PageMappings.ProjectAdminSCM.getUrlForNamedArgs(tokenMap);
	}

	@Override
	public Project getProject() {
		return project;
	}

	public List<ScmRepository> getRepositories() {
		return repositories;
	}

	public String getGitBaseUrl() {
		return gitBaseUrl;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public Section getSection() {
		return Section.ADMIN;
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectScmRepositoriesAction(projectIdentifer));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		repositories = getResult(GetProjectScmRepositoriesResult.class).get();
		gitBaseUrl = getResult(GetProjectScmRepositoriesResult.class).getGitRepositoryBaseUrl();
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(
				ProjectAdminSettingsPlace.createPlace(project.getIdentifier()).getHistoryToken(), "Settings"));
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Source"));
	}

}
