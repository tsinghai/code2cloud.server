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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.CustomAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.tasktop.c2c.server.cloud.domain.Node;
import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.domain.Task;
import com.tasktop.c2c.server.cloud.domain.Task.Status;
import com.tasktop.c2c.server.cloud.service.BasicNodeCreationService;
import com.tasktop.c2c.server.cloud.service.CloudService;
import com.tasktop.c2c.server.cloud.service.NodeLifecycleService;
import com.tasktop.c2c.server.cloud.service.NodeLifecycleServiceProvider;
import com.tasktop.c2c.server.cloud.service.NodeReadyStrategy;
import com.tasktop.c2c.server.cloud.service.NodeWithExtraDiskCreationService;
import com.tasktop.c2c.server.cloud.service.PoolingNodeProvisioningService;
import com.tasktop.c2c.server.cloud.service.ServiceHostService;
import com.tasktop.c2c.server.cloud.service.Template;
import com.tasktop.c2c.server.common.service.job.JobService;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;

@ContextConfiguration({ "/applicationContext-testNoRollback.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
// @Transactional -- If this test is transactional it will fail because the deamon threads will stall waiting on this
// transaction to complete
@Ignore
// FIXME task 2279
public class PoolingNodeProvisioningServiceTest {
	private static final String TEMPLATE_NODE_NAME = "template-node";
	private static final int POOL_SIZE = 2;
	private static final int MAX_TENANTS_PER_NODE = 5;

	private Mockery context;

	private PoolingNodeProvisioningService poolingNodeProvisioningService;
	private final BasicNodeCreationService nodeCreationService = new NodeWithExtraDiskCreationService();

	@Autowired
	private NodeLifecycleServiceProvider nodeLifecycleServiceProvider;

	private final Random random = new Random();
	private final Map<String, Integer> numTenatsByIp = new HashMap<String, Integer>();

	@Autowired
	private PlatformTransactionManager trxManager;

	@Autowired
	private ServiceHostService serviceHostService;

	@Autowired
	private JobService jobService;

	@PersistenceContext
	private EntityManager entityManager;

	@Before
	public void before() throws Exception {
		clearTables();
		context = new JUnit4Mockery();
		final Template mockTemplate = new Template();
		mockTemplate.setIdentity("mockTemplateIdentity");
		mockTemplate.setName(TEMPLATE_NODE_NAME);

		final CloudService mockCloudService = context.mock(CloudService.class);

		final NodeReadyStrategy mockNodeReadyStrategy = context.mock(NodeReadyStrategy.class);

		context.checking(new Expectations() {
			{
				allowing(mockCloudService).listTemplates();
				will(returnValue(Arrays.asList(mockTemplate)));

				allowing(mockCloudService).createNode(with(mockTemplate), with(any(String.class)));
				will(new TaskAction());

				allowing(mockCloudService).allocateDisk(with(any(Node.class)), with(any(Integer.class)));
				will(new TaskAction());

				allowing(mockCloudService).startNode(with(any(Node.class)));
				will(new TaskAction());

				allowing(mockCloudService).retrieveTask(with(any(Task.class)));
				will(new RetrieveTaskAction());

				allowing(mockCloudService).retrieveNodeByName(with(any(String.class)));
				will(new RetrieveNodeAction());

				atLeast(POOL_SIZE).of(mockNodeReadyStrategy).isNodeReady(with(any(Node.class)));
				will(returnValue(true));
			}
		});

		nodeCreationService.setCloudService(mockCloudService);
		nodeCreationService.setNodeReadyStrategy(mockNodeReadyStrategy);
		nodeCreationService.setTemplateNodeName(TEMPLATE_NODE_NAME);
		nodeCreationService.setTaskUpdatePeriod(0);// for testing

		poolingNodeProvisioningService = new PoolingNodeProvisioningService();
		// poolingNodeProvisioningService.setUpdatePeriod(0);// for testing FIXME
		poolingNodeProvisioningService.setNodeTypes(EnumSet.of(ServiceType.TASKS));
		poolingNodeProvisioningService.setServiceHostService(serviceHostService);
		poolingNodeProvisioningService.setMaxCapacity(MAX_TENANTS_PER_NODE);
		poolingNodeProvisioningService.setPoolSize(POOL_SIZE);
		poolingNodeProvisioningService.setJobService(jobService);
		poolingNodeProvisioningService.setTrxManager(trxManager);
		poolingNodeProvisioningService.setUpdatePeriod(100);
		nodeLifecycleServiceProvider.setServicesByHostType(Collections.singletonMap(ServiceType.TASKS,
				(NodeLifecycleService) nodeCreationService));
		poolingNodeProvisioningService.afterPropertiesSet();

	}

	@After
	public void cleanup() {
		poolingNodeProvisioningService.shutdown();
	}

	private void clearTables() {

		new TransactionTemplate(trxManager).execute(new TransactionCallback<ServiceHost>() {

			@Override
			public ServiceHost doInTransaction(TransactionStatus status) {

				for (com.tasktop.c2c.server.profile.domain.internal.ServiceHost host : (List<com.tasktop.c2c.server.profile.domain.internal.ServiceHost>) entityManager
						.createQuery("SELECT h FROM ServiceHost h").getResultList()) {
					entityManager.remove(host);
				}
				for (ProjectService service : (List<ProjectService>) entityManager.createQuery(
						"SELECT s FROM ProjectService s").getResultList()) {
					entityManager.remove(service);
				}
				return null;
			}
		});

	}

	private List<Task> pendingTasks = new ArrayList<Task>();

	private Task createPendingTask() {
		Task task = new Task();
		task.setStatus(Status.QUEUED);
		pendingTasks.add(task);
		return task;
	}

	private boolean tasksCanComplete = true;

	private Task retrieveTask(Task task) {
		Assert.assertFalse(task.getStatus().isDone());
		Assert.assertTrue(pendingTasks.remove(task));
		boolean done = tasksCanComplete && random.nextBoolean();
		Task result = new Task();
		if (done) {
			result.setStatus(Status.COMPLETE);
		} else {
			result.setStatus(Status.RUNNING);
			pendingTasks.add(result);
		}
		return result;
	}

	private class TaskAction extends CustomAction {

		public TaskAction() {
			super("create task");
		}

		@Override
		public Object invoke(Invocation invocation) throws Throwable {
			return createPendingTask();
		}
	}

	private class RetrieveTaskAction extends CustomAction {

		public RetrieveTaskAction() {
			super("retrieve task");
		}

		@Override
		public Object invoke(Invocation invocation) throws Throwable {
			return retrieveTask((Task) invocation.getParameter(0));
		}
	}

	private class RetrieveNodeAction extends CustomAction {

		public RetrieveNodeAction() {
			super("retrieve node");
		}

		private int nextIp = 0;

		@Override
		public Object invoke(Invocation invocation) throws Throwable {
			Node result = new Node();
			result.setIpAddress("10.0.0." + nextIp++);
			return result;
		}
	}

	@Test
	public void testSingleConsumer() throws Exception {
		waitForAllocations();
		Assert.assertEquals(POOL_SIZE, poolingNodeProvisioningService.getNumNodesBelowCapacity());
		for (int i = 0; i < MAX_TENANTS_PER_NODE * POOL_SIZE * 2; i++) {
			ServiceHost host = doProvision();

			if (numTenatsByIp.containsKey(host.getInternalNetworkAddress())) {
				numTenatsByIp.put(host.getInternalNetworkAddress(),
						numTenatsByIp.get(host.getInternalNetworkAddress()) + 1);
			} else {
				numTenatsByIp.put(host.getInternalNetworkAddress(), 1);
			}
		}
		Assert.assertTrue(numTenatsByIp.size() > POOL_SIZE);
		for (Integer numTenants : numTenatsByIp.values()) {
			Assert.assertTrue(numTenants <= MAX_TENANTS_PER_NODE);
		}
		waitForAllocations();
		Assert.assertEquals(POOL_SIZE, poolingNodeProvisioningService.getNumNodesBelowCapacity());
		context.assertIsSatisfied();
	}

	// Need this to run in a transaction otherwise it seems not to get flushed.
	private ServiceHost doProvision() {
		return new TransactionTemplate(trxManager).execute(new TransactionCallback<ServiceHost>() {

			@Override
			public ServiceHost doInTransaction(TransactionStatus status) {
				ServiceHost host = poolingNodeProvisioningService.provisionNode();
				ProjectService randomService = new ProjectService();
				randomService.setServiceHost(entityManager.find(
						com.tasktop.c2c.server.profile.domain.internal.ServiceHost.class, host.getId()));
				randomService.getServiceHost().getProjectServices().add(randomService);
				entityManager.persist(randomService);
				return host;
			}
		});
	}

	private int maxWaits = 5;

	private void waitForAllocations() throws InterruptedException {
		Thread.sleep(600);
		int numWaits = 0;
		while (poolingNodeProvisioningService.isAllocating()) {
			numWaits++;
			if (numWaits > maxWaits) {
				Assert.fail("allocations not happening");
			}
			Thread.sleep(500);
		}
	}

	@Test
	public void testWithEmptyInitialPool() throws Exception {
		tasksCanComplete = false;
		Assert.assertEquals(0, poolingNodeProvisioningService.getNumNodesBelowCapacity());
		final AtomicBoolean threadCompleted = new AtomicBoolean(false);

		new Thread() {
			@Override
			public void run() {
				doProvision();
				threadCompleted.set(true);
			}
		}.start();
		Thread.sleep(100); // Let the other thread work
		Assert.assertFalse(threadCompleted.get());
		tasksCanComplete = true;
		Thread.sleep(1000);
		Assert.assertTrue(threadCompleted.get());
	}

	@Test
	public void testDNSNameUnique() {
		Set<String> generatedNames = new HashSet<String>();
		for (int i = 0; i < 10000; i++) {
			String name = nodeCreationService.generateDnsName();
			Assert.assertFalse(generatedNames.contains(name));
			generatedNames.add(name);
		}
	}
}
