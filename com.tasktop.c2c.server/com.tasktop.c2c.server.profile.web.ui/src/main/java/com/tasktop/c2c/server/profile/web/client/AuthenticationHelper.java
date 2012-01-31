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
package com.tasktop.c2c.server.profile.web.client;

import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.shared.Credentials;

public class AuthenticationHelper {

	private AuthenticationHelper() {
		// No instantiation of this class.
	}

	public static boolean isAnonymous() {
		// If the credentials are null, then this user is anonymous.
		return (ProfileGinjector.get.instance().getAppState().getCredentials() == null);
	}

	public static boolean isAccountDisabled() {
		return !isAnonymous()
				&& ProfileGinjector.get.instance().getAppState().getCredentials().getProfile().getAccountDisabled();
	}

	public static boolean isWatching(String projectId) {
		// Check to see if I'm currently watching this project. If I am, I'll have the Community role for this
		// project already in my credentials.
		return AuthenticationHelper.hasRoleForProject(Role.Community, projectId);
	}

	public static boolean isAdmin(String projectId) {
		// Check to see if I'm currently an admin on this project. If I am, I'll have the Admin role for this
		// project already in my credentials.
		return AuthenticationHelper.hasRoleForProject(Role.Admin, projectId);
	}

	public static boolean isCommitter(String projectId) {
		// Check to see if I'm currently a committer on this project. If I am, I'll have the User role for this
		// project already in my credentials.
		return AuthenticationHelper.hasRoleForProject(Role.User, projectId);
	}

	/**
	 * indicate if the user has the specified role for a specific project
	 * 
	 * @param roleName
	 *            the role name
	 * @return true if and only if the user has the given role
	 * 
	 * @see #hasRole(String)
	 */
	public static boolean hasRoleForProject(String role, String projectIdentifier) {

		String projectRole = role + "/" + projectIdentifier;

		return hasRole(projectRole);
	}

	public static boolean hasRole(String roleName) {
		List<String> userRoles = null;

		Credentials creds = ProfileGinjector.get.instance().getAppState().getCredentials();
		if (creds != null) {
			userRoles = creds.getRoles();
		}

		return hasRole(roleName, userRoles);
	}

	/**
	 * indicate if the user has the specified role name, which is not related to any specific project
	 * 
	 * @param roleName
	 *            the role name
	 * @return true if and only if the user has the given role
	 * 
	 * @see #hasRoleForProject(String, String)
	 */
	public static boolean hasRole(String roleName, List<String> userRoles) {
		boolean hasRole = false;
		if (userRoles != null) {
			for (String userRole : userRoles) {
				if (userRole.equals(roleName)) {
					hasRole = true;
					break;
				}
			}
		}

		return hasRole;
	}

	public static List<String> getRolesForProject(Project project, List<String> globalRoles) {
		List<String> roles = new ArrayList<String>();
		String projectRoleSuffix = "/" + project.getIdentifier();
		if (globalRoles != null) {
			for (String globalRole : globalRoles) {
				if (globalRole.endsWith(projectRoleSuffix)) {
					roles.add(globalRole.substring(0, globalRole.length() - projectRoleSuffix.length()));
				}
			}
			if (project.getPublic()) {
				// logged-in and public
				roles.add(Role.Community);
			}
		}
		if (project.getPublic()) {
			roles.add(Role.Observer);
		}
		return roles;
	}
}
