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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;

public class CredentialsEditView extends Composite {
	interface Binder extends UiBinder<Widget, CredentialsEditView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label serverUrl;
	@UiField
	Label username;
	@UiField
	TextBox serverUrlBox;
	@UiField
	TextBox usernameBox;
	@UiField
	PasswordTextBox password;
	@UiField
	public Anchor validatePasswordButton;

	@UiField
	Panel credentialsInvalidPanel;
	@UiField
	Panel credentialsValidPanel;

	public CredentialsEditView() {
		initWidget(uiBinder.createAndBindUi(this));
		clearValidationMessages();
		setUserUrlEditable(false);
	}

	public void setUserUrlEditable(boolean canEdit) {
		serverUrl.setVisible(!canEdit);
		username.setVisible(!canEdit);
		serverUrlBox.setVisible(canEdit);
		usernameBox.setVisible(canEdit);
	}

	public void setValue(DeploymentConfiguration deployment) {
		setValue(deployment, true);
		clearValidationMessages();
	}

	public void setCredentialsValid(boolean valid) {
		credentialsInvalidPanel.setVisible(!valid);
		credentialsValidPanel.setVisible(valid);
	}

	public void setValue(DeploymentConfiguration deployment, boolean fireEvents) {
		username.setText(deployment.getUsername());
		serverUrl.setText(deployment.getApiBaseUrl());
		password.setText("");
	}

	public void updateValue(DeploymentConfiguration value) {
		if (usernameBox.isVisible()) {
			value.setUsername(usernameBox.getText());
		}
		if (serverUrlBox.isVisible()) {
			value.setApiBaseUrl(serverUrlBox.getText());
		}
		if (!password.getValue().isEmpty()) {
			value.setPassword(password.getValue());
		}
	}

	public void clear() {
		serverUrlBox.setValue("http://api.cloudfoundry.com");
		usernameBox.setValue("");
		password.setValue("");
		clearValidationMessages();
	}

	private void clearValidationMessages() {
		credentialsValidPanel.setVisible(false);
		credentialsInvalidPanel.setVisible(false);
	}

}
