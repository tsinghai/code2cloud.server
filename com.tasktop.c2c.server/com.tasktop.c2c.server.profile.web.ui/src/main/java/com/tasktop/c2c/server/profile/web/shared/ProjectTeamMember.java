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
package com.tasktop.c2c.server.profile.web.shared;

import java.io.Serializable;
import java.util.Set;

@SuppressWarnings("serial")
public class ProjectTeamMember implements Serializable, Comparable<ProjectTeamMember> {
	private Profile profile;
	private Set<ProjectRole> roles;

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Set<ProjectRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<ProjectRole> roles) {
		this.roles = roles;
	}

	@Override
	public int compareTo(ProjectTeamMember o) {
		return profile.compareTo(o.profile);
	}

}
