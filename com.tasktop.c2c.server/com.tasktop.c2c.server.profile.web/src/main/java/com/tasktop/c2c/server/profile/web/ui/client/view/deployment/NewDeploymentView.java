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
package com.tasktop.c2c.server.profile.web.ui.client.view.deployment;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;

public class NewDeploymentView extends Composite {
	interface Binder extends UiBinder<Widget, NewDeploymentView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	TextBox name;
	@UiField
	CredentialsEditView credentialsEditView;
	@UiField
	public Button saveButton;
	@UiField
	Button cancelButton;

	public NewDeploymentView() {
		initWidget(uiBinder.createAndBindUi(this));
		credentialsEditView.setUserUrlEditable(true);
	}

	public DeploymentConfiguration getValue() {
		DeploymentConfiguration value = new DeploymentConfiguration();
		value.setName(name.getText().trim());

		credentialsEditView.updateValue(value);

		return value;
	}

	public void setCredentialsValid(boolean valid) {
		credentialsEditView.setCredentialsValid(valid);
		setSaveEnabled(valid);
	}

	public void addValidatePasswordClickHandler(ClickHandler handler) {
		credentialsEditView.validatePasswordButton.addClickHandler(handler);
	}

	/**
	 * 
	 */
	public void clear() {
		name.setText("");
		credentialsEditView.clear();
		setSaveEnabled(false);
	}

	private void setSaveEnabled(boolean enabled) {
		saveButton.setEnabled(enabled);
		if (enabled) {
			saveButton.removeStyleName("disabled");
		} else {
			saveButton.addStyleName("disabled");
		}
	}

}
