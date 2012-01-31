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
package com.tasktop.c2c.server.profile.web.ui.client.event;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.shared.Profile;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ConfirmLeaveProjectDialog;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public abstract class LeaveProjectClickHandler implements ClickHandler {

	private final Project project;

	public LeaveProjectClickHandler(Project newProject) {
		project = newProject;
	}

	@Override
	public final void onClick(ClickEvent event) {

		final ConfirmLeaveProjectDialog confirm = new ConfirmLeaveProjectDialog();
		confirm.leaveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				confirm.hide();
				// Create the necessary object structure for this call, and plug in our profile ID.
				Profile profile = new Profile();
				ProjectTeamMember member = new ProjectTeamMember();
				member.setProfile(profile);
				profile.setId(ProfileEntryPoint.getInstance().getAppState().getCredentials().getProfile().getId());

				ProfileEntryPoint.getInstance().getProfileService()
						.removeTeamMember(project.getIdentifier(), member, new AsyncCallbackSupport<Boolean>() {
							@Override
							protected void success(Boolean result) {
								// Remove the user role into our session - this saves another server roundtrip, and the
								// next
								// page navigation will re-pull roles from the server anyways.
								Credentials credentials = ProfileEntryPoint.getInstance().getAppState()
										.getCredentials();
								credentials.getRoles().remove(Role.User + "/" + project.getIdentifier());

								// Allow implementers to react to this leave event.
								onLeaveSuccess(project);
							}
						});
			}
		});
		confirm.center();

	}

	protected abstract void onLeaveSuccess(Project project);
}
