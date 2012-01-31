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
package com.tasktop.c2c.server.profile.web.client.place;

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;

/**
 * @author cmorgan
 * 
 */
public abstract class ProjectAdminPlace extends LoggedInPlace implements HeadingPlace, SectionPlace, WindowTitlePlace,
		HasProjectPlace, BreadcrumbPlace {

	protected String projectIdentifer;
	protected Project project;

	protected ProjectAdminPlace(String projectIdentifer) {
		this.projectIdentifer = projectIdentifer;
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle(Section.ADMIN, project.getName());
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectAction(projectIdentifer));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		project = getResult(GetProjectResult.class).get();
	}

	@Override
	public String getHeading() {
		return "Project Admin";
	}

	@Override
	public Section getSection() {
		return Section.ADMIN;
	}

	/**
	 * @return the projectIdentifer
	 */
	public String getProjectIdentifer() {
		return projectIdentifer;
	}

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}

}
