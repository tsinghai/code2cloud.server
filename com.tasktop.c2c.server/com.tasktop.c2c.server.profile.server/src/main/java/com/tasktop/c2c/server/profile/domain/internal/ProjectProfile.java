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
package com.tasktop.c2c.server.profile.domain.internal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ProjectProfile extends BaseEntity {
	private Profile profile;
	private Project project;
	private Boolean owner = false;
	private Boolean user = false;
	private Boolean community = false;

	public ProjectProfile() {
	}

	@ManyToOne
	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@ManyToOne
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Column(name = "owner", nullable = false)
	public Boolean getOwner() {
		return owner;
	}

	public void setOwner(Boolean owner) {
		this.owner = owner;
	}

	@Column(name = "member", nullable = false)
	public Boolean getUser() {
		return user;
	}

	public void setUser(Boolean user) {
		this.user = user;
	}

	@Column(name = "community", nullable = false)
	public Boolean getCommunity() {
		return community;
	}

	public void setCommunity(Boolean community) {
		this.community = community;
	}

	public Boolean hasAnyRoles() {
		return getCommunity() || getUser() || getOwner();
	}
}
