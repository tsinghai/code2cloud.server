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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.tasktop.c2c.server.profile.domain.project.Project;

public class ProjectAdminView extends Composite {

	interface ProjectAdminViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminView> {
	}

	private static ProjectAdminViewUiBinder ourUiBinder = GWT.create(ProjectAdminViewUiBinder.class);
	@UiField(provided = true)
	protected ProjectAdminMenu adminMenu = ProjectAdminMenu.getInstance();

	@UiField
	protected SimplePanel contentContainer;

	private ProjectAdminView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public SimplePanel getContentContainer() {
		return contentContainer;
	}

	private static ProjectAdminView instance;

	public static ProjectAdminView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminView();
		}
		return instance;
	}

	public void setProject(Project project) {
		adminMenu.updateUrls(project);
	}
}
