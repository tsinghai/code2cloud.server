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

import java.util.HashSet;
import java.util.Set;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractProjectPlace extends AbstractPreAuthorizingPlace implements BreadcrumbPlace {

	protected final String projectId;

	protected AbstractProjectPlace(Set<String> roles, String projectId) {
		super(roles);
		this.projectId = projectId;
	}

	@Override
	public Set<String> getRequiredRoles() {
		Set<String> roles = new HashSet<String>(roles());

		// We have a project-specific page, so we need to calculate the project roles.
		String projectId = getProjectId();
		Set<String> newRoles = new HashSet<String>();

		for (String role : roles) {
			// Convert this to the project specific role format (e.g. ROLE_USER/code2cloud)
			newRoles.add(role + "/" + projectId);
		}
		roles.addAll(newRoles);

		return roles;
	}

	public String getProjectId() {
		return projectId;
	}
}
