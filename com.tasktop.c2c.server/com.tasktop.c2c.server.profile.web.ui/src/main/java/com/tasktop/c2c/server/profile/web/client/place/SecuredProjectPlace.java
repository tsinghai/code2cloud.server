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
package com.tasktop.c2c.server.profile.web.client.place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;

/**
 * @deprecated. Instead use the batching pattern
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public abstract class SecuredProjectPlace extends AbstractProjectPlace {

	private final ProfileGinjector injector = ProfileGinjector.get.instance();

	protected SecuredProjectPlace(Set<String> roles, String projectId) {
		super(roles, projectId);
	}

	@Override
	protected void seekPermission() {
		final List<String> userRoles = new ArrayList<String>();

		if (!injector.getAppState().isUserAnonymous()) {
			userRoles.addAll(injector.getAppState().getCredentials().getRoles());
		}
		injector.getProfileService().getRolesForProject(projectId, new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				userRoles.addAll(Arrays.asList(result));
				authorise(userRoles);
			}

			@Override
			public void onFailure(Throwable caught) {
				authorise(userRoles);
			}
		});
	}

	private void authorise(List<String> userRoles) {
		Set<String> placeRoles = getRequiredRoles();

		// If this place has no roles, then it's wide open and the user can visit this page
		boolean hasRole = (placeRoles.size() == 0);

		for (String curRole : placeRoles) {
			if (AuthenticationHelper.hasRole(curRole, userRoles)) {
				hasRole = true;

				// Bail out, we're done.
				break;
			}
		}

		if (hasRole) {
			super.seekPermission();
		} else {
			onNotAuthorised();
		}
	}

}
