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

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolServiceClient;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolServiceController;
import com.tasktop.c2c.server.cloud.service.RequestBuildSlaveResult;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService.PoolStatus;
import com.tasktop.c2c.server.cloud.service.RequestBuildSlaveResult.Type;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;

@ContextConfiguration({ "/applicationContext-test.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class HudsonSlavePoolServiceClientTest {

	@Autowired
	private WebApplicationContainerBean container;

	@Autowired
	private HudsonSlavePoolServiceClient slaveServiceClient;

	@Autowired
	private RestTemplate restTemplate;

	private HudsonSlavePoolService mockSlaveService;

	private Mockery context = new JUnit4Mockery();

	private static final String SLAVE_IP = "10.0.0.1";
	private static final String PROJECT_IDENTIFIER = "projectIdentifier";

	@Before
	public void before() throws ValidationException {
		slaveServiceClient.setRestTemplate(restTemplate);
		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				HudsonSlavePoolServiceClientTest.class));
		container.start();
		slaveServiceClient.setBaseUrl(container.getBaseUrl() + "slave");

		mockSlaveService = context.mock(HudsonSlavePoolService.class);

		final PoolStatus status = new PoolStatus();
		status.setFreeNodes(10);
		status.setNodesOnLoan(2);
		status.setTotalNodes(13);

		context.checking(new Expectations() {
			{
				oneOf(mockSlaveService).acquireSlave(with(PROJECT_IDENTIFIER), (String) with(equal(null)));
				will(returnValue(RequestBuildSlaveResult.forPromise("XXX")));

				oneOf(mockSlaveService).acquireSlave(with(PROJECT_IDENTIFIER), (String) with(equal(null)));
				will(returnValue(RequestBuildSlaveResult.forSlave(SLAVE_IP, null)));

				oneOf(mockSlaveService).releaseSlave(with(PROJECT_IDENTIFIER), with(SLAVE_IP));

				oneOf(mockSlaveService).renewSlave(with(PROJECT_IDENTIFIER), with(SLAVE_IP));
				will(returnValue(RequestBuildSlaveResult.forSlave(SLAVE_IP, null)));

				oneOf(mockSlaveService).getStatus();
				will(returnValue(status));

			}
		});
		HudsonSlavePoolServiceController.setLastInstantiatedService(mockSlaveService);
	}

	@After
	public void after() {
		if (container != null) {
			container.stop();
			container = null;
		}
	}

	@Test
	public void testAcquireRenewRelease() throws ValidationException {

		RequestBuildSlaveResult result = slaveServiceClient.acquireSlave(PROJECT_IDENTIFIER, null);
		Assert.assertEquals(Type.PROMISE, result.getType());

		result = slaveServiceClient.acquireSlave(PROJECT_IDENTIFIER, null);
		Assert.assertEquals(Type.SLAVE, result.getType());
		Assert.assertEquals(SLAVE_IP, result.getSlaveIp());
		result = slaveServiceClient.renewSlave(PROJECT_IDENTIFIER, SLAVE_IP);
		Assert.assertEquals(Type.SLAVE, result.getType());
		Assert.assertEquals(SLAVE_IP, result.getSlaveIp());
		slaveServiceClient.releaseSlave(PROJECT_IDENTIFIER, SLAVE_IP);
		slaveServiceClient.getStatus();
		context.assertIsSatisfied();
	}

}
