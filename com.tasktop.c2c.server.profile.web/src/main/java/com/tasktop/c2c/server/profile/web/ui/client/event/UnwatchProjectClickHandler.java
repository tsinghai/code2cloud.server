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
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public abstract class UnwatchProjectClickHandler implements ClickHandler {

	private final Project project;

	public UnwatchProjectClickHandler(Project newProject) {
		project = newProject;
	}

	@Override
	public final void onClick(ClickEvent event) {
		ProfileEntryPoint.getInstance().getProfileService()
				.unwatchProject(project.getIdentifier(), new AsyncCallbackSupport<Void>() {
					@Override
					protected void success(Void result) {
						// Remove the watcher role from our session so that the watch button renders
						// correctly - this saves another server roundtrip, and the next page
						// navigation will re-pull roles from the server anyways.
						Credentials credentials = ProfileEntryPoint.getInstance().getAppState().getCredentials();
						credentials.getRoles().remove(Role.Community + "/" + project.getIdentifier());

						// Allow implementers to react to this unwatch.
						onUnwatchSuccess(project);
					}
				});
	}

	protected abstract void onUnwatchSuccess(Project project);
}
