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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

/**
 * A project is the central concept behind a project that includes source, issue tracking, builds, etc.
 * 
 */
@Entity
public class Project extends BaseEntity {
	private String name;
	private String identifier;
	private String description;
	private Boolean isPublic;

	private List<ProjectProfile> projectProfiles = new ArrayList<ProjectProfile>();
	private List<ScmRepository> repositories = new ArrayList<ScmRepository>();

	private ProjectServiceProfile projectServiceProfile;

	public Project() {
		super();

		// Default constructor, does nothing.
	}

	/**
	 * The name of the project. Every project must have a name that is unique system-wide.
	 */
	@Basic(optional = false)
	@Column(unique = true, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The short name of the project, used to identify it in URLs.
	 */
	@Basic(optional = false)
	@Column(unique = true, nullable = false)
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * an optional description
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * the profiles that participate in this project
	 */
	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "project")
	public List<ProjectProfile> getProjectProfiles() {
		return projectProfiles;
	}

	public void setProjectProfiles(List<ProjectProfile> projectProfiles) {
		this.projectProfiles = projectProfiles;
	}

	/**
	 * Add the given profile to this project. Returns an existing project profile if one exists, or creates a new one.
	 * The resulting ProjectProfile may need persisting in the entity manager.
	 */
	public ProjectProfile addProfile(Profile profile) {
		for (ProjectProfile projectProfile : getProjectProfiles()) {
			if (projectProfile.getProfile().equals(profile)) {
				return projectProfile;
			}
		}
		ProjectProfile projectProfile = new ProjectProfile();
		projectProfile.setProject(this);
		projectProfile.setProfile(profile);
		getProjectProfiles().add(projectProfile);
		profile.getProjectProfiles().add(projectProfile);
		return projectProfile;
	}

	@OneToOne(cascade = { CascadeType.PERSIST }, optional = true)
	@JoinColumn(nullable = true)
	public ProjectServiceProfile getProjectServiceProfile() {
		return projectServiceProfile;
	}

	public void setProjectServiceProfile(ProjectServiceProfile projectServiceProfile) {
		this.projectServiceProfile = projectServiceProfile;
	}

	/**
	 * compute the {@link #getIdentifier() identifier} from the {@link #getName() name}.
	 */
	public void computeIdentifier() {
		if (getName() != null) {
			// CAREFUL: don't introduce new allowable characters here without considering tenancy and database names
			setIdentifier(getName().trim().replaceAll("[^a-zA-Z0-9\\.]", "-").toLowerCase());
		}
	}

	@Column(name = "is_public", nullable = false)
	public Boolean getPublic() {
		return isPublic;
	}

	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, mappedBy = "project")
	@OrderBy("url")
	public List<ScmRepository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<ScmRepository> repositories) {
		this.repositories = repositories;
	}

	@Transient
	public int getNumWatchers() {
		int result = 0;
		for (ProjectProfile pp : getProjectProfiles()) {
			if (pp.getCommunity()) {
				result++;
			}
		}
		return result;
	}

	@Transient
	public int getNumCommitters() {
		int result = 0;
		for (ProjectProfile pp : getProjectProfiles()) {
			if (pp.getOwner() || pp.getUser()) {
				result++;
			}
		}
		return result;
	}
}
