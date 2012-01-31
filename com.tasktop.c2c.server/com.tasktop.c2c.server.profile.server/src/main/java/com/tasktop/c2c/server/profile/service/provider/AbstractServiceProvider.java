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
package com.tasktop.c2c.server.profile.service.provider;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.service.ProjectServiceService;

public abstract class AbstractServiceProvider<T> implements ServiceProvider<T> {
	protected RestTemplate restTemplate;

	@Autowired
	private ProjectServiceService projectServiceService;

	private final String serviceUri;

	protected AbstractServiceProvider(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	protected abstract AbstractRestServiceClient getNewService();

	@SuppressWarnings("unchecked")
	@Override
	public T getService(String projectIdentifier) {

		AbstractRestServiceClient service = getNewService();
		service.setRestTemplate(restTemplate);

		try {
			ProjectService appService = projectServiceService.findServiceByUri(projectIdentifier, serviceUri);
			if (appService == null) {
				throw new IllegalStateException("No services found for project " + projectIdentifier);
			}
			String baseUri = appService.getInternalBaseUri();
			if (appService.getServiceHost() == null || appService.getServiceHost().getInternalNetworkAddress() == null
					|| !appService.getServiceHost().isAvailable()) {
				throw new IllegalStateException("Service is not avaialable");
			}

			service.setBaseUrl(computeBaseUrl(baseUri));
			return (T) service;

		} catch (EntityNotFoundException e) {
			throw new IllegalStateException();
		}
	}

	protected String computeBaseUrl(String internalBaseUri) {
		return internalBaseUri;
	}

	@Resource(name = "preAuthenticatedRestTemplate")
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
