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


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.NewProjectPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.NewProjectView;

public class NewProjectPresenter extends AbstractProfilePresenter {

	private final NewProjectView view;

	public NewProjectPresenter(NewProjectView projectsView, NewProjectPlace place) {
		super(projectsView);
		this.view = projectsView;

		view.clear();
		// Set the createAvailable flag on our view
		view.displayMaxProjectsMessage(!(place.isCreateAvailable()));

		view.createButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doCreateProject();
			}
		});

		view.cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doCancel();
			}
		});

	}

	@Override
	protected void bind() {

	}

	private void doCreateProject() {
		Project project = new Project();
		project.setName(view.name.getText());
		project.setDescription(view.description.getText());
		project.setPublic(view.publicProjectButton.getValue());

		getProfileService().createProject(getAppState().getCredentials(), project,
				new AsyncCallbackSupport<String>(new OperationMessage("Creating project...")) {
					@Override
					protected void success(final String projectIdentifier) {
						ProjectHomePlace
								.createPlace(projectIdentifier)
								.displayOnArrival(
										Message.createSuccessMessage("Project created! Provisioning project services..."))
								.go();
					}

				});
	}

	private void doCancel() {
		ProjectsDiscoverPlace.createPlace().go();
	}
}
