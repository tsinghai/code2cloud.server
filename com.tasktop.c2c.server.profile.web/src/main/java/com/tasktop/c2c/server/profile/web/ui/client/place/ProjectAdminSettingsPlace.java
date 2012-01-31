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


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectAdminPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;

// TDODO batch fetch 
public class ProjectAdminSettingsPlace extends ProjectAdminPlace implements BreadcrumbPlace {

	public static PageMapping ProjectAdminSettings = new PageMapping(new ProjectAdminSettingsPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/admin");

	public static class Tokenizer implements PlaceTokenizer<ProjectAdminSettingsPlace> {

		@Override
		public ProjectAdminSettingsPlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projId = pathArgs.getString(Path.PROJECT_ID);

			return createPlace(projId);
		}

		@Override
		public String getToken(ProjectAdminSettingsPlace place) {
			return place.getToken();
		}
	}

	public static ProjectAdminSettingsPlace createPlace(String projectId) {
		return new ProjectAdminSettingsPlace(projectId);
	}

	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();

	protected ProjectAdminSettingsPlace(String projectId) {
		super(projectId);
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectIdentifer);

		return ProjectAdminSettings.getUrlForNamedArgs(tokenMap);
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		createBreadcrumbs(project);
		onPlaceDataFetched();
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
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	public Section getSection() {
		return Section.ADMIN;
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Settings"));
	}
}
