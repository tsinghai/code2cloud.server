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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.ServiceHostService;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectServiceProfileFactory;

@ContextConfiguration({ "/applicationContext-testNoRollback.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
// FIXME : This test is meant understand the locking requirements of the PoolServices w.r.t the ServiceHostService.
@Ignore
public class ServiceHostServiceThreadedTest {
	@Autowired
	private PlatformTransactionManager trxManager;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ServiceHostService serviceHostService;

	private Set<ServiceType> type = EnumSet.of(ServiceType.BUILD_SLAVE);

	private String PROJECT_ID = "proj";
	private int NUM_NODES = 5;
	private int LOOP = 1000;
	private Integer unexpecteNodesNumber = null;
	private AtomicInteger threadsComplete = new AtomicInteger(0);

	@Before
	public void setup() {
		new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Project app = MockProjectFactory.create(null);
				app.setIdentifier(PROJECT_ID);
				app.setProjectServiceProfile(MockProjectServiceProfileFactory.create(null));
				app.getProjectServiceProfile().setProject(app);
				entityManager.persist(app);
			}
		});

	}

	private void setupSecurityCtx() {
		SecurityContextHolder.createEmptyContext();
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("user", "pwd", Arrays.asList(new SimpleGrantedAuthority(
						Role.User + "/" + PROJECT_ID))));
	}

	@Test
	public void test() throws InterruptedException {
		for (int i = 0; i < NUM_NODES; i++) {
			recordNodeAllocation("10.0.0." + (i + 1));
		}

		new GetTotalNodesThread().start();
		new AllocateNodesThread().start();
		new DeAllocateNodesThread().start();

		// wait
		for (int numWaits = 0; numWaits < 100; numWaits++) {
			if (threadsComplete.get() == 3) {
				break;
			}
			Thread.sleep(100);
			Assert.assertNull("Got an unexpected number of nodes", unexpecteNodesNumber);
		}

		Assert.assertEquals(3, threadsComplete.get());
		Assert.assertNull("Got an unexpected number of nodes", unexpecteNodesNumber);
	}

	private class GetTotalNodesThread extends Thread {
		public void run() {
			setupSecurityCtx();
			for (int i = 0; i < LOOP; i++) {
				final int fi = i;
				new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						int numFreeNodes = serviceHostService.findHostsBelowCapacity(type, 1).size();
						int numUsedNodes = serviceHostService.findHostsAtCapacity(type, 1).size();
						int numNodes = numFreeNodes + numUsedNodes;
						if (numNodes != NUM_NODES) {
							unexpecteNodesNumber = numNodes;
							System.out.println("UNEXPECTED NUMBER OF NODES ************************************");
							System.out.println(String.format("i:[%d], free:[%d], used:[%d]", fi, numFreeNodes,
									numUsedNodes));
							return;
						}
					}
				});
			}
			threadsComplete.incrementAndGet();
		}
	}

	private class AllocateNodesThread extends Thread {
		public void run() {
			setupSecurityCtx();

			for (int i = 0; i < LOOP; i++) {

				new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {

						List<ServiceHost> hosts = serviceHostService.findHostsBelowCapacity(type, 1);
						if (!hosts.isEmpty()) {
							try {
								serviceHostService.allocateHostToProject(ServiceType.BUILD_SLAVE, hosts.get(0),
										PROJECT_ID);
							} catch (EntityNotFoundException e) {
								throw new RuntimeException(e);
							}
						}
					}
				});
			}
			threadsComplete.incrementAndGet();
		}
	}

	private class DeAllocateNodesThread extends Thread {
		public void run() {
			setupSecurityCtx();

			for (int i = 0; i < LOOP; i++) {
				new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {

						List<ServiceHost> hosts = serviceHostService.findHostsAtCapacity(type, 1);
						if (!hosts.isEmpty()) {
							try {
								serviceHostService.deallocateHostFromProject(hosts.get(0), PROJECT_ID);
							} catch (EntityNotFoundException e) {
								throw new RuntimeException(e);
							}
						}
					}
				});
			}
			threadsComplete.incrementAndGet();
		}
	}

	private void recordNodeAllocation(String ip) {
		ServiceHost node = new ServiceHost();
		node.setSupportedServices(type);
		node.setInternalNetworkAddress(ip);
		serviceHostService.createServiceHost(node);
	}

}
