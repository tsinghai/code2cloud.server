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
import com.tasktop.c2c.server.deployment.service.ServiceException;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class CloudFoundryServiceFactoryImpl implements DeploymentServiceFactory {

	@Override
	public DeploymentService constructService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		String username = deploymentConfiguration.getUsername();
		String password = deploymentConfiguration.getPassword();
		String token = deploymentConfiguration.getApiToken();
		String apiBaseUrl = deploymentConfiguration.getApiBaseUrl();

		if (password != null) {
			return new CloudFoundryServiceImpl(username, password, apiBaseUrl);
		}
		return new CloudFoundryServiceImpl(token, apiBaseUrl);

	}

}
