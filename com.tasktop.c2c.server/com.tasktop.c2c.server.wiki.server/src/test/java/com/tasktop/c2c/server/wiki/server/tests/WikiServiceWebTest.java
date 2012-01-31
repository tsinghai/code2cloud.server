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
package com.tasktop.c2c.server.wiki.server.tests;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.context.ThreadLocalTenancyContextHolderStrategy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.proxy.ProxyPreAuthClientInvocationHandler;
import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;
import com.tasktop.c2c.server.wiki.server.tests.util.TestContextHolderStrategy;
import com.tasktop.c2c.server.wiki.service.WikiServiceClient;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class WikiServiceWebTest extends WikiServiceTest {

	@Autowired
	private WikiServiceClient wikiServiceClient;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private WebApplicationContainerBean _container;

	private static WebApplicationContainerBean container;

	@BeforeClass
	public static void setupTenancyContext() {
		TenancyContextHolder.setStrategy(new TestContextHolderStrategy());
	}

	@AfterClass
	public static void teardownTenancyContext() {
		TenancyContextHolder.setStrategy(new ThreadLocalTenancyContextHolderStrategy());
	}

	@Before
	public void before() {
		if (container == null) {
			container = _container;

			container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
					WikiServiceWebTest.class));
			container.start();
		}
		wikiServiceClient.setRestTemplate(restTemplate);
		wikiServiceClient.setBaseUrl(container.getBaseUrl() + "wiki");
		super.wikiService = ProxyPreAuthClientInvocationHandler.wrap(wikiServiceClient,
				new ProxyPreAuthClientInvocationHandler() {
					@Override
					public AuthenticationToken getAuthenticationToken() {
						AuthenticationServiceUser user = AuthenticationServiceUser.getCurrent();
						return user == null ? null : user.getToken();
					}
				});
		super.before();
	}

	@AfterClass
	public static void stopContainer() {
		if (container != null) {
			container.stop();
			container = null;
		}
	}
}
