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
package com.tasktop.c2c.server.profile.tests.service;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.MockJobService;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ProjectServiceProfile;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectServiceFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectServiceProfileFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class ProjectServiceServiceTest {
	@Autowired
	private com.tasktop.c2c.server.profile.service.ProjectServiceService projectServiceService;

	@Autowired
	ApplicationContext applicationContext;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private MockJobService jobService;

	@Before
	public void before() {
		jobService.getScheduledJobs().clear();
	}

	@Test
	public void testProvisionDefaultServices() throws Exception {
		Project project = MockProjectFactory.create(entityManager);

		entityManager.flush();
		assertNull(project.getProjectServiceProfile());

		projectServiceService.provisionDefaultServices(project.getId());

		// Should be jobs for the task, wiki, scm, maven, build, deployment
		assertEquals(6, jobService.getScheduledJobs().size());
	}

	@Test
	public void testFindServiceByUri() throws EntityNotFoundException {
		Project project = MockProjectFactory.create(entityManager);
		ProjectServiceProfile profile = MockProjectServiceProfileFactory.create(entityManager);
		profile.setProject(project);
		project.setProjectServiceProfile(profile);
		for (ServiceType type : ServiceType.values()) {
			ProjectService projectService = MockProjectServiceFactory.create(entityManager);
			profile.add(projectService);
			projectService.setUriPattern(String.format("/%s.*", type.name()));
		}
		entityManager.flush();
		assertNotNull(projectServiceService.findServiceByUri(project.getIdentifier(), "/" + ServiceType.SCM.name()));
		assertNotNull(projectServiceService.findServiceByUri(project.getIdentifier(), "/" + ServiceType.SCM.name()
				+ "?foo=bar"));
		assertNotNull(projectServiceService.findServiceByUri(project.getIdentifier(), "/" + ServiceType.SCM.name()
				+ "/foo/bar"));
		assertNotNull(projectServiceService.findServiceByUri(project.getIdentifier(), "/" + ServiceType.SCM.name()
				+ "/"));
		assertNull(projectServiceService.findServiceByUri(project.getIdentifier(), ServiceType.SCM.name()));
		assertNull(projectServiceService.findServiceByUri(project.getIdentifier(), ""));
	}

	@Test
	public void testFindExpiredServices() {
		ServiceType type = ServiceType.BUILD_SLAVE;
		Date allocationTime = new Date();
		List<ProjectService> services = projectServiceService.findProjectServicesOlderThan(type, allocationTime);

		Assert.assertEquals(0, services.size());

		ProjectService service = MockProjectServiceFactory.create(entityManager);
		service.setType(type);
		service.setAllocationTime(allocationTime);
		entityManager.flush();

		services = projectServiceService.findProjectServicesOlderThan(type, new Date(allocationTime.getTime() - 1000));

		Assert.assertEquals(0, services.size());

		services = projectServiceService.findProjectServicesOlderThan(type, new Date(allocationTime.getTime() + 1000));

		Assert.assertEquals(1, services.size());

	}
}
