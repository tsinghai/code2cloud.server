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
package com.tasktop.c2c.server.cloud.service.tests;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.ServiceHostService;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ProjectServiceProfile;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectServiceProfileFactory;

@ContextConfiguration({ "/applicationContext-test.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ServiceHostServiceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ServiceHostService serviceHostService;

	private Set<ServiceType> type1 = EnumSet.of(ServiceType.BUILD_SLAVE);
	private Set<ServiceType> type2 = EnumSet.of(ServiceType.TASKS);

	@Test
	public void testFindUnprovisionsedAndRecordAllocatoin() {
		Assert.assertEquals(0, serviceHostService.findHostsBelowCapacity(type1, 1).size());
		recordNodeAllocation("10.0.0.1", type1);
		Assert.assertEquals(1, serviceHostService.findHostsBelowCapacity(type1, 1).size());

		recordNodeAllocation("10.0.0.2", type1);
		Assert.assertEquals(2, serviceHostService.findHostsBelowCapacity(type1, 1).size());
		Assert.assertEquals(0, serviceHostService.findHostsBelowCapacity(type2, 1).size());

		recordNodeProvision("10.0.0.1");
		Assert.assertEquals(1, serviceHostService.findHostsBelowCapacity(type1, 1).size());

		recordNodeProvision("10.0.0.2");
		Assert.assertEquals(0, serviceHostService.findHostsBelowCapacity(type1, 1).size());

		recordNodeUnProvision("10.0.0.2");
		Assert.assertEquals(1, serviceHostService.findHostsBelowCapacity(type1, 1).size());

		recordNodeUnProvision("10.0.0.1");
		Assert.assertEquals(2, serviceHostService.findHostsBelowCapacity(type1, 1).size());
	}

	@Test
	public void testFindHostsBelowCapacitySorting() {
		recordNodeAllocation("10.0.0.1", type1);
		recordNodeAllocation("10.0.0.2", type1);
		recordNodeAllocation("10.0.0.3", type1);
		recordNodeProvision("10.0.0.1");
		recordNodeProvision("10.0.0.1");
		recordNodeProvision("10.0.0.1");
		recordNodeProvision("10.0.0.2");
		recordNodeProvision("10.0.0.2");
		recordNodeProvision("10.0.0.3");
		List<ServiceHost> hosts = serviceHostService.findHostsBelowCapacity(type1, 5);
		Assert.assertEquals(3, hosts.size());
		Assert.assertEquals("10.0.0.3", hosts.get(0).getInternalNetworkAddress());
		Assert.assertEquals("10.0.0.2", hosts.get(1).getInternalNetworkAddress());
		Assert.assertEquals("10.0.0.1", hosts.get(2).getInternalNetworkAddress());

	}

	private void recordNodeAllocation(String ip, Set<ServiceType> type) {
		ServiceHost node = new ServiceHost();
		node.setSupportedServices(type);
		node.setInternalNetworkAddress(ip);
		serviceHostService.createServiceHost(node);
		entityManager.flush();
	}

	private ProjectService recordNodeProvision(String ip) {
		ServiceHost node = serviceHostService.findHostForIpAndType(ip, type1);
		com.tasktop.c2c.server.profile.domain.internal.ServiceHost managed = entityManager.find(
				com.tasktop.c2c.server.profile.domain.internal.ServiceHost.class, node.getId());

		ProjectService service = new ProjectService();
		service.setServiceHost(managed);
		entityManager.persist(service);

		entityManager.flush();
		return service;
	}

	private void recordNodeUnProvision(String ip) {
		ServiceHost node = serviceHostService.findHostForIpAndType(ip, type1);
		com.tasktop.c2c.server.profile.domain.internal.ServiceHost managed = entityManager.find(
				com.tasktop.c2c.server.profile.domain.internal.ServiceHost.class, node.getId());
		entityManager.refresh(managed);
		if (managed.getProjectServices().isEmpty()) {
			return;
		}

		ProjectService service = managed.getProjectServices().remove(0);
		entityManager.remove(service);

		entityManager.flush();
	}

	@Test
	public void testFindHostsByTypeAndProject() {
		Project project = MockProjectFactory.create(entityManager);
		ProjectServiceProfile projectServiceProfile = MockProjectServiceProfileFactory.create(entityManager);
		projectServiceProfile.setProject(project);
		project.setProjectServiceProfile(projectServiceProfile);

		recordNodeAllocation("10.0.0.1", type1);
		recordNodeAllocation("10.0.0.2", type1);
		recordNodeAllocation("10.0.0.3", type1);
		recordNodeAllocation("10.0.0.4", type2);

		List<ServiceHost> result = serviceHostService.findHostsByTypeAndProject(type1, project.getIdentifier());
		Assert.assertEquals(0, result.size());

		ProjectService service = recordNodeProvision("10.0.0.1");
		projectServiceProfile.add(service);
		entityManager.flush();

		result = serviceHostService.findHostsByTypeAndProject(type1, project.getIdentifier());
		Assert.assertEquals(1, result.size());

	}
}
