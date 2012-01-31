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
package com.tasktop.c2c.server.common.service.tests.ajp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.RFC2965Spec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.service.tests.ajp.TestServlet.Payload;
import com.tasktop.c2c.server.common.service.tests.ajp.TestServlet.Request;
import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;
import com.tasktop.c2c.server.web.proxy.AjpProxy;


@ContextConfiguration({ "/applicationContext-testAjpProtocol.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class AjpProxyWebTest {

	@Autowired
	private AjpProxy proxy;

	@Autowired
	private WebApplicationContainerBean container;

	@Before
	public void before() {
		TestServlet.setResponsePayload(null);
		TestServlet.setLastRequest(null);

		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				AjpProtocolTest.class));
		container.setEnableAjp(true);
		container.start();
	}

	@After
	public void after() throws Exception {
		TestServlet.setResponsePayload(null);
		TestServlet.setLastRequest(null);
	}

	@Test
	public void testProxyHandlesCookies() throws Exception {
		final String ajpBaseUri = String.format("ajp://localhost:%s", container.getAjpPort());
		Payload payload = new Payload(HttpServletResponse.SC_OK, "some content\none two three\n\nfour");
		payload.getResponseHeaders().put("foo", "bar");
		payload.getSessionVariables().put("s1", "v1");
		TestServlet.setResponsePayload(payload);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/test");
		request.setQueryString("a=b");
		request.setParameter("a", new String[] { "b" });
		request.addHeader("c", "d ef");
		MockHttpServletResponse response = new MockHttpServletResponse();
		proxy.proxyRequest(ajpBaseUri + "/foo", request, response);

		Request firstRequest = null;
		for (int i = 0; i < 100; i++) {
			firstRequest = TestServlet.getLastRequest();

			// If our request is not yet there, then pause and retry shortly - proxying is an async process, and this
			// request was sometimes coming back as null which was causing test failures on the first assert below.
			if (firstRequest == null) {
				Thread.sleep(10);
			} else {
				// Got our request, so break now.
				break;
			}
		}

		Assert.assertTrue(firstRequest.isNewSession());
		Assert.assertEquals("v1", firstRequest.getSessionAttributes().get("s1"));

		List<org.apache.commons.httpclient.Cookie> cookies = new ArrayList<org.apache.commons.httpclient.Cookie>();
		for (String headerName : response.getHeaderNames()) {
			if (headerName.equalsIgnoreCase("set-cookie") || headerName.equalsIgnoreCase("set-cookie2")) {
				cookies.addAll(Arrays.asList(new RFC2965Spec().parse("localhost", container.getPort(), "/", false,
						response.getHeader(headerName).toString())));
			}
		}
		Assert.assertEquals(1, cookies.size());
		Cookie cookie = cookies.get(0);
		Assert.assertEquals("almp.JSESSIONID", cookie.getName());

		MockHttpServletRequest request2 = new MockHttpServletRequest();
		request2.setMethod("GET");
		request2.setRequestURI("/test");
		request2.addHeader("Cookie", cookie.toExternalForm());
		MockHttpServletResponse response2 = new MockHttpServletResponse();

		payload = new Payload(HttpServletResponse.SC_OK, "test");
		TestServlet.setResponsePayload(payload);

		proxy.proxyRequest(ajpBaseUri + "/foo", request2, response2);

		Request secondRequest = TestServlet.getLastRequest();
		Assert.assertFalse(secondRequest.isNewSession());
		Assert.assertEquals(firstRequest.getSessionId(), secondRequest.getSessionId());
		Assert.assertEquals("v1", secondRequest.getSessionAttributes().get("s1"));
	}
}
