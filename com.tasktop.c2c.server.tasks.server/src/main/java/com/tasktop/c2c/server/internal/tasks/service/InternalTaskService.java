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
package com.tasktop.c2c.server.internal.tasks.service;


import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public interface InternalTaskService {
	/**
	 * Provision an {@link Profile account} for the given authentication token. Guarantees that a profile exists for the
	 * given username, and that the profile is up to date with respect to that user's information (email, name etc.)
	 */
	public Profile provisionAccount(AuthenticationToken authenticationToken);

	/**
	 * Provision an {@link Profile account} for the given user profile
	 */
	public Profile provisionAccount(TaskUserProfile taskUserProfile);

	/**
	 * get the profile associated with the {@link Security#getCurrentUser() current user}
	 */
	public Profile getCurrentUserProfile();

	/**
	 * Finds a {@link Profile profile} by the given username
	 * 
	 * @param username
	 *            The username of the Profile to find
	 * @return The retrieved Profile
	 * @throws EntityNotFoundException
	 *             If the specified Profile does not exist
	 */
	public Profile findProfile(String username) throws EntityNotFoundException;
}
