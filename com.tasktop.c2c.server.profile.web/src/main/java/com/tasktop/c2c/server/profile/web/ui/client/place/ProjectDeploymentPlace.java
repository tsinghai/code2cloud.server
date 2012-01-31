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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.place.SecuredProjectPlace;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentService;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class ProjectDeploymentPlace extends SecuredProjectPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace {

	private static final Set<String> roles = new HashSet<String>(Arrays.asList(Role.User));

	public static class Tokenizer implements PlaceTokenizer<ProjectDeploymentPlace> {

		@Override
		public ProjectDeploymentPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			return createPlace(pathArgs.getString(Path.PROJECT_ID));
		}

		@Override
		public String getToken(ProjectDeploymentPlace place) {
			return place.getToken();
		}
	}

	private Project project;
	private List<Breadcrumb> breadcrumbs;
	private List<DeploymentConfiguration> deploymentConfigurations;
	private DeploymentService.AvailableBuildInformation buildInformation;

	public Project getProject() {
		return project;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public Section getSection() {
		return Section.DEPLOYMENTS;
	}

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	public List<DeploymentConfiguration> getDeploymentConfigurations() {
		return deploymentConfigurations;
	}

	public DeploymentService.AvailableBuildInformation getBuildInformation() {
		return buildInformation;
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		return PageMappings.ProjectDeployment.getUrlForNamedArgs(tokenMap);
	}

	@Override
	protected void fetchPlaceData() {
		AppGinjector.get.instance().getProfileService().getProject(projectId, new AsyncCallbackSupport<Project>() {
			@Override
			protected void success(Project result) {
				project = result;
				createBreadCrumbs(result);
				fetchBuildConfigs();
			}
		});
	}

	private void fetchBuildConfigs() {
		AppGinjector.get.instance().getDeploymentService()
				.getDeploymentConfigurations(projectId, new AsyncCallbackSupport<List<DeploymentConfiguration>>() {

					@Override
					protected void success(List<DeploymentConfiguration> result) {
						deploymentConfigurations = result;
						fetchBuildInformation();
					}
				});
	}

	private void fetchBuildInformation() {
		AppGinjector.get
				.instance()
				.getDeploymentService()
				.getBuildInformation(projectId, null,
						new AsyncCallbackSupport<DeploymentService.AvailableBuildInformation>() {

							@Override
							protected void success(DeploymentService.AvailableBuildInformation result) {
								buildInformation = result;
								onPlaceDataFetched();

							}

						});
	}

	public String getToken() {
		return "";
	}

	public static ProjectDeploymentPlace createPlace(String projectId) {
		return new ProjectDeploymentPlace(projectId);
	}

	private ProjectDeploymentPlace(String projectId) {
		super(roles, projectId);
	}

	private void createBreadCrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectDeploymentPlace.createPlace(project.getIdentifier()).getHref(),
				"Deployments"));
	}
}
