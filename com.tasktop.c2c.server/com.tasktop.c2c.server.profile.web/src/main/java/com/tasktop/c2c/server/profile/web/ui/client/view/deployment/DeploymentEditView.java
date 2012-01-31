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

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.ArtifactEditView.IsDirtyHandler;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.ArtifactEditView.JobNameChangedHandler;

public class DeploymentEditView extends Composite {
	interface Binder extends UiBinder<Widget, DeploymentEditView> {
	}

	public interface EditStartHandler {
		void editStarted(DeploymentConfiguration config);
	}

	private static final String DEFAULT_SAVE_TEXT = "Save";
	private static final String SAVE_AND_DEPLOY_TEXT = "Save and Deploy";

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label title;

	@UiField
	Anchor saveButton1;
	@UiField
	Anchor cancelButton1;
	@UiField
	Anchor saveButton2;
	@UiField
	Anchor cancelButton2;

	@UiField
	CredentialsEditView credentialsEditView;
	@UiField
	SettingsEditView settingsEditView;
	@UiField
	ServicesEditView servicesEditView;
	@UiField
	ArtifactEditView artifactEditView;

	private DeploymentConfiguration originalValue;
	private EditStartHandler editStartHandler;

	public DeploymentEditView() {
		initWidget(uiBinder.createAndBindUi(this));

		artifactEditView.setIsDirtyHandler(new IsDirtyHandler() {

			@Override
			public void isDirty(boolean isAutoDeploy) {
				if (isAutoDeploy) {
					setSaveButtonText(DEFAULT_SAVE_TEXT);
				} else {
					setSaveButtonText(SAVE_AND_DEPLOY_TEXT);
				}
			}
		});
	}

	private void setSaveButtonText(String newText) {
		this.saveButton1.setText(newText);
		this.saveButton2.setText(newText);
	}

	public DeploymentConfiguration getOriginalValue() {
		return this.originalValue;
	}

	public DeploymentConfiguration getValue() {
		DeploymentConfiguration value = originalValue.copy();

		credentialsEditView.updateValue(value);
		servicesEditView.updateValue(value);
		settingsEditView.updateValue(value);
		artifactEditView.updateValue(value);

		return value;
	}

	public void setCredentialsValid(boolean valid) {
		credentialsEditView.setCredentialsValid(valid);
	}

	public void setValue(DeploymentConfiguration deployment) {
		this.originalValue = deployment;
		title.setText(deployment.getName());
		credentialsEditView.setValue(deployment);
		settingsEditView.setValue(deployment);
		servicesEditView.setValue(deployment);
		artifactEditView.setValue(deployment);

		saveButton1.setText(DEFAULT_SAVE_TEXT);
		editStartHandler.editStarted(deployment);
	}

	public void addUpdateClickHandler(ClickHandler handler) {
		saveButton1.addClickHandler(handler);
		saveButton2.addClickHandler(handler);
	}

	public void addCancelClickHandler(ClickHandler handler) {
		cancelButton1.addClickHandler(handler);
		cancelButton2.addClickHandler(handler);
	}

	public void addValidatePasswordClickHandler(ClickHandler handler) {
		credentialsEditView.validatePasswordButton.addClickHandler(handler);
	}

	public void setMemoryValues(List<Integer> memoryValues) {
		settingsEditView.setMemoryValues(memoryValues);
	}

	public void setServices(List<CloudService> services) {
		servicesEditView.setServices(services);
	}

	public void setJobNames(List<String> jobNames) {
		artifactEditView.setJobNames(jobNames);
	}

	public void setBuilds(String jobName, List<BuildDetails> builds) {
		artifactEditView.setBuilds(jobName, builds);
	}

	/**
	 * @param jobNameChangedHandler
	 */
	public void setBuildJobChangedHandler(JobNameChangedHandler jobNameChangedHandler) {
		artifactEditView.setJobNameChangedHandler(jobNameChangedHandler);

	}

	/**
	 * @param availableServiceConfigurations
	 */
	public void setServiceConfigurations(List<DeploymentServiceConfiguration> availableServiceConfigurations) {
		servicesEditView.setServiceConfigurations(availableServiceConfigurations);
	}

	/**
	 * @param editStartHandler
	 *            the editStartHandler to set
	 */
	public void setEditStartHandler(EditStartHandler editStartHandler) {
		this.editStartHandler = editStartHandler;
	}

}
