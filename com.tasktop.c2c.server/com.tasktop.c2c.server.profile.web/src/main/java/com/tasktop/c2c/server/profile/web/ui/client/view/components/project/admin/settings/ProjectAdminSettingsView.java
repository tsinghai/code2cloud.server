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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class ProjectAdminSettingsView extends Composite {
	interface ProjectAdminSettingsViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminSettingsView> {
	}

	public static interface Presenter extends ProjectAdminSettingsDisplayView.Presenter, ProjectAdminSettingsEditView.Presenter {
		boolean isEditing();
	}

	private static ProjectAdminSettingsViewUiBinder ourUiBinder = GWT.create(ProjectAdminSettingsViewUiBinder.class);
	@UiField
	SimplePanel contentContainer;

	private ProjectAdminSettingsEditView editView = ProjectAdminSettingsEditView.getInstance();
	private ProjectAdminSettingsDisplayView displayView = ProjectAdminSettingsDisplayView.getInstance();

	private static ProjectAdminSettingsView instance;

	public static ProjectAdminSettingsView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminSettingsView();
		}
		return instance;
	}

	public ProjectAdminSettingsView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		contentContainer.setWidget(ProjectAdminSettingsDisplayView.getInstance());
	}

	public void setPresenter(Presenter presenter) {
		if (presenter.isEditing()) {
			editView.setPresenter(presenter);
			contentContainer.setWidget(editView);
		} else {
			displayView.setPresenter(presenter);
			contentContainer.setWidget(displayView);
		}
	}

}
