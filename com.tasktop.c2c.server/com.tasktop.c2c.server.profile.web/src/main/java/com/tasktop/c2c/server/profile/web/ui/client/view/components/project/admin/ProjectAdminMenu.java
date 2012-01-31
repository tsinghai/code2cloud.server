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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin;


import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminSourcePlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminTeamPlace;
import com.tasktop.c2c.server.tasks.client.place.AbstractProjectAdminTasksPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminTasksPlace;

public class ProjectAdminMenu extends Composite {
	interface ProjectAdminMenuUiBinder extends UiBinder<HTMLPanel, ProjectAdminMenu> {
	}

	private static ProjectAdminMenu instance;

	public static ProjectAdminMenu getInstance() {
		if (instance == null) {
			instance = new ProjectAdminMenu();
		}
		return instance;
	}

	private static ProjectAdminMenuUiBinder ourUiBinder = GWT.create(ProjectAdminMenuUiBinder.class);
	private static String SELECTED = "selected";

	@UiField
	Anchor adminSettings;
	@UiField
	Anchor adminTasks;
	@UiField
	Anchor adminSourceCode;
	@UiField
	Anchor adminTeam;

	private Widget selected = null;

	public ProjectAdminMenu() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void updateUrls(Project project) {
		String projectIdentifier = project.getIdentifier();

		adminSettings.setHref(ProjectAdminSettingsPlace.createPlace(projectIdentifier).getHref());
		adminTeam.setHref(ProjectAdminTeamPlace.createPlace(projectIdentifier).getHref());

		if (project.getProjectServicesOfType(ServiceType.TASKS).isEmpty()) {
			adminTasks.setVisible(false);
		} else {
			adminTasks.setVisible(true);
			adminTasks.setHref(ProjectAdminTasksPlace.createDefaultPlace(projectIdentifier).getHref());
		}
		if (project.getProjectServicesOfType(ServiceType.SCM).isEmpty()) {
			adminSourceCode.setVisible(false);
		} else {
			adminSourceCode.setVisible(true);
			adminSourceCode.setHref(ProjectAdminSourcePlace.createPlace(projectIdentifier).getHref());
		}
	}

	public void select(Place place) {
		if (place instanceof ProjectAdminSettingsPlace) {
			changeSelected(adminSettings);
		} else if (place instanceof AbstractProjectAdminTasksPlace) {
			changeSelected(adminTasks);
		} else if (place instanceof ProjectAdminSourcePlace) {
			changeSelected(adminSourceCode);
		} else if (place instanceof ProjectAdminTeamPlace) {
			changeSelected(adminTeam);
		}
	}

	private void changeSelected(Widget toSelect) {
		if (selected != null) {
			selected.removeStyleName(SELECTED);
		}
		selected = toSelect;
		selected.addStyleName(SELECTED);
	}
}
