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
package com.tasktop.c2c.server.auth.service;

import java.util.List;

public interface InternalAuthenticationService {

	/**
	 * create a compound role scoped by the given project identity
	 */
	public String toCompoundRole(String roleName, String projectIdentifier);

	/**
	 * get the role name from the compound role, if it applies to the given project identity
	 * 
	 * @param compoundRole
	 *            the compound role from {@link #toCompoundRole(String, String)}
	 * @param projectIdentifier
	 *            the project identity
	 * @return the role name if the given compound role applies to the given project identity, otherwise null.
	 */
	public String fromCompoundRole(String compoundRole, String projectIdentifier);

	/**
	 * get the role name from the compound role, if it applies to the given project identity
	 * 
	 * @param compoundRoles
	 *            the compound roles from {@link #toCompoundRole(String, String)}
	 * @param projectIdentifier
	 *            the project identity
	 * @return the role names that apply to the given project identity
	 */
	public List<String> fromCompoundRole(List<String> compoundRoles, String projectIdentifier);

	/**
	 * specialize an authentication token for a specific project identity.
	 * 
	 * @param originalToken
	 *            the token to specialize
	 * @param projectIdentifier
	 *            the application identity
	 * @param projectIsPublic
	 *            whether the application is public
	 * @return the new token
	 */
	public AuthenticationToken specializeAuthenticationToken(AuthenticationToken originalToken,
			String projectIdentifier, boolean projectIsPublic);

	/**
	 * Replace the current security context with one authorized for Role.User and Role.System access for the given
	 * project.
	 * 
	 * @param projectIdentifier
	 */
	void assumeSystemIdentity(String projectIdentifier);

}
