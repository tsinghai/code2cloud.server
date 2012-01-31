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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.Project;

public class ProjectDetailsAdminView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, ProjectDetailsAdminView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public TextBox name;
	@UiField
	public TextArea description;
	@UiField
	public RadioButton privateProjectButton;
	@UiField
	public RadioButton publicProjectButton;
	@UiField
	public Button saveButton;

	private Project project;

	public ProjectDetailsAdminView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setProject(Project project) {
		this.project = project;
		updateUi();
	}

	public Project getProject() {
		project.setName(name.getText());
		project.setDescription(description.getText());
		project.setPublic(publicProjectButton.getValue());
		return project;
	}

	private void updateUi() {
		publicProjectButton.setValue(project.getPublic());
		privateProjectButton.setValue(!(project.getPublic()));
		name.setText(project.getName());
		description.setText(project.getDescription());
	}
}
