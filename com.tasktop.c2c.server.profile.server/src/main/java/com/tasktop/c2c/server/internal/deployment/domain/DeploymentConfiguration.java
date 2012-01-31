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
package com.tasktop.c2c.server.internal.deployment.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


import com.tasktop.c2c.server.deployment.domain.DeploymentType;
import com.tasktop.c2c.server.profile.domain.internal.BaseEntity;
import com.tasktop.c2c.server.profile.domain.internal.Project;


/**
 * The stored DeploymentConfiguration.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Entity
public class DeploymentConfiguration extends BaseEntity {
	private Project project;
	private String name;

	// Credentials
	private String apiBaseUrl;
	private String username;
	private String apiToken;

	// C2C Configuration
	private DeploymentType deploymentType;
	private String buildJobName;
	private String buildJobNumber;
	private String buildArtifactPath; // if deploymentType is AUTOMATIC, then this can be a wildcard (ant-style)
	private Date lastDeploymentDate;
	private boolean deployUnstableBuilds; // if deploymentType is AUTOMATIC. If true the we deploy even if tests fail.

	@ManyToOne
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Column
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column
	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	@Column
	public String getApiBaseUrl() {
		return apiBaseUrl;
	}

	public void setApiBaseUrl(String apiBaseUrl) {
		this.apiBaseUrl = apiBaseUrl;
	}

	@Enumerated(EnumType.STRING)
	@Column
	public DeploymentType getDeploymentType() {
		return deploymentType;
	}

	public void setDeploymentType(DeploymentType deploymentType) {
		this.deploymentType = deploymentType;
	}

	@Column
	public String getBuildJobName() {
		return buildJobName;
	}

	public void setBuildJobName(String buildJobName) {
		this.buildJobName = buildJobName;
	}

	/**
	 * @return the buildJobNumber
	 */
	@Column
	public String getBuildJobNumber() {
		return buildJobNumber;
	}

	/**
	 * @param buildJobNumber
	 *            the buildJobNumber to set
	 */
	public void setBuildJobNumber(String buildJobNumber) {
		this.buildJobNumber = buildJobNumber;
	}

	@Column
	public String getBuildArtifactPath() {
		return buildArtifactPath;
	}

	public void setBuildArtifactPath(String buildArtifactPath) {
		this.buildArtifactPath = buildArtifactPath;
	}

	@Column
	public boolean isDeployUnstableBuilds() {
		return deployUnstableBuilds;
	}

	public void setDeployUnstableBuilds(boolean deployUnstableBuilds) {
		this.deployUnstableBuilds = deployUnstableBuilds;
	}

	/**
	 * @return the lastDeploymentDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column
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
