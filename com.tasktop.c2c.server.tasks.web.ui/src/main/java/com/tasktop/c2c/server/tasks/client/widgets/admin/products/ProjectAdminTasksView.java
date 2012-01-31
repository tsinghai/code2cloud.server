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
package com.tasktop.c2c.server.tasks.client.widgets.admin.products;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class ProjectAdminTasksView extends Composite implements
		IProjectAdminTasksView<IProjectAdminTasksView.ProjectAdminTasksPresenter> {

	interface ProjectAdminTasksViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminTasksView> {
	}

	private static ProjectAdminTasksViewUiBinder ourUiBinder = GWT.create(ProjectAdminTasksViewUiBinder.class);

	public static interface Presenter extends ProjectAdminTasksMenu.Presenter {
	}

	private static ProjectAdminTasksView instance;
	private ProjectAdminTasksEditView editView = ProjectAdminTasksEditView.getInstance();
	private ProjectAdminTasksDisplayView displayView = ProjectAdminTasksDisplayView.getInstance();

	public static ProjectAdminTasksView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminTasksView();
		}
		return instance;
	}

	@UiField
	SimplePanel contentContainer;
	@UiField(provided = true)
	ProjectAdminTasksMenu menu = ProjectAdminTasksMenu.getInstance();

	public ProjectAdminTasksView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void setPresenter(IProjectAdminTasksView.ProjectAdminTasksPresenter presenter) {
		menu.setPresenter(presenter);
		if (presenter.isEditing()) {
			editView.setPresenter(presenter);
			contentContainer.setWidget(editView);
		} else {
			displayView.setPresenter(presenter);
			contentContainer.setWidget(displayView);
		}
	}
}
