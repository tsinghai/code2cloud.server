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
package com.tasktop.c2c.server.wiki.web.ui.client.place;

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.place.AbstractBatchFetchingPlace;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractProjectWikiPlace extends AbstractBatchFetchingPlace implements HeadingPlace,
		HasProjectPlace, BreadcrumbPlace, SectionPlace, WindowTitlePlace {

	protected String projectId;
	protected Project project;

	/**
	 * @param roles
	 */
	public AbstractProjectWikiPlace(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * 
	 */
	public AbstractProjectWikiPlace() {
		super();
	}

	public Project getProject() {
		return project;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public Section getSection() {
		return Section.WIKI;
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectAction(projectId));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		project = getResult(GetProjectResult.class).get();
	}

	// TODO push to new super
	@Override
	protected List<String> getUserRolesForPlace() {
		List<String> roles = null;
		if (ProfileGinjector.get.instance().getAppState().getCredentials() != null) {
			roles = ProfileGinjector.get.instance().getAppState().getCredentials().getRoles();
		}
		return AuthenticationHelper.getRolesForProject(getProject(), roles);
	}

}
