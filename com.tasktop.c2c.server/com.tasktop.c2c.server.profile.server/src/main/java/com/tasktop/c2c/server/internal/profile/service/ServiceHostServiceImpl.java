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
package com.tasktop.c2c.server.internal.profile.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.ServiceHostService;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ProjectServiceProfile;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHost;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHostConfiguration;
import com.tasktop.c2c.server.profile.service.ProfileService;

@Component(value = "serviceHostService")
@Transactional
public class ServiceHostServiceImpl implements ServiceHostService {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ServiceHostServiceImpl.class.getName());

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	@Qualifier("main")
	private ProfileService profileService;

	private static ServiceHost convertToInternal(com.tasktop.c2c.server.cloud.domain.ServiceHost node) {
		ServiceHost result = new ServiceHost();
		result.setId(node.getId());
		result.setInternalNetworkAddress(node.getInternalNetworkAddress());
		// result.setType(node.getType()); FIXME ???
		result.setAvailable(node.isAvailable());
		return result;
	}

	private static com.tasktop.c2c.server.cloud.domain.ServiceHost convertToPublic(ServiceHost node) {
		com.tasktop.c2c.server.cloud.domain.ServiceHost result = new com.tasktop.c2c.server.cloud.domain.ServiceHost();
		result.setId(node.getId());
		result.setInternalNetworkAddress(node.getInternalNetworkAddress());
		if (node.getServiceHostConfiguration() != null) {
			result.setSupportedServices(node.getServiceHostConfiguration().getSupportedServices());
		}
		result.setAvailable(node.isAvailable());

		return result;
	}

	private static void updateManaged(com.tasktop.c2c.server.cloud.domain.ServiceHost publicNode,
			ServiceHost managed) {
		managed.setInternalNetworkAddress(publicNode.getInternalNetworkAddress());
		// managed.setType(publicNode.getType());
		managed.setAvailable(publicNode.isAvailable());
	}

	@Override
	public List<com.tasktop.c2c.server.cloud.domain.ServiceHost> findHostsByType(Set<ServiceType> type) {
		ServiceHostConfiguration config = findSupportedConfiguration(type);
		if (config == null) {
			return Collections.EMPTY_LIST;
		}
		List<ServiceHost> managedResults = entityManager
				.createQuery(
						"SELECT host FROM "
								+ ServiceHost.class.getSimpleName()
								+ " host WHERE  host.serviceHostConfiguration = :serviceHostConfiguration ORDER BY SIZE(host.projectServices) ASC")
				.setParameter("serviceHostConfiguration", config).getResultList();
		List<com.tasktop.c2c.server.cloud.domain.ServiceHost> publicNodes = new ArrayList<com.tasktop.c2c.server.cloud.domain.ServiceHost>(
				managedResults.size());
		for (ServiceHost node : managedResults) {
			publicNodes.add(convertToPublic(node));
		}
		return publicNodes;
	}

	private ServiceHostConfiguration findSupportedConfiguration(Set<ServiceType> types) {
		String queryString = "SELECT config FROM " + ServiceHostConfiguration.class.getSimpleName() + " config WHERE ";
		List<ServiceType> typeList = new ArrayList<ServiceType>(types);
		for (int i = 0; i < types.size(); i++) {
			if (i > 0) {
				queryString += " AND ";
			}
			queryString += ":type" + i + " MEMBER OF config.supportedServices";
		}

		Query query = entityManager.createQuery(queryString); // .setLockMode(LockModeType.PESSIMISTIC_WRITE);
		for (int i = 0; i < types.size(); i++) {
			query.setParameter("type" + i, typeList.get(i));
		}

		try {
			ServiceHostConfiguration shc = (ServiceHostConfiguration) query.getSingleResult();
			entityManager.lock(shc, LockModeType.PESSIMISTIC_WRITE);
			return shc;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<com.tasktop.c2c.server.cloud.domain.ServiceHost> findHostsBelowCapacity(Set<ServiceType> type,
			int capacity) {
		ServiceHostConfiguration config = findSupportedConfiguration(type);
		if (config == null) {
			return Collections.EMPTY_LIST;
		}

		List<ServiceHost> managedResults = entityManager
				.createQuery(
						"SELECT host FROM "
								+ ServiceHost.class.getSimpleName()
								+ " host WHERE  host.serviceHostConfiguration = :config AND SIZE(host.projectServices) < :maxCapacity ORDER BY SIZE(host.projectServices) ASC")
				.setParameter("config", config).setParameter("maxCapacity", capacity).getResultList();

		List<com.tasktop.c2c.server.cloud.domain.ServiceHost> publicNodes = new ArrayList<com.tasktop.c2c.server.cloud.domain.ServiceHost>(
				managedResults.size());
		for (ServiceHost node : managedResults) {
			publicNodes.add(convertToPublic(node));
		}
		return publicNodes;
	}

	@Override
	public List<com.tasktop.c2c.server.cloud.domain.ServiceHost> findHostsAtCapacity(Set<ServiceType> type,
			int capacity) {
		ServiceHostConfiguration config = findSupportedConfiguration(type);

		if (config == null) {
			return Collections.EMPTY_LIST;
		}
		List<ServiceHost> managedResults = entityManager
				.createQuery(
						"SELECT host FROM "
								+ ServiceHost.class.getSimpleName()
								+ " host WHERE  host.serviceHostConfiguration = :config AND SIZE(host.projectServices) >= :maxCapacity")
				.setParameter("config", config).setParameter("maxCapacity", capacity).getResultList();

		List<com.tasktop.c2c.server.cloud.domain.ServiceHost> publicNodes = new ArrayList<com.tasktop.c2c.server.cloud.domain.ServiceHost>(
				managedResults.size());
		for (ServiceHost node : managedResults) {
			publicNodes.add(convertToPublic(node));
		}
		return publicNodes;
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.ServiceHost findHostForIpAndType(String ip, Set<ServiceType> type) {
		ServiceHostConfiguration config = findSupportedConfiguration(type);
		if (config == null) {
			return null;
		}

		List<ServiceHost> results = entityManager
				.createQuery(
						"SELECT node FROM "
								+ ServiceHost.class.getSimpleName()
								+ " node WHERE node.internalNetworkAddress = :addr AND node.serviceHostConfiguration = :config")
				.setParameter("addr", ip).setParameter("config", config).getResultList();
		if (results.isEmpty()) {
			return null;
		}
		if (results.size() == 1) {
			return convertToPublic(results.get(0));
		}
		throw new IllegalStateException("Multiple pool nodes with ip: " + ip);
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.ServiceHost createServiceHost(
			com.tasktop.c2c.server.cloud.domain.ServiceHost node) {
		ServiceHost internalNode = convertToInternal(node);
		ServiceHostConfiguration configuration = findSupportedConfiguration(node.getSupportedServices());
		if (configuration == null) {
			configuration = new ServiceHostConfiguration();
			configuration.setSupportedServices(node.getSupportedServices());
			entityManager.persist(configuration);
		}
		internalNode.setServiceHostConfiguration(configuration);
		entityManager.persist(internalNode);
		entityManager.flush();
		return convertToPublic(internalNode);
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.ServiceHost updateServiceHost(
			com.tasktop.c2c.server.cloud.domain.ServiceHost node) {
		ServiceHost managed = entityManager.find(ServiceHost.class, node.getId());
		if (managed == null) {
			throw new IllegalStateException();
		}
		updateManaged(node, managed);
		return convertToPublic(managed);
	}

	@Override
	public void removeServiceHost(com.tasktop.c2c.server.cloud.domain.ServiceHost node) {
		ServiceHost managed = entityManager.find(ServiceHost.class, node.getId());
		if (managed == null) {
			throw new IllegalStateException();
		}
		entityManager.remove(managed);

	}

	@Override
	public boolean isAtCapacity(com.tasktop.c2c.server.cloud.domain.ServiceHost host, int capacity) {
		ServiceHost managed = entityManager.find(ServiceHost.class, host.getId());
		if (managed == null) {
			throw new IllegalStateException();
		}
		return managed.getProjectServices().size() >= capacity;
	}

	@Override
	public void allocateHostToProject(ServiceType type, com.tasktop.c2c.server.cloud.domain.ServiceHost host,
			String projectIdentifier) throws EntityNotFoundException {
		ServiceHost managed = entityManager.find(ServiceHost.class, host.getId(), LockModeType.PESSIMISTIC_WRITE);
		if (managed == null) {
			throw new IllegalStateException();
		}
		entityManager.lock(managed, LockModeType.PESSIMISTIC_WRITE);
		Project project = profileService.getProjectByIdentifier(projectIdentifier);
		ProjectService service = new ProjectService();
		service.setProjectServiceProfile(project.getProjectServiceProfile());
		service.setType(type);
		service.setServiceHost(managed);
		service.setAllocationTime(new Date());
		entityManager.persist(service);
	}

	@Override
	public boolean isHostAllocatedToProject(com.tasktop.c2c.server.cloud.domain.ServiceHost host,
			String projectIdentifier) throws EntityNotFoundException {
		ServiceHost managedHost = entityManager.find(ServiceHost.class, host.getId(), LockModeType.PESSIMISTIC_WRITE);
		if (managedHost == null) {
			throw new EntityNotFoundException();
		}
		entityManager.refresh(managedHost); // This is needed (in tests at least), otherwise the project services do no
											// show up.
		entityManager.lock(managedHost, LockModeType.PESSIMISTIC_WRITE);

		for (ProjectService service : managedHost.getProjectServices()) {
			if (service.getProjectServiceProfile().getProject().getIdentifier().equals(projectIdentifier)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deallocateHostFromProject(com.tasktop.c2c.server.cloud.domain.ServiceHost host,
			String projectIdentifier) throws EntityNotFoundException {
		ServiceHost managedHost = entityManager.find(ServiceHost.class, host.getId(), LockModeType.PESSIMISTIC_WRITE);
		if (managedHost == null) {
			throw new EntityNotFoundException();
		}
		entityManager.refresh(managedHost); // This is needed (in tests at least), otherwise the project services do no
											// show up.

		if (managedHost.getProjectServices().size() != 1) {
			LOGGER.warn(String
					.format("Trying to deallocate host at [%s] for project [%s], but it was allocated to [%d] projects",
							managedHost.getInternalNetworkAddress(), projectIdentifier, managedHost
									.getProjectServices().size()));
			for (ProjectService allocatedService : managedHost.getProjectServices()) {
				LOGGER.warn("Allocated to : "
						+ allocatedService.getProjectServiceProfile().getProject().getIdentifier());
			}
			throw new IllegalStateException();
		}
		ProjectService service = managedHost.getProjectServices().get(0);
		if (!service.getProjectServiceProfile().getProject().getIdentifier().equals(projectIdentifier)) {
			throw new IllegalStateException();
		}
		managedHost.getProjectServices().remove(service);
		service.getProjectServiceProfile().getProjectServices().remove(service);
		service.setProjectServiceProfile(null);
		service.setServiceHost(null);

		entityManager.flush(); // Needed otherwise the entity gets put back
		entityManager.remove(service);
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.ServiceHost retrieve(Long serviceHostId)
			throws EntityNotFoundException {
		ServiceHost managedHost = entityManager.find(ServiceHost.class, serviceHostId);
		if (managedHost == null) {
			throw new EntityNotFoundException();
		}
		return convertToPublic(managedHost);
	}

	private ServiceHostConfiguration findConfigForType(Set<ServiceType> type, boolean create) {
		ServiceHostConfiguration config = findSupportedConfiguration(type);
		if (config != null) {
			return config;
		}
		if (create) {
			ServiceHostConfiguration shp = new ServiceHostConfiguration();
			shp.setSupportedServices(type);
			shp.setPendingAllocations(0);
			shp.setPendingDeletions(0);
			entityManager.persist(shp);
			return shp;
		}
		return null;
	}

	@Override
	public int getNumAllocatingNodes(Set<ServiceType> type) {
		ServiceHostConfiguration shp = findConfigForType(type, false);
		if (shp == null) {
			return 0;
		}
		return shp.getPendingAllocations();
	}

	@Override
	public void recordAllocationScheduled(Set<ServiceType> type) {
		ServiceHostConfiguration shp = findConfigForType(type, true);
		if (shp == null) {
			throw new IllegalStateException();
		}
		shp.setPendingAllocations(shp.getPendingAllocations() + 1);

	}

	@Override
	public void recordAllocationComplete(Set<ServiceType> type) {
		ServiceHostConfiguration shp = findConfigForType(type, false);
		if (shp == null) {
			throw new IllegalStateException();
		}
		shp.setPendingAllocations(shp.getPendingAllocations() - 1);
	}

	// REVIEW this query could be simplified by just querying on the ProjectSerivce table
	@Override
	public List<com.tasktop.c2c.server.cloud.domain.ServiceHost> findHostsByTypeAndProject(Set<ServiceType> type,
			String projectIdentifier) {
		ServiceHostConfiguration config = findSupportedConfiguration(type);
		if (config == null) {
			return Collections.EMPTY_LIST;
		}

		List<ServiceHost> managedResults = entityManager
				.createQuery(
						"SELECT host FROM "
								+ ServiceHost.class.getSimpleName()
								+ " host, "
								+ ProjectServiceProfile.class.getSimpleName()
								+ " projectServiceProfile, "
								+ ProjectService.class.getSimpleName()
								+ " projectService WHERE host.serviceHostConfiguration = :config AND projectServiceProfile.project.identifier = :projectId "
								+ "AND projectService MEMBER projectServiceProfile.projectServices AND projectService.serviceHost = host")
				.setParameter("config", config).setParameter("projectId", projectIdentifier).getResultList();

		List<com.tasktop.c2c.server.cloud.domain.ServiceHost> publicNodes = new ArrayList<com.tasktop.c2c.server.cloud.domain.ServiceHost>(
				managedResults.size());
		for (ServiceHost node : managedResults) {
			publicNodes.add(convertToPublic(node));
		}
		return publicNodes;
	}
}
