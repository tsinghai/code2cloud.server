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
package com.tasktop.c2c.server.internal.deployment.service;

import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;

/**
 * Helper class for translating between public and internal data-storage objects.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class DeploymentDomain {

	public static DeploymentConfiguration convertToPublic(
			com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration source) {
		DeploymentConfiguration result = new DeploymentConfiguration();
		result.setId(source.getId());
		result.setApiToken(source.getApiToken());
		result.setApiBaseUrl(source.getApiBaseUrl());
		result.setName(source.getName());
		result.setUsername(source.getUsername());

		result.setDeploymentType(source.getDeploymentType());
		result.setBuildJobName(source.getBuildJobName());
		result.setBuildJobNumber(source.getBuildJobNumber());
		result.setBuildArtifactPath(source.getBuildArtifactPath());
		result.setDeployUnstableBuilds(source.isDeployUnstableBuilds());
		result.setLastDeploymentDate(source.getLastDeploymentDate());
		// TODO: result.setDescription(description)

		// TODO -- other fields
		return result;
	}

	public static com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration convertToInternal(
			DeploymentConfiguration source) {
		com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration result = new com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration();
		updateInternal(source, result);

		return result;
	}

	public static void updateInternal(DeploymentConfiguration source,
			com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration target) {
		target.setApiToken(source.getApiToken());
		target.setApiBaseUrl(source.getApiBaseUrl());
		target.setName(source.getName());
		target.setUsername(source.getUsername());

		target.setBuildJobName(source.getBuildJobName());
		target.setBuildJobNumber(source.getBuildJobNumber());
		target.setDeploymentType(source.getDeploymentType());
		target.setBuildArtifactPath(source.getBuildArtifactPath());
		target.setDeployUnstableBuilds(source.isDeployUnstableBuilds());
		target.setLastDeploymentDate(source.getLastDeploymentDate());
	}
}
