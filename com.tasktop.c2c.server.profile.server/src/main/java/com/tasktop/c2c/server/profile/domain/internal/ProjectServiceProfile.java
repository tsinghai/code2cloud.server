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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class ProjectServiceProfile extends BaseEntity {
	private List<ProjectService> projectServices = new ArrayList<ProjectService>();

	private boolean template;

	private Project project;

	@OneToOne(fetch = FetchType.EAGER, optional = true, mappedBy = "projectServiceProfile")
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * the services that run this project
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "projectServiceProfile")
	public List<ProjectService> getProjectServices() {
		return projectServices;
	}

	public void setProjectServices(List<ProjectService> projectServices) {
		this.projectServices = projectServices;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public ProjectServiceProfile createCopy() {
		ProjectServiceProfile copy = new ProjectServiceProfile();

		for (ProjectService serviceTemplate : getProjectServices()) {
			ProjectService projectService = new ProjectService();
			projectService.setType(serviceTemplate.getType());
			projectService.setInternalPort(serviceTemplate.getInternalPort());
			projectService.setInternalProtocol(serviceTemplate.getInternalProtocol());
			projectService.setInternalUriPrefix(serviceTemplate.getInternalUriPrefix());
			projectService.setUriPattern(serviceTemplate.getUriPattern());
			projectService.setAjpPort(serviceTemplate.getAjpPort());
			projectService.setExternalUrl(serviceTemplate.getExternalUrl());
			copy.add(projectService);
		}
		return copy;
	}

	public void add(ProjectService service) {
		service.setProjectServiceProfile(this);
		getProjectServices().add(service);
	}
}
