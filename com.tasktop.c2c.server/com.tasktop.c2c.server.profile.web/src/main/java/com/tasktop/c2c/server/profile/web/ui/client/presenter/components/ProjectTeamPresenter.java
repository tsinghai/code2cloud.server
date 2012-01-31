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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamSummary;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectTeamPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ProjectTeamView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminTeamPlace;

public class ProjectTeamPresenter extends AbstractProfilePresenter implements SplittableActivity {

	private ProjectTeamView view;

	private String projectIdentifier;
	private ProjectTeamSummary teamSummary;

	public ProjectTeamPresenter(ProjectTeamView view) {
		super(view);
		this.view = view;
	}

	public ProjectTeamPresenter() {
		this(new ProjectTeamView());
	}

	public void setPlace(Place p) {
		ProjectTeamPlace place = (ProjectTeamPlace) p;
		this.projectIdentifier = place.getProject().getIdentifier();
		this.teamSummary = place.getProjectTeamSummary();
	}

	@Override
	protected void bind() {
		view.setProjectTeamMembers(teamSummary.getMembers());
		view.setupAdminControls(projectIdentifier);
		view.manageMembers.setHref(ProjectAdminTeamPlace.createPlace(projectIdentifier).getHref());
	}
}
