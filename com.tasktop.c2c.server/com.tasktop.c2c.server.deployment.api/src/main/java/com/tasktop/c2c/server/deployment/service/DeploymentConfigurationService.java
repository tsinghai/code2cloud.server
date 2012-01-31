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
package com.tasktop.c2c.server.deployment.service;

import java.util.List;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.logging.NoLog;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;

/**
 * Service for interacting with {@link DeploymentConfiguration}s as well as operating on them to perform the actual
 * deployment. Note this service is project-specific.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Terry Denney <terry.denney@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface DeploymentConfigurationService {
	/**
	 * List the current deployment configurations. If there are errors communicating with CF for a particular
	 * {@link DeploymentConfiguration}, then the errorMessage will be populated.
	 * 
	 * @param region
	 * @return
	 */
	List<DeploymentConfiguration> listDeployments(Region region);

	/**
	 * Check if credentials are valid.
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return true for valid credentials, false for invalid
	 */
	boolean validateCredentials(String url, String username, @NoLog String password) throws ServiceException;

	/**
	 * Create a deployment configuration.
	 * 
	 * 
	 * @param deploymentConfiguration
	 * @return
	 * @throws ValidationException
	 */
	DeploymentConfiguration createDeployment(DeploymentConfiguration deploymentConfiguration)
			throws ValidationException;

	/**
	 * Update a deployment configuration.
	 * 
	 * @param deploymentConfiguration
	 * @return
	 * @throws EntityNotFoundException
	 * @throws ValidationException
	 */
	DeploymentConfiguration updateDeployment(DeploymentConfiguration deploymentConfiguration)
			throws EntityNotFoundException, ValidationException;

	/**
	 * Delete a deployment configuration.
	 * 
	 * @param deploymentConfiguration
	 * @param alsoDeleteFromCloudFoundry
	 *            if true, then we will propogate the delete to CF.
	 * @throws EntityNotFoundException
	 * @throws ServiceException
	 */
	void deleteDeployment(DeploymentConfiguration deploymentConfiguration, boolean alsoDeleteFromCloudFoundry)
			throws EntityNotFoundException, ServiceException;

	DeploymentStatus startDeployment(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	DeploymentStatus stopDeployment(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	DeploymentStatus restartDeployment(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	List<CloudService> getAvailableServices(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	CloudService createService(CloudService service, DeploymentConfiguration deploymentConfiguration)
			throws ServiceException;

	List<Integer> getAvailableMemoryConfigurations(DeploymentConfiguration deploymentCongfiguration)
			throws ServiceException;

	List<DeploymentServiceConfiguration> getAvailableServiceConfigurations(
			DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	/**
	 * To suppor tautomatic deployments
	 * 
	 * @param jobName
	 * @param buildDetails
	 */
	void onBuildCompleted(String jobName, BuildDetails buildDetails);

}
