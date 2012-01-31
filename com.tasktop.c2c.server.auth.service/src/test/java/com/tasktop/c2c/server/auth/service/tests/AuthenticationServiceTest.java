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
package com.tasktop.c2c.server.auth.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceClient;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-test.xml" })
public class AuthenticationServiceTest {

	@Autowired
	private AuthenticationServiceClient serviceClient;

	@Autowired
	private WebApplicationContainerBean container;

	@Before
	public void before() {
		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				AuthenticationServiceTest.class));
		container.start();
		serviceClient.setBaseUrl(container.getBaseUrl() + "auth");
	}

	@After
	public void after() {
		if (container != null) {
			container.stop();
			container = null;
		}
	}

	@Test(expected = AuthenticationException.class)
	public void testFailedAuth() throws Exception {
		serviceClient.authenticate("foo", "bar");
	}

	@Test
	public void testAuthSuccess() throws AuthenticationException {
		AuthenticationToken token = serviceClient.authenticate("testuser", "test123");
		assertNotNull(token);
		assertNotNull(token.getKey());
		assertNotNull(token.getIssued());
		assertNotNull(token.getExpiry());
		assertTrue(token.getExpiry().after(token.getIssued()));
		assertEquals("testuser", token.getUsername());

		assertNotNull(token.getAuthorities());
		// FIXME
		assertTrue(token.getAuthorities().contains(Role.User));
	}

}
