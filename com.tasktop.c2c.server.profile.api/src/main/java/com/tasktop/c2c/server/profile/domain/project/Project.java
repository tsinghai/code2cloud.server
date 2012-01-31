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
package com.tasktop.c2c.server.profile.domain.project;

import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.cloud.domain.ServiceType;

@SuppressWarnings("serial")
public class Project extends AbstractEntity {
	private String identifier;
	private String name;
	private String description;
	private Boolean isPublic;
	private List<ProjectService> projectServices;
	private Integer numWatchers;
	private Integer numCommiters;

	public Project() {
		// Default constructor, does nothing.
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ProjectService> getProjectServices() {
		return projectServices;
	}

	public void setProjectServices(List<ProjectService> projectServices) {
		this.projectServices = projectServices;
	}

	public List<ProjectService> getProjectServicesOfType(ServiceType type) {
		List<ProjectService> results = new ArrayList<ProjectService>();

		for (ProjectService service : projectServices) {
			if (service.getServiceType().equals(type)) {
				results.add(service);
			}
		}
		return results;
	}

	public Boolean getPublic() {
		return isPublic;
	}

	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the numWatchers
	 */
	public Integer getNumWatchers() {
		return numWatchers;
	}

	/**
	 * @param numWatchers
	 *            the numWatchers to set
	 */
	public void setNumWatchers(Integer numWatchers) {
		this.numWatchers = numWatchers;
	}

	/**
	 * @return the numCommiters
	 */
	public Integer getNumCommiters() {
		return numCommiters;
	}

	/**
	 * @param numCommiters
	 *            the numCommiters to set
	 */
	public void setNumCommiters(Integer numCommiters) {
		this.numCommiters = numCommiters;
	}

}
