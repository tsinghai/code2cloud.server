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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cloudfoundry.client.lib.CloudApplication;
import org.cloudfoundry.client.lib.CloudApplication.AppState;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.CloudService;
import org.cloudfoundry.client.lib.ServiceConfiguration;
import org.cloudfoundry.client.lib.ServiceConfiguration.Tier;
import org.springframework.http.HttpStatus;

import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration.ServiceTier;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus.Result;
import com.tasktop.c2c.server.deployment.service.ServiceException;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class CloudFoundryServiceImpl implements DeploymentService {
	private final CloudFoundryClient client;

	private void wrapInServiceException(CloudFoundryException exception) throws ServiceException {
		throw new ServiceException(exception.getDescription(), exception);
	}

	private void wrapInServiceException(MalformedURLException exception) throws ServiceException {
		throw new ServiceException("Invalid url", exception);
	}

	/**
	 * @param username
	 * @param password
	 * @param apiBaseUrl
	 * @throws MalformedURLException
	 * @throws ServiceException
	 */
	public CloudFoundryServiceImpl(String username, String password, String apiBaseUrl) throws ServiceException {
		CloudFoundryClient theClient = null;
		try {
			theClient = new CloudFoundryClient(username, password, apiBaseUrl);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		} catch (MalformedURLException e) {
			wrapInServiceException(e);
		}
		client = theClient;
	}

	/**
	 * @param token
	 * @param apiBaseUrl
	 * @throws MalformedURLException
	 * @throws ServiceException
	 */
	public CloudFoundryServiceImpl(String token, String apiBaseUrl) throws ServiceException {
		CloudFoundryClient theClient = null;
		try {
			theClient = new CloudFoundryClient(token, apiBaseUrl);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		} catch (MalformedURLException e) {
			wrapInServiceException(e);
		}
		client = theClient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#uploadApplication(java.lang.String,
	 * java.io.File)
	 */
	@Override
	public void uploadApplication(String name, File warFile) throws IOException, ServiceException {
		try {
			client.uploadApplication(name, warFile);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#createAndUploadAndStartApplication
	 * (java .lang.String, java.lang.Object, int, java.io.File, java.util.List, java.util.List)
	 */
	@Override
	public void create(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		try {
			client.createApplication(deploymentConfiguration.getName(), CloudApplication.SPRING,
					deploymentConfiguration.getMemory(), deploymentConfiguration.getMappedUrls(),
					getServiceNames(deploymentConfiguration));
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	private List<String> getServiceNames(DeploymentConfiguration deploymentConfiguration) {
		List<String> serviceNames = new ArrayList<String>();
		if (deploymentConfiguration.getServices() != null) {
			for (com.tasktop.c2c.server.deployment.domain.CloudService service : deploymentConfiguration
					.getServices()) {
				serviceNames.add(service.getName());
			}
		}
		return serviceNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#deleteApplication(java.lang.String)
	 */
	@Override
	public void deleteApplication(String name) throws ServiceException {
		try {
			client.deleteApplication(name);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#stopApplication(java.lang.String)
	 */
	@Override
	public void stopApplication(String name) throws ServiceException {
		try {
			client.stopApplication(name);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#startApplication(java.lang.String)
	 */
	@Override
	public void startApplication(String name) throws ServiceException {
		try {
			client.startApplication(name);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#getServices()
	 */
	@Override
	public List<com.tasktop.c2c.server.deployment.domain.CloudService> getServices() throws ServiceException {
		try {
			List<com.tasktop.c2c.server.deployment.domain.CloudService> services = new ArrayList<com.tasktop.c2c.server.deployment.domain.CloudService>();
			List<CloudService> cfServices = client.getServices();
			for (CloudService cfService : cfServices) {
				services.add(convertToDeploymentService(cfService));
			}

			return services;
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
			return null; // never
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#createService(org.cloudfoundry.client
	 * . lib.CloudService)
	 */
	@Override
	public com.tasktop.c2c.server.deployment.domain.CloudService createService(
			com.tasktop.c2c.server.deployment.domain.CloudService service) throws ServiceException {
		CloudService cfService = new CloudService();
		cfService.setName(service.getName());
		cfService.setType(service.getType());
		cfService.setVendor(service.getVendor());
		cfService.setVersion(service.getVersion());
		cfService.setTier(service.getTierType());

		try {
			client.createService(cfService);
			return service;
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
			return null; // never
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#getApplicationMemoryChoices()
	 */
	@Override
	public int[] getApplicationMemoryChoices() throws ServiceException {
		try {
			return client.getApplicationMemoryChoices();
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
			return null; // never
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.internal.deployment.service.CloudFoundryService#login()
	 */
	@Override
	public String login() throws ServiceException {
		try {
			return client.login();
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
			return null; // never
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.internal.deployment.service.DeploymentService#populate()
	 */
	@Override
	public void populate(DeploymentConfiguration deploymentConfiguration) throws ServiceException {

		try {
			CloudApplication application = client.getApplication(deploymentConfiguration.getName());

			if (application == null) {
				deploymentConfiguration.setErrorString("No application found");
			} else {
				deploymentConfiguration.setMappedUrls(application.getUris());
				deploymentConfiguration.setMemory(application.getMemory());
				deploymentConfiguration.setNumInstances(application.getInstances());

				List<com.tasktop.c2c.server.deployment.domain.CloudService> services = new ArrayList<com.tasktop.c2c.server.deployment.domain.CloudService>();
				List<String> serviceNames = application.getServices();
				for (String serviceName : serviceNames) {
					CloudService cfService = client.getService(serviceName);
					services.add(convertToDeploymentService(cfService));
				}
				deploymentConfiguration.setServices(services);

				DeploymentStatus status = new DeploymentStatus();
				status.setResult(getStatusResult(application.getState()));

				deploymentConfiguration.setStatus(status);
			}
		} catch (IllegalArgumentException e) {
			throw new ServiceException(e.getMessage(), e);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	private com.tasktop.c2c.server.deployment.domain.CloudService convertToDeploymentService(
			CloudService cfService) {
		com.tasktop.c2c.server.deployment.domain.CloudService service = new com.tasktop.c2c.server.deployment.domain.CloudService();
		service.setName(cfService.getName());
		service.setType(cfService.getType());
		service.setVendor(cfService.getVendor());
		service.setVersion(cfService.getVersion());
		service.setTierType(cfService.getTier());
		return service;
	}

	private Result getStatusResult(AppState state) {
		if (state == AppState.STARTED) {
			return Result.STARTED;
		} else if (state == AppState.UPDATING) {
			return Result.UPDATING;
		} else {
			return Result.STOPPED;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.DeploymentService#update(com.tasktop.c2c.server
	 * .deployment .domain.DeploymentConfiguration)
	 */
	@Override
	public void update(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		try {
			String name = deploymentConfiguration.getName();
			CloudApplication application = client.getApplication(name);

			int numInstances = deploymentConfiguration.getNumInstances();
			if (application.getInstances() != numInstances) {
				client.updateApplicationInstances(name, numInstances);
			}

			int memory = deploymentConfiguration.getMemory();
			if (application.getMemory() != memory) {
				client.updateApplicationMemory(name, memory);
			}

			List<com.tasktop.c2c.server.deployment.domain.CloudService> services = deploymentConfiguration
					.getServices();

			if (services != null) {
				List<String> serviceNames = new ArrayList<String>();
				for (com.tasktop.c2c.server.deployment.domain.CloudService service : services) {
					serviceNames.add(service.getName());
				}
				List<String> cfServiceNames = application.getServices();
				if (!cfServiceNames.equals(serviceNames)) {
					Set<String> existingServiceNames = getExistingServiceNames();

					for (com.tasktop.c2c.server.deployment.domain.CloudService service : services) {
						if (!existingServiceNames.contains(service.getName())) {
							createService(service);
						}
					}

					client.updateApplicationServices(name, serviceNames);
				}
			}

			List<String> mappedUrls = deploymentConfiguration.getMappedUrls();
			if (!application.getUris().equals(mappedUrls)) {
				client.updateApplicationUris(name, mappedUrls);
			}
			populate(deploymentConfiguration);
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
	}

	private Set<String> getExistingServiceNames() {
		Set<String> result = new HashSet<String>();
		for (CloudService service : client.getServices()) {
			result.add(service.getName());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.DeploymentService#updateStatus(org.cloudfoundry.code
	 * .server.deployment .domain.DeploymentConfiguration)
	 */
	@Override
	public void updateStatus(DeploymentConfiguration deploymentConfiguration) throws ServiceException {

		try {
			DeploymentStatus status = new DeploymentStatus();
			deploymentConfiguration.setStatus(status);

			CloudApplication application = client.getApplication(deploymentConfiguration.getName());
			status.setResult(getStatusResult(application.getState()));
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
		}
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
		try {
			List<DeploymentServiceConfiguration> configurations = new ArrayList<DeploymentServiceConfiguration>();

			List<ServiceConfiguration> cfConfigurations = client.getServiceConfigurations();
			for (ServiceConfiguration cfConfiguration : cfConfigurations) {
				DeploymentServiceConfiguration configuration = new DeploymentServiceConfiguration();
				configuration.setDescription(cfConfiguration.getDescription());
				configuration.setType(cfConfiguration.getType());
				configuration.setVendor(cfConfiguration.getVendor());
				configuration.setVersion(cfConfiguration.getVersion());

				List<Tier> cfTiers = cfConfiguration.getTiers();
				List<ServiceTier> tiers = new ArrayList<ServiceTier>();
				for (Tier cfTier : cfTiers) {
					ServiceTier tier = new ServiceTier();
					tier.setDescription(cfTier.getDescription());
					tier.setPricingPeriod(cfTier.getPricingPeriod());
					tier.setPricingType(cfTier.getPricingType());
					tier.setType(cfTier.getType());
					tiers.add(tier);
				}
				configuration.setTiers(tiers);

				configurations.add(configuration);
			}

			return configurations;
		} catch (CloudFoundryException e) {
			wrapInServiceException(e);
			return null; // never
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.DeploymentService#exists(com.tasktop.c2c.server
	 * .deployment .domain.DeploymentConfiguration)
	 */
	@Override
	public boolean exists(DeploymentConfiguration config) throws ServiceException {
		try {
			CloudApplication existing = client.getApplication(config.getName());
			return true;
		} catch (CloudFoundryException e) {
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				return false;
			} else {
				wrapInServiceException(e);
				return false; // never
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.internal.deployment.service.DeploymentService#validateCredentials(org.cloudfoundry
	 * .code.server .deployment.domain.DeploymentConfiguration)
	 */
	@Override
	public boolean validateCredentials(DeploymentConfiguration config) {
		try {
			String token = client.login();
			return token != null;
		} catch (Exception e) {
			return false;
		}
	}

}
