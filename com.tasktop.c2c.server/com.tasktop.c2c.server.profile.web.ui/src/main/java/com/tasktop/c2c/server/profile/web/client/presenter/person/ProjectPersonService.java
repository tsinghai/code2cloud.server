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
package com.tasktop.c2c.server.profile.web.client.presenter.person;

import com.tasktop.c2c.server.common.web.client.widgets.chooser.ValueSuggestionService;

public class ProjectPersonService extends AbstractPersonService implements ValueSuggestionService {

	private String projectIdentifier;

	public ProjectPersonService() {
	}

	public ProjectPersonService(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	public String getProjectIdentifier() {
		return projectIdentifier;
	}

	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public void suggest(String query, int limit, Callback callback) {
		getProfileService().getProfiles(projectIdentifier, query, limit, new ProfilesCallback(callback));
	}

	@Override
	public void suggest(Callback callback) {
		getProfileService().getProfiles(projectIdentifier, new ProfilesCallback(callback));
	}

}
