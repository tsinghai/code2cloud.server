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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ProfileWebServiceClientTest extends ProfileWebServiceTest {
	@Autowired
	private ProfileWebServiceClient profileWebServiceClient;

	@Autowired
	private WebApplicationContainerBean container;

	private static WebApplicationContainerBean staticContainer;

	private static boolean initialized = false;

	@Before
	public void before() {

		if (!initialized) {
			staticContainer = container;
			staticContainer.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
					ProfileWebServiceClientTest.class));
			staticContainer.start();
			profileWebServiceClient.setBaseUrl(staticContainer.getBaseUrl() + "profile");

			initialized = true;
		}
		super.profileWebService = profileWebServiceClient;

	}

	@AfterClass
	public static void after() {
		if (staticContainer != null) {
			staticContainer.stop();
			staticContainer = null;
		}
	}

	@Test
	public void serviceAcceptsNonCompliantJSONMediaTypes() throws HttpException, IOException {
		String baseUrl = profileWebServiceClient.getBaseUrl();
		for (String mediaType : new String[] { "application/json", "application/x-javascript", "text/javascript",
				"text/x-javascript", "text/x-json", "text/json" }) {
			HttpClient client = new HttpClient();
			HttpMethod get = new GetMethod(baseUrl + "/"
					+ ProfileWebServiceClient.GET_PROJECTS_URL.replaceAll("\\{.*?\\}", "123"));
			get.addRequestHeader("Accept", mediaType);
			int rc = client.executeMethod(get);
			String responseBody = get.getResponseBodyAsString();

			assertTrue("Expected JSON response for media type \"" + mediaType + "\" but got " + responseBody,
					responseBody.trim().startsWith("{\"error\":{\"message\":"));
		}
	}

}
