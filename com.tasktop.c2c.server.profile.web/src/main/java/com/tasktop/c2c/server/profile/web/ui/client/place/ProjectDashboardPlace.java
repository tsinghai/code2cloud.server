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

import java.util.ArrayList;
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
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class ProjectDashboardPlace extends AbstractBatchFetchingPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace, WindowTitlePlace {
	private static final String NO_ID = "create";

	public static class Tokenizer implements PlaceTokenizer<ProjectDashboardPlace> {

		@Override
		public ProjectDashboardPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			return createPlace(pathArgs.getString(Path.PROJECT_ID));
		}

		@Override
		public String getToken(ProjectDashboardPlace place) {
			return place.getToken();
		}
	}

	private String projectId;
	private Project project;
	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();

	@Override
	public Project getProject() {
		return project;
	}

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public Section getSection() {
		return Section.DASHBOARD;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectAction(projectId));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		this.project = getResult(GetProjectResult.class).get();
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		return PageMappings.ProjectDashboard.getUrlForNamedArgs(tokenMap);
	}

	public String getToken() {
		return "";
	}

	public static ProjectDashboardPlace createPlace(String projectId) {
		if (projectId == null || projectId.length() == 0 || projectId.equals("")) {
			projectId = NO_ID;
		}
		return new ProjectDashboardPlace(projectId);
	}

	private ProjectDashboardPlace(String projectId) {
		this.projectId = projectId;
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectDashboardPlace.createPlace(project.getIdentifier()).getToken(),
				"Dashboard"));
	}

	@Override
	public String getWindowTitle() {
		return "Dashboard - " + project.getName() + " - " + WindowTitleBuilder.PRODUCT_NAME;
	}
}
