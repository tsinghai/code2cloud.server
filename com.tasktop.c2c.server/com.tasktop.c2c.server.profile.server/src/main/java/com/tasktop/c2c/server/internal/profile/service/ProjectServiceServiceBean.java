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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.auth.service.AuthUtils;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.NodeProvisioningService;
import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.job.JobService;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationService;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceClient;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceProvider;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ProjectServiceProfile;
import com.tasktop.c2c.server.profile.domain.internal.ScmRepository;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHost;
import com.tasktop.c2c.server.profile.service.ProfileServiceConfiguration;
import com.tasktop.c2c.server.profile.service.ProfileWebService;
import com.tasktop.c2c.server.profile.service.ProjectServiceService;

@Service("projectServiceService")
@Transactional(rollbackFor = Exception.class)
public class ProjectServiceServiceBean extends AbstractJpaServiceBean implements ProjectServiceService,
		InternalApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceServiceBean.class.getSimpleName());

	@Autowired
	private ProfileServiceConfiguration configuration;

	@Autowired
	private JobService jobService;

	@Resource(name = "profileWebServiceBean")
	private ProfileWebService profileWebService;

	@Resource
	private Map<ServiceType, NodeProvisioningService> nodeProvisioningServiceByType;

	@Resource
	private Map<ServiceType, String> configPathsByServiceType;

	@Autowired
	private NodeConfigurationServiceProvider nodeConfigurationServiceProvider;

	@Resource
	private InternalAuthenticationService internalAuthenticationService;

	private boolean updateServiceTemplateOnStart = true;

	@Override
	public void provisionDefaultServices(Long projectId) throws EntityNotFoundException, ProvisioningException {
		Project project = entityManager.find(Project.class, projectId);
		if (project == null) {
			throw new EntityNotFoundException();
		}
		verifyCanProvision(project);

		ProjectServiceProfile template = getDefaultTemplate();
		if (template == null) {
			throw new EntityNotFoundException();
		}
		ProjectServiceProfile serviceProfile = template.createCopy();
		serviceProfile.setProject(project);
		project.setProjectServiceProfile(serviceProfile);

		updateTemplateServiceConfiguration(project);
		entityManager.persist(serviceProfile);

		// Schedule the jobs
		for (ProjectService service : serviceProfile.getProjectServices()) {
			jobService.schedule(new ProjectServicesProvisioningJob(project, service.getType()));
		}

	}

	/**
	 * get the default template
	 * 
	 * @return the default template, or null if there is none
	 */
	private ProjectServiceProfile getDefaultTemplate() {
		try {
			ProjectServiceProfile template = (ProjectServiceProfile) entityManager.createQuery(
					"select e from " + ProjectServiceProfile.class.getSimpleName() + " e where e.template = true")
					.getSingleResult();
			// we shouldn't need to do this here, but for some reason it fixes an issue when running unit
			// tests from within Eclipse. Pretty harmless.
			entityManager.refresh(template);
			return template;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void doProvisionServices(Long projectId, ServiceType type) throws EntityNotFoundException,
			ProvisioningException {
		Project project = entityManager.find(Project.class, projectId);
		if (project == null) {
			throw new EntityNotFoundException();
		}

		AuthUtils
				.insertSystemAuthToken(internalAuthenticationService.toCompoundRole(Role.User, project.getIdentifier()));

		NodeConfigurationService.NodeConfiguration config = new NodeConfigurationService.NodeConfiguration();
		config.setApplicationId(project.getIdentifier());
		config.setProperty(NodeConfigurationService.PROFILE_HOSTNAME, configuration.getWebHost());
		config.setProperty(NodeConfigurationService.PROFILE_PROTOCOL, configuration.getProfileApplicationProtocol());
		config.setProperty(NodeConfigurationService.PROFILE_BASE_URL, configuration.getProfileBaseUrl());
		config.setProperty(NodeConfigurationService.PROFILE_BASE_SERVICE_URL,
				configuration.getServiceUrlPrefix(project.getIdentifier()));

		NodeProvisioningService nodeProvisioningService = nodeProvisioningServiceByType.get(type);
		if (nodeProvisioningService == null) {
			LOGGER.info("Not seting up service " + type + " no node provisionService available");
			return;
		}

		LOGGER.info("provisioning node for " + type);
		ServiceHost serviceHost = convertToInternal(nodeProvisioningService.provisionNode());
		LOGGER.info("provisioned to " + serviceHost.getInternalNetworkAddress());

		// Now configure the host for the new project.
		NodeConfigurationServiceClient nodeConfigurationService = nodeConfigurationServiceProvider.getNewService();
		String baseUrl = "http://" + serviceHost.getInternalNetworkAddress() + ":" + ALM_HTTP_PORT + "/"
				+ configPathsByServiceType.get(type);
		nodeConfigurationService.setBaseUrl(baseUrl);

		try {
			LOGGER.info("configuring node for " + type);
			nodeConfigurationService.configureNode(config);
			LOGGER.info("configuring done");
		} catch (Exception e) {
			throw new ProvisioningException("Caught exception while configuring node", e);
		}

		for (ProjectService service : project.getProjectServiceProfile().getProjectServices()) {
			if (service.getType().equals(type)) {
				service.setServiceHost(serviceHost);
			}
		}

		if (type.equals(ServiceType.SCM)) {
			updateScmRepository(project);
		}

	}

	private ServiceHost convertToInternal(com.tasktop.c2c.server.cloud.domain.ServiceHost serviceHost) {
		return entityManager.find(ServiceHost.class, serviceHost.getId());
	}

	private void updateScmRepository(Project project) {

		try {
			// This seems crazy, but I can't figure out a better way to do this that doesn't involve refactoring a large
			// chunk of code - the external service URL is calculated inside this method and associated with the
			// returned Project, and I need that URL to put into the database. This code path needs some refactoring to
			// make it easier to use.
			com.tasktop.c2c.server.profile.domain.project.Project serviceUrlEnabledProject = profileWebService
					.getProjectByIdentifier(project.getIdentifier());

			String scmServiceUrl = null;

			for (com.tasktop.c2c.server.profile.domain.project.ProjectService curService : serviceUrlEnabledProject
					.getProjectServices()) {
				if (ServiceType.SCM.equals(curService.getServiceType())) {
					scmServiceUrl = curService.getUrl();
				}
			}

			// If we didn't have an SCM service provisioned, we probably had an external Git URL - skip this.
			if (scmServiceUrl != null) {
				// Register our new Git repository with our SCM config, and save it to the DB.
				ScmRepository newRepo = new ScmRepository();
				newRepo.setType(ScmType.GIT);
				newRepo.setScmLocation(ScmLocation.CODE2CLOUD);
				newRepo.setUrl(String.format("%s%s.git", scmServiceUrl, project.getIdentifier()));
				newRepo.setProject(project);
				project.getRepositories().add(newRepo);

				entityManager.persist(newRepo);
				entityManager.persist(project);
			}
		} catch (EntityNotFoundException enfe) {
			LOGGER.error("Unable to load project with identifier: " + project.getIdentifier());
		}
	}

	private void updateTemplateServiceConfiguration(Project project) {
		for (ProjectService service : project.getProjectServiceProfile().getProjectServices()) {
			switch (service.getType()) {
			case BUILD:
				// Replace our marker string with the actual project identifier
				service.setInternalUriPrefix(service.getInternalUriPrefix().replace("APPID", project.getIdentifier()));
				break;
			}
		}
	}

	protected void verifyCanProvision(Project project) throws ProvisioningException {
		if (project.getProjectServiceProfile() != null) {
			// can't provision services if they're already provisioned.
			throw new ProvisioningException("Can't provision services: they're already provisioned");
		}
	}

	private static final int ALM_HTTP_PORT = 8080;
	private static final int ALM_AJP_PORT = 8009;

	private List<ProjectService> projectServiceTemplate;

	@Autowired
	@Resource(name = "projectSeriviceTemplate")
	public void setProjectServiceTemplate(List<ProjectService> projectServices) {
		this.projectServiceTemplate = projectServices;
	}

	@Override
	public void initializeApplicationServiceProfileTemplate() {
		ProjectServiceProfile templateServiceProfile = getDefaultTemplate();

		boolean create;

		if (templateServiceProfile != null) {
			if (!updateServiceTemplateOnStart) {
				return;
			}

			for (ProjectService service : templateServiceProfile.getProjectServices()) {
				entityManager.remove(service);
			}
			entityManager.flush();

			templateServiceProfile.getProjectServices().clear();

			create = false;
		} else {
			create = true;
			templateServiceProfile = new ProjectServiceProfile();
			templateServiceProfile.setTemplate(true);
		}

		for (ProjectService service : projectServiceTemplate) {
			templateServiceProfile.add(service);
		}

		if (create) {
			entityManager.persist(templateServiceProfile);
		}
		entityManager.flush();
	}

	@Override
	public ProjectService findServiceByUri(String projectIdentifier, String uri) throws EntityNotFoundException {
		Project project = getProjectByIdentifier(projectIdentifier);
		if (project.getProjectServiceProfile() == null) {
			return null;
		}
		for (ProjectService service : project.getProjectServiceProfile().getProjectServices()) {
			if (service.matchesUri(uri)) {
				return service;
			}
		}
		return null;
	}

	protected Project getProjectByIdentifier(String projectIdentifier) throws EntityNotFoundException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Project> query = criteriaBuilder.createQuery(Project.class);
		Root<Project> root = query.from(Project.class);
		query.select(root).where(criteriaBuilder.equal(root.get("identifier"), projectIdentifier));

		Project project;
		try {
			project = entityManager.createQuery(query).getSingleResult();
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}
		return project;
	}

	@Override
	public List<ProjectService> findProjectServiceByType(String projectIdentifier, ServiceType serviceType)
			throws EntityNotFoundException {
		Project project = getProjectByIdentifier(projectIdentifier);
		List<ProjectService> services = new ArrayList<ProjectService>();
		if (project.getProjectServiceProfile() != null) {
			for (ProjectService service : project.getProjectServiceProfile().getProjectServices()) {
				if (service.getType() == serviceType) {
					services.add(service);
				}
			}
		}
		return services;
	}

	public void setNodeProvisioningServiceByType(Map<ServiceType, NodeProvisioningService> nodeProvisioningServiceByType) {
		this.nodeProvisioningServiceByType = nodeProvisioningServiceByType;
	}

	public void setConfigPathsByServiceType(Map<ServiceType, String> configPathsByServiceType) {
		this.configPathsByServiceType = configPathsByServiceType;
	}

	@Override
	public List<ServiceHost> findHostsForAddress(String remoteAddr) {
		@SuppressWarnings("unchecked")
		List<ServiceHost> results = entityManager
				.createQuery(
						"SELECT node FROM " + ServiceHost.class.getSimpleName()
								+ " node WHERE node.internalNetworkAddress = :addr").setParameter("addr", remoteAddr)
				.getResultList();
		return results;
	}

	@Override
	public List<ProjectService> findProjectServicesOlderThan(ServiceType type, Date date) {
		List<ProjectService> projectServices = entityManager
				.createQuery(
						"SELECT projectService FROM "
								+ ProjectService.class.getSimpleName()
								+ " projectService WHERE projectService.type = :type AND projectService.allocationTime < :date")
				.setParameter("type", type).setParameter("date", date).getResultList();

		return projectServices;
	}

	@Value("${updateServiceTemplateOnStart}")
	public void setUpdateServiceTemplateOnStart(boolean updateServiceTemplateOnStart) {
		this.updateServiceTemplateOnStart = updateServiceTemplateOnStart;
	}
}
