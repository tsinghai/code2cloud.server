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
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ProjectInvitationView;

public class ProjectInvitationPresenter extends AbstractProfilePresenter {

	private final ProjectInvitationView view;
	private final String invitationToken;
	private final Project project;

	public ProjectInvitationPresenter(ProjectInvitationView invitationView, String token, Project project) {
		super(invitationView);
		this.view = invitationView;
		this.invitationToken = token;
		this.project = project;
	}

	@Override
	protected void bind() {
		configureForProject(project);
	}

	private void configureForProject(Project project) {
		view.setProject(project);
		view.acceptInviteAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doAccept();
			}
		});
	}

	private void doAccept() {
		getProfileService().acceptInvitation(invitationToken, new AsyncCallbackSupport<Void>() {

			@Override
			protected void success(Void result) {
				// FIXME redirect to project home
				ProjectsDiscoverPlace.createPlace()
						.displayOnArrival(Message.createSuccessMessage("Invitation accepted")).go();
			}
		});
	}
}
