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
package com.tasktop.c2c.server.profile.service;

import java.util.Date;
import java.util.List;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.internal.profile.service.ProvisioningException;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHost;

/**
 * Manages the services associated with a project. This includes provisioning the initial service for a new project, and
 * methods to find existing services.
 * 
 * @see ProjectService
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 * 
 */
public interface ProjectServiceService {

	/**
	 * Provision a default set of services for a project.
	 * 
	 * @param projectId
	 * @throws EntityNotFoundException
	 * @throws ProvisioningException
	 */
	public void provisionDefaultServices(Long projectId) throws EntityNotFoundException, ProvisioningException;

	/**
	 * Compute the project service by matching a URI
	 * 
	 * @param projectIdentifier
	 *            the {@link Project#getIdentifier() project identifier} for which the match should occur
	 * @param uri
	 *            the {@link com.tasktop.c2c.server.profile.domain.internal.ProjectService#getUriPattern()
	 *            service-specific URI} that identifies the project service.
	 * @return the project service, or null if there was no match
	 * @throws EntityNotFoundException
	 *             if the given project identifier is unknown
	 */
	public com.tasktop.c2c.server.profile.domain.internal.ProjectService findServiceByUri(String projectIdentifier,
			String uri) throws EntityNotFoundException;

	/**
	 * Find project services by type for the given project
	 * 
	 * @param projectIdentitifier
	 *            the identity of the project
	 * @param serviceType
	 *            the type of service to locate.
	 * @return a list of services, or an empty list if there are none
	 * @throws EntityNotFoundException
	 *             if the given project identifier cannot be found
	 */
	public List<com.tasktop.c2c.server.profile.domain.internal.ProjectService> findProjectServiceByType(
			String projectIdentifier, ServiceType serviceType) throws EntityNotFoundException;

	/**
	 * Find all service hosts for a give address. Normally this should return a single host, however in a local dev
	 * setup there can be multiple hosts mapped to the local address.
	 * 
	 * @param remoteAddr
	 * @return
	 */
	List<ServiceHost> findHostsForAddress(String remoteAddr);

	public List<ProjectService> findProjectServicesOlderThan(ServiceType type, Date date);

}
