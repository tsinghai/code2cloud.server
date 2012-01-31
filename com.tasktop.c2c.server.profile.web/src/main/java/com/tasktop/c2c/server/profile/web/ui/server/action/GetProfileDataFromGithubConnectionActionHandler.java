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
package com.tasktop.c2c.server.profile.web.ui.server.action;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import com.tasktop.c2c.server.profile.web.shared.actions.GetProfileDataFromGitubConnectionAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProfileResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class GetProfileDataFromGithubConnectionActionHandler extends
		AbstractProfileActionHandler<GetProfileDataFromGitubConnectionAction, GetProfileResult> {

	public GetProfileDataFromGithubConnectionActionHandler() {
		super();
	}

	@Override
	public GetProfileResult execute(GetProfileDataFromGitubConnectionAction action, ExecutionContext context)
			throws DispatchException {

		com.tasktop.c2c.server.profile.domain.project.Profile profile = null;

		Connection<?> userConnection = ProviderSignInUtils.getConnection(RequestContextHolder.getRequestAttributes());
		if (userConnection != null) {
			UserProfile remoteProfile = userConnection.fetchUserProfile();
			profile = new com.tasktop.c2c.server.profile.domain.project.Profile();
			profile.setUsername(remoteProfile.getUsername());
			profile.setFirstName(remoteProfile.getFirstName());
			profile.setLastName(remoteProfile.getLastName());
			profile.setEmail(remoteProfile.getEmail());
		}

		return new GetProfileResult(profile);
	}

}
