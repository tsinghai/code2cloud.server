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
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.ProjectAdminPlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationAction;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationResult;

public abstract class AbstractProjectAdminTasksPlace extends ProjectAdminPlace {

	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
	protected RepositoryConfiguration repositoryConfiguration;

	protected AbstractProjectAdminTasksPlace(String projectId) {
		super(projectId);
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetRepositoryConfigurationAction(projectIdentifer));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		repositoryConfiguration = getResult(GetRepositoryConfigurationResult.class).get();
		createBreadcrumbs(project);
	}

	public RepositoryConfiguration getRepositoryConfiguration() {
		return repositoryConfiguration;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		// TODO admin
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Tasks"));
	}

	@Override
	public String getWindowTitle() {
		return "Tasks Admin - " + project.getName() + " - " + WindowTitleBuilder.PRODUCT_NAME;
	}
}
