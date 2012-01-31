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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;

public class DeploymentReadOnlyView extends Composite {
	interface Binder extends UiBinder<Widget, DeploymentReadOnlyView> {
	}

	public interface DeleteHandler {
		void delete(DeploymentConfiguration config, boolean alsoDeleteFromCF);
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label title;
	@UiField
	Label status;
	@UiField
	Anchor startButton;
	@UiField
	Anchor stopButton;
	@UiField
	Anchor restartButton;
	@UiField
	Anchor deleteButton;
	@UiField
	Anchor editButton;

	@UiField
	CredentialsReadOnlyView credentialsReadOnlyView;
	@UiField
	SettingsReadOnlyView settingsReadOnlyView;
	@UiField
	ServicesReadOnlyView servicesReadOnlyView;
	@UiField
	ArtifactReadOnlyView artifactReadOnlyView;

	private DeleteHandler deleteHandler;

	private DeploymentConfiguration originalValue;

	public DeploymentReadOnlyView() {
		initWidget(uiBinder.createAndBindUi(this));

		deleteButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final DeleteDeploymentDialog deleteDialog = new DeleteDeploymentDialog();
				deleteDialog.deleteButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						deleteHandler.delete(getOriginalValue(), deleteDialog.alsoDeleteInCF.getValue());
						deleteDialog.hide();

					}
				});
				deleteDialog.center();
				deleteDialog.show();
			}
		});

	}

	public DeploymentConfiguration getOriginalValue() {
		return this.originalValue;
	}

	public void setValue(DeploymentConfiguration deployment) {

		this.originalValue = deployment;

		title.setText(deployment.getName());

		updateStatus(deployment.getStatus());

		credentialsReadOnlyView.setValue(deployment);
		settingsReadOnlyView.setValue(deployment);
		servicesReadOnlyView.setValue(deployment);
		artifactReadOnlyView.setValue(deployment);
	}

	/**
	 * @param deployment
	 */
	public void updateStatus(DeploymentStatus deploymentStatus) {
		setEnabledWithStyle(startButton, true);
		setEnabledWithStyle(stopButton, true);
		setEnabledWithStyle(restartButton, true);

		if (deploymentStatus == null || deploymentStatus.getResult() == null) {
			status.setText("Unknown");
		} else {
			switch (deploymentStatus.getResult()) {
			case STARTED:
				status.setText("Started");
				setEnabledWithStyle(startButton, false);
				break;
			case STOPPED:
				status.setText("Stopped");
				setEnabledWithStyle(stopButton, false);
				setEnabledWithStyle(restartButton, false);
				break;
			case UPDATING:
				status.setText("Updating");
				break;
			}
		}
	}

	private void setEnabledWithStyle(Anchor button, boolean enabled) {
		if (enabled) {
			button.removeStyleName("disabled");
		} else {
			button.addStyleName("disabled");
		}
		button.setEnabled(enabled);
	}

	/**
	 * @param clickHandler
	 */
	public void addDeleteHandler(DeleteHandler deleteHandler) {
		this.deleteHandler = deleteHandler;
	}

	public void addStopClickHandler(ClickHandler clickHandler) {
		addEnabledRespectingClickHandler(stopButton, clickHandler);
	}

	public void addStartClickHandler(ClickHandler clickHandler) {
		addEnabledRespectingClickHandler(startButton, clickHandler);
	}

	public void addRestartClickHandler(ClickHandler clickHandler) {
		addEnabledRespectingClickHandler(restartButton, clickHandler);
	}

	private void addEnabledRespectingClickHandler(final Anchor a, final ClickHandler clickHandler) {
		a.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (a.isEnabled()) {
					clickHandler.onClick(event);
				}

			}
		});
	}

	/**
	 * @param editEnabled
	 */
	public void setEnableEdit(boolean editEnabled) {
		editButton.setVisible(editEnabled);
		startButton.setVisible(editEnabled);
		stopButton.setVisible(editEnabled);
		restartButton.setVisible(editEnabled);
		deleteButton.setVisible(editEnabled);
	}

}
