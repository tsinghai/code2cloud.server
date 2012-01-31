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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.tasktop.c2c.server.profile.domain.project.Project;

public class ProjectAdminSettingsEditView extends Composite implements Editor<Project> {
	interface ProjectAdminSettingsEditViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminSettingsEditView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Project, ProjectAdminSettingsEditView> {
	}

	private static ProjectAdminSettingsEditViewUiBinder ourUiBinder = GWT
			.create(ProjectAdminSettingsEditViewUiBinder.class);

	private static ProjectAdminSettingsEditView instance;
	public Driver driver = GWT.create(Driver.class);

	public static ProjectAdminSettingsEditView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminSettingsEditView();
		}
		return instance;
	}

	public static interface Presenter {
		Project getProject();

		void onSaveProject();

		void onCancelProjectEdit();
	}

	@UiField
	@Path("public")
	RadioButton privacyPublicOption;
	@UiField
	@Ignore
	RadioButton privacyPrivateOption;
	@UiField
	@Path("description")
	TextArea projectDescription;
	@UiField
	@Path("name")
	TextBox projectName;
	@UiField
	Button cancelEditButton;
	@UiField
	Button saveButton;

	private Presenter presenter;

	public ProjectAdminSettingsEditView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		privacyPublicOption.setValue(presenter.getProject().getPublic());
		privacyPrivateOption.setValue(!presenter.getProject().getPublic());
		driver.edit(presenter.getProject());
	}

	@UiHandler("privacyPrivateOption")
	void onPrivateSelected(ValueChangeEvent<Boolean> event) {
		privacyPublicOption.setValue(!event.getValue());
	}

	@UiHandler("privacyPublicOption")
	void onPublicSelected(ValueChangeEvent<Boolean> event) {
		privacyPrivateOption.setValue(!event.getValue());
	}

	@UiHandler("cancelEditButton")
	void onCancelEdit(ClickEvent event) {
		presenter.onCancelProjectEdit();
	}

	@UiHandler("saveButton")
	void onSave(ClickEvent event) {
		if (driver.isDirty()) {
			driver.flush();
			presenter.onSaveProject();
		} else {
			presenter.onCancelProjectEdit();
		}
	}
}
