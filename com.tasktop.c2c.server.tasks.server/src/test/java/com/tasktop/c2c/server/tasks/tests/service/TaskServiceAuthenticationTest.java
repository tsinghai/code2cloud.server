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
package com.tasktop.c2c.server.tasks.tests.service;

import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.auth.service.AuthUtils;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockTaskFactory;


@Ignore
// HTTP Auth no longer works since we're pushing auth tokens from the proxy
// server.
//
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml", "/applicationContext-testAuthentication.xml" })
@Transactional
public class TaskServiceAuthenticationTest {
	@Autowired
	private TaskServiceClient taskServiceClient;

	@Autowired
	private WebApplicationContainerBean container;

	@Before
	public void before() {
		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				TaskServiceAuthenticationTest.class));
		container.start();
		taskServiceClient.setBaseUrl(container.getBaseUrl() + "tasks");
	}

	@After
	public void after() {
		logout();
		if (container != null) {
			container.stop();
			container = null;
		}
	}

	private void login(String username, String password) {
		// NOTE: the token type and password are propagated so that they can be reused
		// elsewhere (see rest client)
		Authentication authentication = new UsernamePasswordAuthenticationToken(username, password,
				AuthUtils.toGrantedAuthorities(Collections.singletonList(Role.User)));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void logout() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticationFailedNoCredentials() throws Exception {
		taskServiceClient.createTask(MockTaskFactory.createDO());
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticationFailedBadCredentials() throws Exception {
		login("foo", "asldfkj03209r2");

		taskServiceClient.createTask(MockTaskFactory.createDO());
	}

	@Test
	public void testAuthenticationFailedBadThenCorrectedCredentials() {

		login("testuser", "asldfkj03209r2");
		try {
			taskServiceClient.getTaskSummary();
			fail("Expected exception");
		} catch (BadCredentialsException e) {
			// expected
		}
		login("testuser", "test123");
		taskServiceClient.getTaskSummary();
	}
}
