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

import java.util.List;
import java.util.Set;


import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;

public interface ServiceHostService {

	/**
	 * Return nodes of a given type, ordered by load (smallest-largest)
	 * 
	 * @param type
	 */
	List<ServiceHost> findHostsByType(Set<ServiceType> type);

	/**
	 * Return nodes below a maximum capacity threshold, ordered by load (smallest-largest)
	 * 
	 * @param type
	 * @param maxCapacity
	 *            : currently the number of project services on the host.
	 */
	List<ServiceHost> findHostsBelowCapacity(Set<ServiceType> type, int maxCapacity);

	/**
	 * Return nodes at or above maximum capacity threshold.
	 * 
	 * @param type
	 * @param maxCapacity
	 *            : currently the number of project services on the host.
	 */
	List<ServiceHost> findHostsAtCapacity(Set<ServiceType> type, int maxCapacity);

	/**
	 * Ask about the capacity of a specific host.
	 * 
	 * @param host
	 * @param capacity
	 * @return
	 */
	boolean isAtCapacity(ServiceHost host, int capacity);

	ServiceHost findHostForIpAndType(String ip, Set<ServiceType> type);

	ServiceHost createServiceHost(ServiceHost node);

	ServiceHost updateServiceHost(ServiceHost node) throws EntityNotFoundException;

	void removeServiceHost(ServiceHost node) throws EntityNotFoundException;

	/**
	 * Allocate a host to a project.
	 * 
	 * @param host
	 * @param projectIdentifier
	 * @throws EntityNotFoundException
	 */
	void allocateHostToProject(ServiceType type, ServiceHost host, String projectIdentifier)
			throws EntityNotFoundException;

	/**
	 * De-allocate a host from a project.
	 * 
	 * @param host
	 * @param projectIdentifier
	 * @throws EntityNotFoundException
	 */
	void deallocateHostFromProject(ServiceHost host, String projectIdentifier) throws EntityNotFoundException;

	ServiceHost retrieve(Long serviceHostId) throws EntityNotFoundException;

	int getNumAllocatingNodes(Set<ServiceType> type);

	void recordAllocationScheduled(Set<ServiceType> type);

	void recordAllocationComplete(Set<ServiceType> type);

	List<ServiceHost> findHostsByTypeAndProject(Set<ServiceType> type, String projectIdentifier);

	/**
	 * @param host
	 * @param projectIdentifier
	 * @return
	 * @throws EntityNotFoundException
	 */
	boolean isHostAllocatedToProject(ServiceHost host, String projectIdentifier) throws EntityNotFoundException;
}
