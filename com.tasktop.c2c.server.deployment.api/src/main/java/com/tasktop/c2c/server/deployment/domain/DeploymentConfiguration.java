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
package com.tasktop.c2c.server.deployment.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tasktop.c2c.server.profile.domain.project.AbstractEntity;

/**
 * Represents a configuration for a deployment.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class DeploymentConfiguration extends AbstractEntity {
	private String name;
	private String description;

	// Credentials
	private String apiBaseUrl;
	private String username;
	private String password; // Not stored!
	private String apiToken;
	private String errorString;

	// CF Configuration
	private List<CloudService> services;
	private List<String> mappedUrls;
	private int numInstances;
	private int memory;

	// C2C Configuration
	private DeploymentType deploymentType;
	private String buildJobName;
	private String buildJobNumber;
	private String buildArtifactPath; // if deploymentType is AUTOMATIC
	private String artifactUrl; // if deploymentType is MANUAL
	private Date lastDeploymentDate;
	private boolean deployUnstableBuilds; // if deploymentType is AUTOMATIC. If true the we deploy even if tests fail.

	// Status
	private DeploymentStatus status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public List<CloudService> getServices() {
		return services;
	}

	public void setServices(List<CloudService> services) {
		this.services = services;
	}

	public List<String> getMappedUrls() {
		return mappedUrls;
	}

	public void setMappedUrls(List<String> mappedUrls) {
		this.mappedUrls = mappedUrls;
	}

	public int getNumInstances() {
		return numInstances;
	}

	public void setNumInstances(int numInstances) {
		this.numInstances = numInstances;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public DeploymentType getDeploymentType() {
		return deploymentType;
	}

	public void setDeploymentType(DeploymentType deploymentType) {
		this.deploymentType = deploymentType;
	}

	public String getBuildJobName() {
		return buildJobName;
	}

	public void setBuildJobName(String buildJobName) {
		this.buildJobName = buildJobName;
	}

	public String getBuildArtifactPath() {
		return buildArtifactPath;
	}

	public void setBuildArtifactPath(String buildArtifactPath) {
		this.buildArtifactPath = buildArtifactPath;
	}

	public boolean isDeployUnstableBuilds() {
		return deployUnstableBuilds;
	}

	public void setDeployUnstableBuilds(boolean deployUnstableBuilds) {
		this.deployUnstableBuilds = deployUnstableBuilds;
	}

	public DeploymentStatus getStatus() {
		return status;
	}

	public void setStatus(DeploymentStatus status) {
		this.status = status;
	}

	public String getApiBaseUrl() {
		return apiBaseUrl;
	}

	public void setApiBaseUrl(String apiBaseUrl) {
		this.apiBaseUrl = apiBaseUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBuildJobNumber() {
		return buildJobNumber;
	}

	public void setBuildJobNumber(String buildJobNumber) {
		this.buildJobNumber = buildJobNumber;
	}

	/**
	 * @return
	 */
	public DeploymentConfiguration copy() {
		// Error string not copied
		DeploymentConfiguration copy = new DeploymentConfiguration();
		copy.apiBaseUrl = this.apiBaseUrl;
		copy.apiToken = this.apiToken;
		copy.buildArtifactPath = this.buildArtifactPath;
		copy.artifactUrl = this.artifactUrl;
		copy.buildJobName = this.buildJobName;
		copy.buildJobNumber = this.buildJobNumber;
		copy.deploymentType = this.deploymentType;
		copy.deployUnstableBuilds = this.deployUnstableBuilds;
		copy.description = this.description;
		copy.id = this.id;
		copy.lastDeploymentDate = this.lastDeploymentDate;
		if (this.mappedUrls != null) {
			copy.mappedUrls = new ArrayList<String>(this.mappedUrls);
		}
		copy.memory = this.memory;
		copy.name = this.name;
		copy.numInstances = this.numInstances;
		copy.password = this.password;
		if (this.services != null) {
			copy.services = new ArrayList<CloudService>(this.services);
		}
		copy.status = this.status;
		copy.username = this.username;
		return copy;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}

	public boolean hasError() {
		return this.errorString != null && this.errorString.length() > 0;
	}

	/**
	 * @return the artifactUrl
	 */
	public String getArtifactUrl() {
		return artifactUrl;
	}

	/**
	 * @param artifactUrl
	 *            the artifactUrl to set
	 */
	public void setArtifactUrl(String artifactUrl) {
		this.artifactUrl = artifactUrl;
	}

	/**
	 * @return the lastDeploymentDate
	 */
	public Date getLastDeploymentDate() {
		return lastDeploymentDate;
	}

	/**
	 * @param lastDeploymentDate
	 *            the lastDeploymentDate to set
	 */
	public void setLastDeploymentDate(Date lastDeploymentDate) {
		this.lastDeploymentDate = lastDeploymentDate;
	}

}
