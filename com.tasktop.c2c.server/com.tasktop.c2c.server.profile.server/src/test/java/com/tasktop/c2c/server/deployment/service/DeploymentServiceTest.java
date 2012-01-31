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
package com.tasktop.c2c.server.deployment.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentType;
import com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService;
import com.tasktop.c2c.server.deployment.service.ServiceException;
import com.tasktop.c2c.server.internal.deployment.service.DeploymentConfigurationServiceImpl;
import com.tasktop.c2c.server.internal.deployment.service.DeploymentService;
import com.tasktop.c2c.server.internal.deployment.service.DeploymentServiceFactory;
import com.tasktop.c2c.server.profile.domain.build.BuildArtifact;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.service.ProjectArtifactService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class DeploymentServiceTest {

	@Autowired
	protected ProfileService profileService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DeploymentConfigurationService deploymentConfigurationService;

	private Mockery context;

	@Before
	public void setup() throws Exception {
		Project mock = MockProjectFactory.create(entityManager);
		TenancyContextHolder.createEmptyContext();
		TenancyContextHolder.getContext().setTenant(new DefaultTenant(mock.getIdentifier(), mock));

		context = new JUnit4Mockery();
		final DeploymentServiceFactory mockCloudServiceFactory = context.mock(DeploymentServiceFactory.class);
		final DeploymentService mockDeploymentService = context.mock(DeploymentService.class);
		final ProjectArtifactService mockArtifactService = context.mock(ProjectArtifactService.class);
		List<String> serviceNames = new ArrayList<String>();
		serviceNames.add("service");

		final List<CloudService> mockServices = new ArrayList<CloudService>();

		final CloudService mockService = new CloudService();
		mockService.setName("service");

		context.checking(new Expectations() {
			{
				allowing(mockCloudServiceFactory).constructService(with(any(DeploymentConfiguration.class)));
				will(returnValue(mockDeploymentService));

				allowing(mockDeploymentService).getServices();
				will(returnValue(mockServices));

				allowing(mockDeploymentService).createService(with(any(CloudService.class)));
				will(new Action() {

					@Override
					public Object invoke(Invocation invocation) throws Throwable {
						mockServices.add(mockService);
						return null;
					}

					@Override
					public void describeTo(Description description) {
					}
				});

				allowing(mockDeploymentService).populate(with(any(DeploymentConfiguration.class)));
				will(new Action() {

					@Override
					public Object invoke(Invocation invocation) throws Throwable {
						DeploymentConfiguration deploymentConfiguration = (DeploymentConfiguration) invocation
								.getParameter(0);
						deploymentConfiguration.setMemory(512);
						deploymentConfiguration.setNumInstances(1);
						deploymentConfiguration.setMappedUrls(Arrays.asList("http://example.com"));
						return null;
					}

					@Override
					public void describeTo(Description description) {
					}
				});

				allowing(mockDeploymentService).getServices();
				will(returnValue(mockServices));

				// Everything else will return null.
				allowing(mockDeploymentService);

				allowing(mockArtifactService);
			}
		});

		DeploymentConfigurationServiceImpl.getLastInstance().setCloudFoundryServiceFactory(mockCloudServiceFactory);
		DeploymentConfigurationServiceImpl.getLastInstance().setProjectArtifactService(mockArtifactService);
	}

	@Test
	public void testCreateAndList() throws ValidationException, ServiceException {
		List<DeploymentConfiguration> configs = deploymentConfigurationService.listDeployments(null);
		Assert.assertEquals(0, configs.size());

		DeploymentConfiguration config = new DeploymentConfiguration();

		try {
			deploymentConfigurationService.createDeployment(config);
			Assert.fail("expected validation exception");
		} catch (ValidationException e) {
			// expected;
		}

		config.setName("name");
		config.setDeploymentType(DeploymentType.AUTOMATED);
		deploymentConfigurationService.createDeployment(config);

		configs = deploymentConfigurationService.listDeployments(null);
		Assert.assertEquals(1, configs.size());

		config = configs.get(0);
		Assert.assertEquals("name", configs.get(0).getName());

		List<String> urls = config.getMappedUrls();
		Assert.assertEquals(1, urls.size());
		Assert.assertEquals("http://example.com", urls.get(0));

		Assert.assertEquals(512, config.getMemory());
		Assert.assertEquals(1, config.getNumInstances());

		config = new DeploymentConfiguration();
		config.setName("name");
		config.setDeploymentType(DeploymentType.AUTOMATED);

		try {
			deploymentConfigurationService.createDeployment(config);
			Assert.fail("expected validation exception");
		} catch (ValidationException e) {
			// expected;
		}

	}

	@Test
	public void testCreateServiceAndList() throws ValidationException, ServiceException {
		DeploymentConfiguration config = new DeploymentConfiguration();
		config.setName("name");
		config.setDeploymentType(DeploymentType.AUTOMATED);
		deploymentConfigurationService.createDeployment(config);

		List<CloudService> services = deploymentConfigurationService.getAvailableServices(config);
		Assert.assertEquals(0, services.size());

		CloudService service = new CloudService();
		service.setName("service");
		deploymentConfigurationService.createService(service, config);

		services = deploymentConfigurationService.getAvailableServices(config);
		Assert.assertEquals(1, services.size());
		Assert.assertEquals("service", services.get(0).getName());
	}

	@Test
	@Ignore
	public void testListServiceConfigurations() throws ServiceException {
		DeploymentConfiguration config = new DeploymentConfiguration();
		config.setName("name");
		config.setDeploymentType(DeploymentType.AUTOMATED);

		List<DeploymentServiceConfiguration> serviceConfigs = deploymentConfigurationService
				.getAvailableServiceConfigurations(config);
		Assert.assertEquals(1, serviceConfigs.size());

		DeploymentServiceConfiguration serviceConfig = serviceConfigs.get(0);
		Assert.assertEquals("description", serviceConfig.getDescription());
		Assert.assertEquals("type", serviceConfig.getType());
		Assert.assertEquals("vendor", serviceConfig.getVendor());
		Assert.assertEquals("1.0", serviceConfig.getVersion());
	}

	// @Test
	// public void testUpdateDeploymentConfiguration() throws EntityNotFoundException, ValidationException,
	// ServiceException {
	// DeploymentConfiguration config = new DeploymentConfiguration();
	// config.setName("name");
	// config.setServices(new ArrayList<DeploymentService>());
	// config.setNumInstances(2);
	//
	// Assert.assertEquals(config.getServices().size(), 0);
	//
	// deploymentConfigurationService.updateDeployment(config);
	//
	// Assert.assertEquals(config.getServices().size(), 1);
	// Assert.assertEquals(config.getServices().get(0), "service");
	// }

	@Test
	public void testAutoDeployment() throws ValidationException {
		DeploymentConfiguration config = new DeploymentConfiguration();

		try {
			deploymentConfigurationService.createDeployment(config);
			Assert.fail("expected validation exception");
		} catch (ValidationException e) {
			// expected;
		}

		config.setName("name");
		config.setDeploymentType(DeploymentType.AUTOMATED);
		String jobName = "job";
		String jobNumber = "111";
		String artifactPath = "**/*.war";
		config.setBuildJobName(jobName);
		config.setBuildJobNumber(jobNumber);
		config.setBuildArtifactPath(artifactPath);
		deploymentConfigurationService.createDeployment(config);

		BuildDetails buildDetails = new BuildDetails();
		buildDetails.setResult(BuildResult.SUCCESS);
		buildDetails.setNumber(111);
		BuildArtifact a1 = new BuildArtifact();
		a1.setRelativePath("src/bar.txt");
		BuildArtifact a2 = new BuildArtifact();
		a2.setRelativePath("target/bar.war");

		buildDetails.setArtifacts(Arrays.asList(a1));
		deploymentConfigurationService.onBuildCompleted(jobName, buildDetails);

		config = deploymentConfigurationService.listDeployments(null).get(0);
		Assert.assertNull(config.getLastDeploymentDate());

		buildDetails.setArtifacts(Arrays.asList(a1, a2));
		deploymentConfigurationService.onBuildCompleted(jobName, buildDetails);

		config = deploymentConfigurationService.listDeployments(null).get(0);
		Assert.assertNotNull(config.getLastDeploymentDate());

	}
}
