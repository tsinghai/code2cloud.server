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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;

import java.util.List;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentService;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentService.AvailableBuildInformation;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDeploymentPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.DeploymentsView;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.ArtifactEditView.JobNameChangedHandler;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.DeploymentEditView.EditStartHandler;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.DeploymentReadOnlyView.DeleteHandler;

public class DeploymentsPresenter extends AbstractProfilePresenter implements SplittableActivity {

	private final DeploymentsView view;
	private String projectIdentifier;
	private List<DeploymentConfiguration> deploymentConfigurations;
	private DeploymentService.AvailableBuildInformation buildInformation;

	public DeploymentsPresenter(DeploymentsView view) {
		super(view);
		this.view = view;
	}

	public DeploymentsPresenter() {
		this(new DeploymentsView());
	}

	public void setPlace(Place place) {
		ProjectDeploymentPlace depPlace = (ProjectDeploymentPlace) place;
		this.projectIdentifier = depPlace.getProjectId();
		this.deploymentConfigurations = depPlace.getDeploymentConfigurations();
		this.buildInformation = depPlace.getBuildInformation();
	}

	@Override
	protected void bind() {
		if (AuthenticationHelper.hasRoleForProject(Role.Admin, projectIdentifier)
				|| AuthenticationHelper.hasRoleForProject(Role.User, projectIdentifier)) {
			view.setEnableEdit(true);
		} else {
			view.setEnableEdit(false);
		}

		view.setDeploymentConfigurations(deploymentConfigurations);
		if (!deploymentConfigurations.isEmpty()) {
			view.setSelectedConfig(deploymentConfigurations.get(0));
		}
		// Populate the initial dropdown of job names
		view.deploymentEditView.setJobNames(buildInformation.getBuildJobNames());

		view.deploymentEditView.setBuildJobChangedHandler(new JobNameChangedHandler() {

			@Override
			public void jobNameChanged(final String jobName) {
				getDeploymentService().getBuildInformation(projectIdentifier, jobName,
						new AsyncCallbackSupport<AvailableBuildInformation>() {

							@Override
							protected void success(AvailableBuildInformation result) {
								view.deploymentEditView.setBuilds(jobName, result.getBuilds());
							}

						});

			}
		});

		view.newDeploymentView.addValidatePasswordClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getDeploymentService().validateCredentials(view.newDeploymentView.getValue(),
						new AsyncCallbackSupport<Boolean>() {

							@Override
							protected void success(Boolean result) {
								view.newDeploymentView.setCredentialsValid(result);
							}
						});
			}
		});

		view.newDeploymentView.saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				OperationMessage message = new OperationMessage("Saving");
				getDeploymentService().createDeploymentConfiguration(projectIdentifier,
						view.newDeploymentView.getValue(), new AsyncCallbackSupport<DeploymentConfiguration>(message) {

							@Override
							protected void success(DeploymentConfiguration result) {
								if (result.hasError()) {
									ProfileGinjector.get.instance().getNotifier()
											.displayMessage(Message.createErrorMessage("Error while saving"));
								} else {
									ProfileGinjector.get.instance().getNotifier()
											.displayMessage(Message.createSuccessMessage("Saved"));
								}
								view.newDeploymentCreated(result);
							}
						});
			}
		});

		view.deploymentEditView.addValidatePasswordClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getDeploymentService().validateCredentials(view.deploymentEditView.getValue(),
						new AsyncCallbackSupport<Boolean>() {

							@Override
							protected void success(Boolean result) {
								view.deploymentEditView.setCredentialsValid(result);
							}
						});
			}
		});

		view.deploymentEditView.addUpdateClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getDeploymentService().updateDeployment(view.deploymentEditView.getValue(),
						new AsyncCallbackSupport<DeploymentConfiguration>(new OperationMessage("Saving")) {

							@Override
							protected void success(DeploymentConfiguration result) {
								if (result.hasError()) {
									ProfileGinjector.get.instance().getNotifier()
											.displayMessage(Message.createErrorMessage("Error while saving"));
								} else {
									ProfileGinjector.get.instance().getNotifier()
											.displayMessage(Message.createSuccessMessage("Saved"));
								}
								view.updated(result);
							}
						});
			}
		});

		view.deploymentEditView.setEditStartHandler(new EditStartHandler() {

			@Override
			public void editStarted(final DeploymentConfiguration config) {

				getDeploymentService().getDeploymentConfigurationOptions(projectIdentifier, config,
						new AsyncCallbackSupport<DeploymentService.DeploymentConfigurationOptions>() {

							@Override
							protected void success(DeploymentService.DeploymentConfigurationOptions result) {

								view.deploymentEditView.setMemoryValues(result.getAvailableMemories());
								view.deploymentEditView.setServices(result.getAvailableServices());
								view.deploymentEditView.setServiceConfigurations(result
										.getAvailableServiceConfigurations());
								if (result.getBuildInformation() != null) {
									view.deploymentEditView.setBuilds(config.getBuildJobName(), result
											.getBuildInformation().getBuilds());
								}

							}
						});

			}
		});

		view.deploymentReadOnlyView.addDeleteHandler(new DeleteHandler() {

			@Override
			public void delete(final DeploymentConfiguration config, boolean alsoDeleteFromCF) {
				OperationMessage message = new OperationMessage("Deleting");
				message.setSuccessText("Deleted");
				getDeploymentService().deleteDeployment(config, alsoDeleteFromCF,
						new AsyncCallbackSupport<Void>(message) {

							@Override
							protected void success(Void result) {
								view.deleted(config);
							}
						});

			}
		});

		view.deploymentReadOnlyView.addStartClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				OperationMessage message = new OperationMessage("Starting");
				message.setSuccessText("Started");
				final DeploymentConfiguration configuration = view.deploymentReadOnlyView.getOriginalValue();
				getDeploymentService().startDeployment(configuration,
						new AsyncCallbackSupport<DeploymentStatus>(message) {

							@Override
							protected void success(DeploymentStatus result) {
								view.updateStatus(configuration, result);
							}
						});
			}
		});

		view.deploymentReadOnlyView.addStopClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				OperationMessage message = new OperationMessage("Stopping");
				message.setSuccessText("Stopped");
				final DeploymentConfiguration configuration = view.deploymentReadOnlyView.getOriginalValue();
				getDeploymentService().stopDeployment(configuration,
						new AsyncCallbackSupport<DeploymentStatus>(message) {

							@Override
							protected void success(DeploymentStatus result) {
								view.updateStatus(configuration, result);

							}
						});
			}
		});

		view.deploymentReadOnlyView.addRestartClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				OperationMessage message = new OperationMessage("Restarting");
				message.setSuccessText("Restarted");
				final DeploymentConfiguration configuration = view.deploymentReadOnlyView.getOriginalValue();
				getDeploymentService().restartDeployment(configuration,
						new AsyncCallbackSupport<DeploymentStatus>(message) {

							@Override
							protected void success(DeploymentStatus result) {
								view.updateStatus(configuration, result);

							}
						});
			}
		});

	}
}
