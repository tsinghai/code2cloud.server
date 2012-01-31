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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class NewProjectView extends Composite {

	private static NewProjectView instance = null;

	public static NewProjectView getInstance() {
		if (instance == null) {
			instance = new NewProjectView();
		}
		return instance;
	}

	interface NewProjectViewUiBinder extends UiBinder<HTMLPanel, NewProjectView> {
	}

	private static NewProjectViewUiBinder uiBinder = GWT.create(NewProjectViewUiBinder.class);

	@UiField
	public Panel newProjectForm;
	@UiField
	public TextBox name;
	@UiField
	public TextArea description;
	@UiField
	public Button createButton;
	@UiField
	public Button cancelButton;
	@UiField
	public Panel maxProjectsMessagePanel;

	@UiField
	public RadioButton privateProjectButton;

	@UiField
	public RadioButton publicProjectButton;

	private NewProjectView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void displayMaxProjectsMessage(boolean show) {
		// Only show one of these two controls at a time.
		this.maxProjectsMessagePanel.setVisible(show);
		this.newProjectForm.setVisible(!show);
	}

	/**
	 * 
	 */
	public void clear() {
		name.setValue("");
		description.setValue("");
		privateProjectButton.setValue(true);		
	}
}
