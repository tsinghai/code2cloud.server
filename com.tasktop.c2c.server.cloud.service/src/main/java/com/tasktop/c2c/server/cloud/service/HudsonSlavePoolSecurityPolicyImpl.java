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
package com.tasktop.c2c.server.cloud.service;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.domain.Role;

@Component
public class HudsonSlavePoolSecurityPolicyImpl implements HudsonSlavePoolSecurityPolicy {
	@Autowired
	private InternalAuthenticationService internalAuthenticationService;

	@Autowired(required = false)
	// Let it fail to autowire for the tests here.
	private ServiceHostService serviceHostService;

	public void authorize(String projectIdentifier) {
		if (!Security.hasRole(internalAuthenticationService.toCompoundRole(Role.User, projectIdentifier))) {
			throw new InsufficientPermissionsException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.cloud.service.HudsonSlavePoolSecurityPolicy#authorize(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void authorize(String projectIdentifier, String ip) {
		authorize(projectIdentifier);
		ServiceHost host = serviceHostService.findHostForIpAndType(ip, EnumSet.of(ServiceType.BUILD_SLAVE));

		try {
			if (host != null && !serviceHostService.isHostAllocatedToProject(host, projectIdentifier)) {
				throw new InsufficientPermissionsException();
			}
		} catch (EntityNotFoundException e) {
			//
		}
	}
}
