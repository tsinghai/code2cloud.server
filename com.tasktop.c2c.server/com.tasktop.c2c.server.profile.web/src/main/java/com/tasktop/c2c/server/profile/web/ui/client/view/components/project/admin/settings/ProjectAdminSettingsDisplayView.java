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
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.tasktop.c2c.server.profile.domain.project.Project;

public class ProjectAdminSettingsDisplayView extends Composite implements Editor<Project> {
	interface ProjectAdminSettingsDisplayViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminSettingsDisplayView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Project, ProjectAdminSettingsDisplayView> {
	}

	private static ProjectAdminSettingsDisplayViewUiBinder ourUiBinder = GWT
			.create(ProjectAdminSettingsDisplayViewUiBinder.class);

	private static Driver driver = GWT.create(Driver.class);
	private static ProjectAdminSettingsDisplayView instance;

	public static ProjectAdminSettingsDisplayView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminSettingsDisplayView();
		}
		return instance;
	}

	public static interface Presenter {
		Project getProject();

		void onEdit();
	}

	@UiField
	@Path("name")
	Label projectName;
	@UiField
	@Path("description")
	Label projectDescription;
	@UiField
	@Ignore
	RadioButton privacyPrivateOption;
	@UiField
	@Ignore
	RadioButton privacyPublicOption;
	@UiField
	Anchor editButton;

	private Presenter presenter;

	public ProjectAdminSettingsDisplayView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		privacyPublicOption.setValue(presenter.getProject().getPublic());
		privacyPrivateOption.setValue(!presenter.getProject().getPublic());
		driver.edit(presenter.getProject());
	}

	@UiHandler("editButton")
	void onEdit(ClickEvent event) {
		presenter.onEdit();
	}
}
