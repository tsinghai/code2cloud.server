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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.DynamicPoolSizeStrategy;
import com.tasktop.c2c.server.cloud.service.FinishReleaseHudsonSlaveJob;
import com.tasktop.c2c.server.cloud.service.FixedPoolSizeStrategy;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolSecurityPolicy;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolServiceImpl;
import com.tasktop.c2c.server.cloud.service.NodeCleaningService;
import com.tasktop.c2c.server.cloud.service.NodeLifecycleService;
import com.tasktop.c2c.server.cloud.service.NodeLifecycleServiceProvider;
import com.tasktop.c2c.server.cloud.service.PoolSizeStrategy;
import com.tasktop.c2c.server.cloud.service.PromiseService;
import com.tasktop.c2c.server.cloud.service.RequestBuildSlaveResult;
import com.tasktop.c2c.server.cloud.service.ServiceHostService;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.job.JobService;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.service.ConfigurationPropertyService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectServiceProfileFactory;

@ContextConfiguration({ "/applicationContext-testNoRollback.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
// @Transactional DON't want this because..
public class HudsonSlavePoolServiceTest {
	private class IncrementingStringAction implements Action {
		int nextIp = 1;

		@Override
		public void describeTo(Description arg0) {

		}

		@Override
		public synchronized Object invoke(Invocation invocation) throws Throwable {
			return "10.0.0." + nextIp++;
		}

	}

	private static final int POOL_SIZE = 5;
	private static final String APPID = "application-one";

	private Mockery context;

	private HudsonSlavePoolServiceImpl hudsonSlavePoolServiceImpl;
	private HudsonSlavePoolService hudsonSlavePoolService;

	@Autowired
	private NodeLifecycleServiceProvider nodeLifecycleServiceProvider;

	@Autowired
	private ServiceHostService serviceHostService;

	@Autowired
	private JobService jobService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlatformTransactionManager trxManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	@Qualifier("main")
	private PromiseService promiseService;

	private static boolean didStaticSetup = false;

	@Before
	public void before() throws Exception {
		context = new JUnit4Mockery();

		hudsonSlavePoolServiceImpl = new HudsonSlavePoolServiceImpl();
		mockNodeLifecycleService = context.mock(NodeLifecycleService.class);
		mockNodeCleaningService = context.mock(NodeCleaningService.class);
		final ConfigurationPropertyService mockConfigPropertyService = context.mock(ConfigurationPropertyService.class);

		context.checking(new Expectations() {
			{
				allowing(mockNodeLifecycleService).createNewNode();
				will(new IncrementingStringAction());

				allowing(mockNodeLifecycleService).decomissionNode(with(any(String.class)));

				allowing(mockNodeCleaningService).cleanNode(with(any(ServiceHost.class)));
				allowing(mockConfigPropertyService).getConfigurationValue(with(any(String.class)));
				will(returnValue(null));
			}
		});

		if (!didStaticSetup) {
			doStaticSetup();
			didStaticSetup = true;
		}

		hudsonSlavePoolServiceImpl.setUpdatePeriod(0);// for testing FIXME
		hudsonSlavePoolServiceImpl.setServiceHostService(serviceHostService);
		hudsonSlavePoolServiceImpl.setConfigurationPropertyService(mockConfigPropertyService);
		hudsonSlavePoolServiceImpl.setPoolSizeStrategy(new FixedPoolSizeStrategy(POOL_SIZE, POOL_SIZE));
		hudsonSlavePoolServiceImpl.setSecurityPolicy(new HudsonSlavePoolSecurityPolicy() {

			@Override
			public void authorize(String applicatoinId) {
				// ok
			}

			@Override
			public void authorize(String projectIdentifier, String ip) {
				// ok

			}
		});
		hudsonSlavePoolServiceImpl.setJobService(jobService);
		hudsonSlavePoolServiceImpl.setTrxManager(trxManager);
		hudsonSlavePoolServiceImpl.setUpdatePeriod(100);
		hudsonSlavePoolServiceImpl.setPromiseService(promiseService);
		nodeLifecycleServiceProvider.setServicesByHostType(Collections.singletonMap(ServiceType.BUILD_SLAVE,
				(NodeLifecycleService) mockNodeLifecycleService));

		// Need to wrap the service calls in a transaction
		TransactionProxyFactoryBean trxProxy = new TransactionProxyFactoryBean();
		trxProxy.setTransactionManager(trxManager);
		trxProxy.setTarget(hudsonSlavePoolServiceImpl);
		Properties trxAttributes = new Properties();
		trxAttributes.put("*", "PROPAGATION_REQUIRED");
		trxProxy.setTransactionAttributes(trxAttributes);
		trxProxy.afterPropertiesSet();
		hudsonSlavePoolService = (HudsonSlavePoolService) trxProxy.getObject();

		SecurityContextHolder.createEmptyContext();
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("user", "pwd", Arrays.asList(new SimpleGrantedAuthority(
						Role.User + "/" + APPID))));
		promiseService.clearAllPromises();
	}

	// Should only run once per suite
	private void doStaticSetup() {

		((GenericApplicationContext) applicationContext).getBeanFactory().registerSingleton("mockNodeCleaningService",
				mockNodeCleaningService);
		FinishReleaseHudsonSlaveJob.setCleaningServiceBeanName("mockNodeCleaningService");

		new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Project app = MockProjectFactory.create(null);
				app.setIdentifier(APPID);
				app.setProjectServiceProfile(MockProjectServiceProfileFactory.create(null));
				app.getProjectServiceProfile().setProject(app);
				entityManager.persist(app);
			}
		});

	}

	@After
	public void teardown() {
		hudsonSlavePoolServiceImpl.shutdown();
		hudsonSlavePoolServiceImpl = null;
	}

	@Test
	public void testWithFixedSizePool() throws Exception {
		hudsonSlavePoolServiceImpl.afterPropertiesSet(); // Kick off threads;

		Set<String> provisionedIps = new HashSet<String>();
		waitForAllocations();
		Assert.assertEquals(POOL_SIZE, hudsonSlavePoolService.getStatus().getTotalNodes());
		Assert.assertEquals(POOL_SIZE, hudsonSlavePoolService.getStatus().getFreeNodes());
		Assert.assertEquals(0, hudsonSlavePoolService.getStatus().getNodesOnLoan());

		// Exhaust the pool
		RequestBuildSlaveResult result = null;
		for (int i = 0; i < POOL_SIZE; i++) {
			result = hudsonSlavePoolService.acquireSlave(APPID, null);
			Assert.assertEquals(RequestBuildSlaveResult.Type.SLAVE, result.getType());
			String host = result.getSlaveIp();
			Assert.assertTrue("gave out the same ip i=" + i, provisionedIps.add(host));
		}

		hudsonSlavePoolService.renewSlave(APPID, result.getSlaveIp());
		hudsonSlavePoolService.renewSlave(APPID, result.getSlaveIp());

		result = hudsonSlavePoolService.acquireSlave(APPID, null);
		Assert.assertEquals(RequestBuildSlaveResult.Type.PROMISE, result.getType());
		Assert.assertNotNull(result.getPromiseToken());
		Assert.assertEquals(1, hudsonSlavePoolService.getStatus().getOutstandingPromises());

		Assert.assertEquals(POOL_SIZE, hudsonSlavePoolService.getStatus().getTotalNodes());
		Assert.assertEquals(0, hudsonSlavePoolService.getStatus().getFreeNodes());
		Assert.assertEquals(POOL_SIZE, hudsonSlavePoolService.getStatus().getNodesOnLoan());

		// Fill it back up
		for (String ip : provisionedIps) {
			hudsonSlavePoolService.releaseSlave(APPID, ip);
		}
		Thread.sleep(1000); // jobs run
		Assert.assertEquals(POOL_SIZE, hudsonSlavePoolService.getStatus().getTotalNodes());
		Assert.assertEquals(POOL_SIZE, hudsonSlavePoolService.getStatus().getFreeNodes());
		Assert.assertEquals(0, hudsonSlavePoolService.getStatus().getNodesOnLoan());

		// hudsonSlavePoolService.setPoolSizeStrategy(new FixedPoolSizeStrategy(POOL_SIZE - 1, POOL_SIZE - 1));

		context.assertIsSatisfied();
	}

	private static final int MAX_TOTAL_NODES = 10;
	private static final int MIN_TOTAL_NODES = 2;
	private static final int MIN_FREE = 1;
	private static final int MAX_FREE = 2;

	private PoolSizeStrategy getDynamicPoolSizeStrategy() {
		DynamicPoolSizeStrategy s = new DynamicPoolSizeStrategy();
		s.setMaxTotalNodes(MAX_TOTAL_NODES);
		s.setMinTotalNodes(MIN_TOTAL_NODES);
		s.setMaxUnderCapacityNodes(MAX_FREE);
		s.setMinUnderCapacityNodes(MIN_FREE);
		return s;
	}

	@Test
	public void testWithDynamicSizePool() throws Exception {
		hudsonSlavePoolServiceImpl.setPoolSizeStrategy(getDynamicPoolSizeStrategy());
		hudsonSlavePoolServiceImpl.afterPropertiesSet(); // Kick off threads;

		waitForAllocations();

		Assert.assertEquals(MIN_TOTAL_NODES, hudsonSlavePoolService.getStatus().getTotalNodes());
		Assert.assertEquals(MIN_TOTAL_NODES, hudsonSlavePoolService.getStatus().getFreeNodes());
		Assert.assertEquals(0, hudsonSlavePoolService.getStatus().getNodesOnLoan());

		List<String> provisionedIps = new ArrayList<String>();
		// Exhaust the pool
		for (int i = 0; i < MIN_TOTAL_NODES; i++) {
			RequestBuildSlaveResult aquireResult = hudsonSlavePoolService.acquireSlave(APPID, null);
			Assert.assertEquals(RequestBuildSlaveResult.Type.SLAVE, aquireResult.getType());
			String host = aquireResult.getSlaveIp();
			Assert.assertTrue("gave out the same ip i=" + i, provisionedIps.add(host));
		}

		waitForAllocations();
		Assert.assertEquals(MIN_TOTAL_NODES + MIN_FREE, hudsonSlavePoolService.getStatus().getTotalNodes());
		Assert.assertEquals(MIN_FREE, hudsonSlavePoolService.getStatus().getFreeNodes());
		Assert.assertEquals(provisionedIps.size(), hudsonSlavePoolService.getStatus().getNodesOnLoan());

		// Exhaust the pool
		for (int i = 0; i < MIN_FREE; i++) {
			String host = hudsonSlavePoolService.acquireSlave(APPID, null).getSlaveIp();
			Assert.assertTrue("gave out the same ip i=" + i, provisionedIps.add(host));
		}

		waitForAllocations();
		Assert.assertEquals(MIN_TOTAL_NODES + MIN_FREE * 2, hudsonSlavePoolService.getStatus().getTotalNodes());
		Assert.assertEquals(MIN_FREE, hudsonSlavePoolService.getStatus().getFreeNodes());
		Assert.assertEquals(provisionedIps.size(), hudsonSlavePoolService.getStatus().getNodesOnLoan());

		// At this point we have 5
		int currentTotalSize = MIN_TOTAL_NODES + MIN_FREE * 2;
		int freeNodes = MIN_FREE;
		// Give them back
		for (int i = 0; i < provisionedIps.size(); i++) {
			hudsonSlavePoolService.releaseSlave(APPID, provisionedIps.get(i));
			waitForAllocations();

			freeNodes++;

			if (freeNodes > MAX_FREE) {
				freeNodes--; // Expect a deletion
				currentTotalSize--;
			}

			Assert.assertEquals(currentTotalSize, hudsonSlavePoolService.getStatus().getTotalNodes());
			Assert.assertEquals(freeNodes, hudsonSlavePoolService.getStatus().getFreeNodes());
			Assert.assertEquals(provisionedIps.size() - i - 1, hudsonSlavePoolService.getStatus().getNodesOnLoan());
		}

		context.assertIsSatisfied();
	}

	private int maxWaits = 50;
	private NodeLifecycleService mockNodeLifecycleService;
	private NodeCleaningService mockNodeCleaningService;

	private void waitForAllocations() throws InterruptedException {
		Thread.sleep(100);

		int numWaits = 0;
		while (hudsonSlavePoolServiceImpl.isAllocating()) {
			numWaits++;
			if (numWaits > maxWaits) {
				Assert.fail("allocations not happening");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
