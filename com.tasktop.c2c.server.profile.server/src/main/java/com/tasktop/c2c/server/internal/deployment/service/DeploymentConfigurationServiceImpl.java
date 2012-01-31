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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;
import com.tasktop.c2c.server.deployment.domain.DeploymentType;
import com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService;
import com.tasktop.c2c.server.deployment.service.ServiceException;
import com.tasktop.c2c.server.internal.profile.service.SecurityPolicy;
import com.tasktop.c2c.server.profile.domain.build.BuildArtifact;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifact;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifacts;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.service.ProjectArtifactService;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Transactional(rollbackFor = { Exception.class })
@Service("deploymentConfigurationService")
public class DeploymentConfigurationServiceImpl extends AbstractJpaServiceBean implements
		DeploymentConfigurationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentConfigurationServiceImpl.class.getName());

	private static DeploymentConfigurationServiceImpl lastInstance;

	// For testing
	public static DeploymentConfigurationServiceImpl getLastInstance() {
		return lastInstance;
	}

	public DeploymentConfigurationServiceImpl() {
		lastInstance = this;
	}

	@Autowired
	private ProfileService profileService;

	@Qualifier("main")
	@Autowired
	private ProjectArtifactService projectArtifactService;

	@Autowired
	private SecurityPolicy securityPolicy;

	private DeploymentServiceFactory cloudFoundryServiceFactory = new CloudFoundryServiceFactoryImpl();

	private String getProjectIdentifier() {
		return TenancyContextHolder.getContext().getTenant().getIdentity().toString();
	}

	private Project getProject() {
		try {
			return profileService.getProjectByIdentifier(getProjectIdentifier());
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#listDeployments(org.cloudfoundry
	 * .code.server .common.service.domain.Region)
	 */
	@Override
	public List<DeploymentConfiguration> listDeployments(Region region) {
		// TODO paging params
		List<com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration> internalResultList = entityManager
				.createQuery(
						"SELECT dc FROM "
								+ com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration.class
										.getSimpleName() + " dc where dc.project.identifier = :projectIdentifer")
				.setParameter("projectIdentifer", getProjectIdentifier()).getResultList();

		List<DeploymentConfiguration> result = new ArrayList<DeploymentConfiguration>(internalResultList.size());

		for (com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration internalDeploymentConfiguration : internalResultList) {
			securityPolicy.retrieve(internalDeploymentConfiguration);
			DeploymentConfiguration config = DeploymentDomain.convertToPublic(internalDeploymentConfiguration);
			try {
				populateDeploymentConfiguration(config);
			} catch (ServiceException e) {
				config.setErrorString(e.getMessage());
			}
			result.add(config);
		}

		return result;
	}

	private void populateDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {

		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		deploymentService.populate(deploymentConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#createDeployment(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public DeploymentConfiguration createDeployment(DeploymentConfiguration deploymentConfiguration)
			throws ValidationException {
		validate(deploymentConfiguration);
		DeploymentService deploymentService = null;
		try {
			deploymentService = createDeploymentService(deploymentConfiguration);
			updateTokenIfNeeded(deploymentConfiguration, deploymentService);
		} catch (ServiceException e) {
			Errors errors = createErrors(deploymentConfiguration);
			errors.reject("deployment.credentials.invalid");
			throw new ValidationException(errors);
		}

		try {
			com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration internalDeploymentConfiguration = DeploymentDomain
					.convertToInternal(deploymentConfiguration);
			internalDeploymentConfiguration.setProject(getProject());

			securityPolicy.create(internalDeploymentConfiguration);

			if (findDeploymetByName(internalDeploymentConfiguration.getName()) != null) {
				Errors errors = createErrors(deploymentConfiguration);
				errors.reject("deployment.name.notUnique");
				throw new ValidationException(errors);
			}

			entityManager.persist(internalDeploymentConfiguration);
			entityManager.flush(); // Need the Id;
			deploymentConfiguration.setId(internalDeploymentConfiguration.getId());

			boolean exists = deploymentService.exists(deploymentConfiguration);

			if (exists) {
				deploymentService.populate(deploymentConfiguration);
				return deploymentConfiguration;
			} else {
				try {
					deploymentService.create(deploymentConfiguration);
					deploymentService.populate(deploymentConfiguration);

				} catch (Exception e) {
					setStatusErrorMessage(deploymentConfiguration, e.getMessage());
				}
			}
		} catch (ServiceException e) {
			setStatusErrorMessage(deploymentConfiguration, e.getMessage());

		}

		return deploymentConfiguration;
	}

	private com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration findDeploymetByName(
			String name) {
		try {
			return (com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration) entityManager
					.createQuery(
							"SELECT dc FROM "
									+ com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration.class
											.getSimpleName()
									+ " dc where dc.project.identifier = :projectIdentifer AND dc.name = :name")
					.setParameter("projectIdentifer", getProjectIdentifier()).setParameter("name", name)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	private void updateTokenIfNeeded(DeploymentConfiguration config, DeploymentService deploymentService)
			throws ServiceException {
		if (config.getPassword() != null && !config.getPassword().isEmpty()) {
			String token = deploymentService.login();
			config.setApiToken(token);
		}
	}

	/**
	 * @param deploymentConfiguration
	 * @param warFile
	 * @throws ServiceException
	 */
	private void deployWar(DeploymentConfiguration deploymentConfiguration, DeploymentService deploymentSerivce,
			File warFile) throws ServiceException {
		try {
			String name = deploymentConfiguration.getName();
			deploymentSerivce.uploadApplication(name, warFile);

		} catch (MalformedURLException e) {
			throw new ServiceException("Failed to deploy war file " + warFile.getName(), e);
		} catch (IOException e) {
			throw new ServiceException("Failed to deploy war file " + warFile.getName(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#updateDeployment(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public DeploymentConfiguration updateDeployment(DeploymentConfiguration deploymentConfiguration)
			throws EntityNotFoundException, ValidationException {
		validate(deploymentConfiguration);
		com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration internalDeploymentConfiguration = entityManager
				.find(com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration.class,
						deploymentConfiguration.getId());
		if (internalDeploymentConfiguration == null) {
			throw new EntityNotFoundException("No DC with id: " + deploymentConfiguration.getId());
		}
		securityPolicy.modify(internalDeploymentConfiguration);

		boolean shouldDeploy = shouldDeployOnUpdate(deploymentConfiguration, internalDeploymentConfiguration);
		DeploymentService deploymentService = null;
		try {

			deploymentService = createDeploymentService(deploymentConfiguration);

			updateTokenIfNeeded(deploymentConfiguration, deploymentService);

			deploymentService.update(deploymentConfiguration);
			String projectId = internalDeploymentConfiguration.getProject().getIdentifier();

			if (shouldDeploy) {
				ProjectArtifacts artifacts = projectArtifactService.findBuildArtifacts(projectId,
						deploymentConfiguration.getBuildJobName(), deploymentConfiguration.getBuildJobNumber());
				ProjectArtifact artifact = null;
				if (artifacts == null) {
					setStatusErrorMessage(deploymentConfiguration, "Could not find build");
				} else {
					for (ProjectArtifact a : artifacts.getArtifacts()) {
						if (a.getPath().equals(deploymentConfiguration.getBuildArtifactPath())) {
							artifact = a;
						}
					}
					if (artifact == null) {
						setStatusErrorMessage(deploymentConfiguration, "Could not find artifact");
					} else {

						File tempWarFile = File.createTempFile("deploy", "war");
						projectArtifactService.downloadProjectArtifact(projectId, tempWarFile, artifact);
						deployWar(deploymentConfiguration, deploymentService, tempWarFile);
						deploymentConfiguration.setLastDeploymentDate(new Date());
					}
				}
			}

		} catch (ServiceException e) {
			setStatusErrorMessage(deploymentConfiguration, e.getMessage());
			if (deploymentService != null) {
				try {
					deploymentService.populate(deploymentConfiguration);
				} catch (ServiceException e1) {
					// ignore
				}
			}

		} catch (IOException e) {
			setStatusErrorMessage(deploymentConfiguration, e.getMessage());
		}

		// Update the fields in the managed object
		DeploymentDomain.updateInternal(deploymentConfiguration, internalDeploymentConfiguration);

		return deploymentConfiguration;
	}

	/**
	 * @param deploymentConfiguration
	 * @param existingDeploymentConfiguration
	 * @return
	 */
	private boolean shouldDeployOnUpdate(
			DeploymentConfiguration deploymentConfiguration,
			com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration existingDeploymentConfiguration) {
		if (deploymentConfiguration.getDeploymentType() == null
				|| !deploymentConfiguration.getDeploymentType().equals(DeploymentType.MANUAL)) {
			return false;
		} else if (deploymentConfiguration.getBuildJobName() == null
				|| deploymentConfiguration.getBuildJobNumber() == null
				|| deploymentConfiguration.getBuildArtifactPath() == null) {
			return false;
		}
		// Ok so check for changes;
		return !(deploymentConfiguration.getBuildJobName().equals(existingDeploymentConfiguration.getBuildJobName())
				&& deploymentConfiguration.getBuildJobNumber().equals(
						existingDeploymentConfiguration.getBuildJobNumber()) && deploymentConfiguration
				.getBuildArtifactPath().equals(existingDeploymentConfiguration.getBuildArtifactPath()));
	}

	private void setStatusErrorMessage(DeploymentConfiguration deploymentConfiguration, String message) {
		deploymentConfiguration.setErrorString(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#deleteDeployment(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public void deleteDeployment(DeploymentConfiguration deploymentConfiguration, boolean alsoDeleteFromCF)
			throws EntityNotFoundException, ServiceException {
		com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration internalDeploymentConfiguration = entityManager
				.find(com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration.class,
						deploymentConfiguration.getId());
		if (internalDeploymentConfiguration == null) {
			throw new EntityNotFoundException("No DC with id: " + deploymentConfiguration.getId());
		}
		securityPolicy.delete(internalDeploymentConfiguration);
		entityManager.remove(internalDeploymentConfiguration);

		if (alsoDeleteFromCF) {
			createDeploymentService(deploymentConfiguration).deleteApplication(deploymentConfiguration.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#startDeployment(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public DeploymentStatus startDeployment(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		checkUpdatePermissions(deploymentConfiguration);
		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		deploymentService.startApplication(deploymentConfiguration.getName());
		deploymentService.updateStatus(deploymentConfiguration);

		return deploymentConfiguration.getStatus();
	}

	/**
	 * @param deploymentConfiguration
	 */
	private void checkUpdatePermissions(DeploymentConfiguration deploymentConfiguration) {
		com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration internalDeploymentConfiguration = entityManager
				.find(com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration.class,
						deploymentConfiguration.getId());
		securityPolicy.modify(internalDeploymentConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#stopDeployment(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public DeploymentStatus stopDeployment(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		checkUpdatePermissions(deploymentConfiguration);

		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		String name = deploymentConfiguration.getName();
		deploymentService.stopApplication(name);
		deploymentService.updateStatus(deploymentConfiguration);

		return deploymentConfiguration.getStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#restartDeployment(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public DeploymentStatus restartDeployment(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		return startDeployment(deploymentConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#getAvailableServices()
	 */
	@Override
	public List<com.tasktop.c2c.server.deployment.domain.CloudService> getAvailableServices(
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		return deploymentService.getServices();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#createService(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentService)
	 */
	@Override
	public CloudService createService(CloudService service, DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {
		checkUpdatePermissions(deploymentConfiguration);
		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		return deploymentService.createService(service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#getAvailableMemoryConfigurations()
	 */
	@Override
	public List<Integer> getAvailableMemoryConfigurations(DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {
		List<Integer> options = new ArrayList<Integer>();

		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		int[] choices = deploymentService.getApplicationMemoryChoices();
		for (int choice : choices) {
			options.add(choice);
		}

		return options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#getAvailableServiceConfigurations
	 * ()
	 */
	@Override
	public List<DeploymentServiceConfiguration> getAvailableServiceConfigurations(
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		DeploymentService deploymentService = createDeploymentService(deploymentConfiguration);
		return deploymentService.getAvailableServiceConfigurations(deploymentConfiguration);
	}

	private DeploymentService createDeploymentService(DeploymentConfiguration deploymentConfiguration)
			throws ServiceException {
		return cloudFoundryServiceFactory.constructService(deploymentConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService#validateCredentials(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	// TODO, push down to service.
	@Override
	public boolean validateCredentials(String url, String username, String password) {
		DeploymentConfiguration deploymentConfiguration = new DeploymentConfiguration();
		deploymentConfiguration.setApiBaseUrl(url);
		deploymentConfiguration.setUsername(username);
		deploymentConfiguration.setPassword(password);

		DeploymentService deploymentSerivce;
		try {
			deploymentSerivce = createDeploymentService(deploymentConfiguration);
			return deploymentSerivce.validateCredentials(deploymentConfiguration);
		} catch (ServiceException e) {
			return false;
		}
	}

	public void setCloudFoundryServiceFactory(DeploymentServiceFactory cloudFoundryServiceFactory) {
		this.cloudFoundryServiceFactory = cloudFoundryServiceFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService
	 * #onBuildCompleted(java.lang.String, com.tasktop.c2c.server.profile.domain.build.BuildDetails)
	 */
	@Override
	// TODO explicit security check.
	public void onBuildCompleted(String jobName, BuildDetails buildDetails) {
		List<com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration> internalResultList = entityManager
				.createQuery(
						"SELECT dc FROM "
								+ com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration.class
										.getSimpleName()
								+ " dc where dc.project.identifier = :projectIdentifer AND dc.deploymentType = :depType AND dc.buildJobName = :jobName")
				.setParameter("projectIdentifer", getProjectIdentifier())
				.setParameter("depType", DeploymentType.AUTOMATED).setParameter("jobName", jobName).getResultList();

		for (com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration dc : internalResultList) {
			if (!(BuildResult.SUCCESS.equals(buildDetails.getResult()) || (BuildResult.UNSTABLE.equals(buildDetails
					.getResult()) && dc.isDeployUnstableBuilds()))) {
				return;
			}
			String artifactPath = dc.getBuildArtifactPath();
			BuildArtifact toDeploy = null;
			for (BuildArtifact artifact : buildDetails.getArtifacts()) {
				if (matches(artifactPath, artifact)) {
					// TODO what if multiple matches.
					toDeploy = artifact;
				}
			}

			if (toDeploy == null) {
				// TODO message somewhere?
				continue;
			}

			try {
				File tempWarFile = File.createTempFile("deploy", "war");
				ProjectArtifact artifact = new ProjectArtifact();
				artifact.setUrl(toDeploy.getUrl()); // All thats needed now
				projectArtifactService.downloadProjectArtifact(getProjectIdentifier(), tempWarFile, artifact);
				DeploymentConfiguration deploymentConfig = DeploymentDomain.convertToPublic(dc);
				deployWar(deploymentConfig, createDeploymentService(deploymentConfig), tempWarFile);
				dc.setLastDeploymentDate(new Date());
				dc.setBuildJobNumber(buildDetails.getNumber() + "");
			} catch (IOException e) {
				LOGGER.warn("exception durring auto deployment", e);
			} catch (ServiceException e) {
				LOGGER.warn("exception durring auto deployment", e);
			}

		}

	}

	private PathMatcher pathMatcher = new AntPathMatcher();

	private boolean matches(String artifactPath, BuildArtifact artifact) {
		return pathMatcher.match(artifactPath, artifact.getRelativePath());
	}

	/**
	 * @param projectArtifactService
	 *            the projectArtifactService to set
	 */
	public void setProjectArtifactService(ProjectArtifactService projectArtifactService) {
		this.projectArtifactService = projectArtifactService;
	}
}
