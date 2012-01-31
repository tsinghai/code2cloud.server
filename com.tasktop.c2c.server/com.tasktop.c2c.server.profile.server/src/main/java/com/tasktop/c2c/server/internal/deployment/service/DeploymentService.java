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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.deployment.service.ServiceException;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface DeploymentService {

	boolean validateCredentials(DeploymentConfiguration config) throws ServiceException;

	boolean exists(DeploymentConfiguration config) throws ServiceException;

	void populate(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	/** Update all the configuration in this deployment. This includes creating new services if needed. **/
	void update(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	void updateStatus(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	void uploadApplication(String name, File warFile) throws ServiceException, IOException;

	/**
	 * @param name
	 * @param framework
	 * @param memory
	 * @param uris
	 * @param serviceNames
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	void create(DeploymentConfiguration deploymentConfiguration) throws ServiceException;

	/**
	 * @param name
	 */
	void deleteApplication(String name) throws ServiceException;

	/**
	 * @param name
	 */
	void stopApplication(String name) throws ServiceException;

	/**
	 * @return
	 */
	List<com.tasktop.c2c.server.deployment.domain.CloudService> getServices() throws ServiceException;

	/**
	 * @param cfService
	 */
	com.tasktop.c2c.server.deployment.domain.CloudService createService(
			com.tasktop.c2c.server.deployment.domain.CloudService service) throws ServiceException;

	/**
	 * @return
	 */
	int[] getApplicationMemoryChoices() throws ServiceException;

	/**
	 * @return
	 * @throws ServiceException
	 */
	String login() throws ServiceException;

	/**
	 * @param name
	 */
	void startApplication(String name) throws ServiceException;

	public List<DeploymentServiceConfiguration> getAvailableServiceConfigurations(
			DeploymentConfiguration deploymentConfiguration) throws ServiceException;

}
