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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
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
import com.tasktop.c2c.server.web.proxy.ajp.AjpPoolableConnectionFactory;
import com.tasktop.c2c.server.web.proxy.ajp.AjpProtocol;
import com.tasktop.c2c.server.web.proxy.ajp.Packet;


@ContextConfiguration({ "/applicationContext-testAjpProtocol.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class AjpProtocolTest {

	@Autowired
	private WebApplicationContainerBean container;

	@Autowired
	private KeyedObjectPool socketPool;

	private AjpProtocol protocol;

	@Before
	public void before() {
		TestServlet.setResponsePayload(null);
		TestServlet.setLastRequest(null);

		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				AjpProtocolTest.class));
		container.setEnableAjp(true);
		container.start();

		protocol = new AjpProtocol();
		protocol.setProxyHost("localhost");
		protocol.setProxyPort(container.getAjpPort());
		protocol.setSocketPool(socketPool);
	}

	@After
	public void after() throws UnsupportedOperationException, Exception {
		TestServlet.setResponsePayload(null);
		TestServlet.setLastRequest(null);

		socketPool.clear();
	}

	@Test
	public void testSocketPool() throws Exception {
		Object socketKey = new AjpPoolableConnectionFactory.Key("localhost", container.getAjpPort());

		Socket s1 = (Socket) socketPool.borrowObject(socketKey);
		Socket s2 = (Socket) socketPool.borrowObject(socketKey);
		junit.framework.Assert.assertNotSame(s1, s2);
		socketPool.returnObject(socketKey, s1);
		Socket s3 = (Socket) socketPool.borrowObject(socketKey);
		junit.framework.Assert.assertSame(s3, s1);
	}

	@Test
	public void testGet() throws IOException {
		Payload payload = new Payload(HttpServletResponse.SC_OK, "some content\none two three\n\nfour");
		payload.getResponseHeaders().put("foo", "bar");
		TestServlet.setResponsePayload(payload);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/test");
		request.setQueryString("a=b");
		request.setParameter("a", new String[] { "b" });
		request.addHeader("c", "d ef");
		MockHttpServletResponse response = new MockHttpServletResponse();
		protocol.forward(request, response);

		assertRequestIsExpected(request, TestServlet.getLastRequest());
		assertResponseIsExpected(payload, response);
	}

	@Test
	public void testGetLargePayload() throws IOException {
		int resonseDataSize = (Packet.MAX_SIZE * 3) + 243;
		byte[] responseData = createData(resonseDataSize);
		Payload payload = new Payload(HttpServletResponse.SC_OK, responseData);
		payload.getResponseHeaders().put("foo", "bar");
		TestServlet.setResponsePayload(payload);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/testGetLargePayload");

		MockHttpServletResponse response = new MockHttpServletResponse();
		protocol.forward(request, response);

		assertRequestIsExpected(request, TestServlet.getLastRequest());
		assertResponseIsExpected(payload, response);
	}

	@Test
	public void testPostData() throws IOException {
		Payload payload = new Payload(HttpServletResponse.SC_CREATED, "some content\none two three\n\nfour");
		payload.getResponseHeaders().put("foo", "bar");
		TestServlet.setResponsePayload(payload);

		String formContent = "a=b&c=def";
		byte[] requestContent = formContent.getBytes();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/testPostData");
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Content-Length", requestContent.length);
		request.setContent(requestContent);

		MockHttpServletResponse response = new MockHttpServletResponse();
		protocol.forward(request, response);

		assertRequestIsExpected(request, TestServlet.getLastRequest());
		assertResponseIsExpected(payload, response);
	}

	@Test
	public void testMultipleSuccessiveRequestsOnOneConnection() throws IOException {
		GenericKeyedObjectPool uniSocketPool = new GenericKeyedObjectPool(new AjpPoolableConnectionFactory());
		uniSocketPool.setLifo(true);
		uniSocketPool.setMaxIdle(1);
		uniSocketPool.setMaxTotal(1);
		uniSocketPool.setMinIdle(1);
		protocol.setSocketPool(uniSocketPool);
		final int numRequests = 50;
		for (int x = 0; x < numRequests; ++x) {
			System.out.println("request: " + x);
			Payload payload = new Payload();
			MockHttpServletRequest request = new MockHttpServletRequest();
			if (x % 3 == 0) {
				payload.setResponseCode(HttpServletResponse.SC_CREATED);
				payload.setCharacterContent("some content " + x);
				payload.getResponseHeaders().put("foo", "bar");
				TestServlet.setResponsePayload(payload);

				String formContent = "a=b&c=def";
				byte[] requestContent = formContent.getBytes();

				request.setMethod("PUT");
				request.setRequestURI("/testPostData" + x);
				request.addHeader("Content-Type", "application/x-www-form-urlencoded");
				request.addHeader("Content-Length", requestContent.length);
				request.setContent(requestContent);
			} else {
				payload.setResponseCode(HttpServletResponse.SC_OK);
				payload.setBinaryContent(createData((x * 1024) + 3));
				payload.getResponseHeaders().put("Content-Length", Integer.toString(payload.getBinaryContent().length));
				payload.getResponseHeaders().put("Content-Type", "unknown");
				payload.getResponseHeaders().put("TestPayloadNumber",
						Integer.toHexString(x) + '/' + Integer.toHexString(numRequests));
				TestServlet.setResponsePayload(payload);

				request.setMethod("GET");
				request.setRequestURI("/test" + x);
			}

			MockHttpServletResponse response = new MockHttpServletResponse();
			protocol.forward(request, response);

			assertRequestIsExpected(request, TestServlet.getLastRequest());
			assertResponseIsExpected(payload, response);
		}
	}

	private static final String GET_RESPONSE_PAYLOAD = "001f# service=git-receive-pack\n"
			+ "000000720667c78f90e8a397feaef34b7179b49823550a8a refs/heads/master\n"
			+ " report-status delete-refs side-band-64k ofs-delta\n" + "0000";
	private static final String POST_RESPONSE_PAYLOAD = "0030000eunpack ok\n0019ok refs/heads/master\n00000000";
	private static final String AUTH_HEADER = "almtoken H4sIAAAAAAAAAH2QvU4CQRDHBw7IgYkabbCwtzCH6MmdsZEECuKJCWhhZQZYYOE+cHcODmNM6HwFC9"
			+ "/A97C39BWM7+AeHzE0bjGZyW/2P/+Z9x9ISwFWO/AMQjmkYGSg6xmSiTETBobUn+e8zYyyKphPvI3EA/8mGDIfFi+RBN2Bjbg7EJw4kwTbzgDHWAiJuwWHSzp3IM0"
			+ "85C7BzgK56PcKTRLc7ymYYdGIi+n6xwoSUyzb5UJSHT32AM+QVM1cypB14kpzQBuy6RLoLq416qFy76/qaKR23Y3ljVjeKAuB09hcNPvcf/3ANw0SNUhJ/qjMqL2Sk5S"
			+ "KWwTZxrVTvb9tVhsEuXlerlzV6hFBvu1ynwwvED30L1YnVOdUkzb/JsWL9AezxOUd7WkA0Wiiq7Md5p++dCWSnovI2GNuCarfLwocnJkts9S1rdaxddQqnhZtq9S10D"
			+ "Y7zDKLJ2ij2WmVFCPILCz84+gXFhb4XuwBAAA=";

	@Test
	// Trying to recreate task 718
	public void testGetThenPost() throws Exception {
		final byte[] POST_REQUEST_PAYLOAD = createData(431);

		GenericKeyedObjectPool uniSocketPool = new GenericKeyedObjectPool(new AjpPoolableConnectionFactory());
		uniSocketPool.setLifo(true);
		uniSocketPool.setMaxIdle(1);
		uniSocketPool.setMaxTotal(1);
		uniSocketPool.setMinIdle(1);
		protocol.setSocketPool(uniSocketPool);

		for (int i = 0; i < 10; i++) {
			MockHttpServletRequest getRequest = new MockHttpServletRequest();

			getRequest.setMethod("GET");
			getRequest.setRequestURI("/alm/s/code2cloud/scm/test4.git/info/refs");
			getRequest.setQueryString("service=git-receive-pack");
			getRequest.addParameter("service", "git-receive-pack");
			getRequest.addHeader("Authorization", AUTH_HEADER);
			getRequest.addHeader("Host", "localhost:8888");
			getRequest.addHeader("User-Agent", "git/1.7.3.1");
			getRequest.addHeader("Accept", "*/*");
			getRequest.addHeader("Pragma", "no-cache");
			Payload getPayload = new Payload();
			getPayload.setResponseCode(HttpServletResponse.SC_OK);
			getPayload.setCharacterContent(GET_RESPONSE_PAYLOAD);

			MockHttpServletResponse response = new MockHttpServletResponse();
			TestServlet.setResponsePayload(getPayload);
			protocol.forward(getRequest, response);
			assertRequestIsExpected(getRequest, TestServlet.getLastRequest());
			assertResponseIsExpected(getPayload, response);

			MockHttpServletRequest postRequest = new MockHttpServletRequest();
			postRequest.setMethod("POST");
			postRequest.setRequestURI("/alm/s/code2cloud/scm/test4.git/git-receive-pack");
			postRequest.addHeader("Authorization", AUTH_HEADER);
			postRequest.addHeader("Host", "localhost:8888");
			postRequest.addHeader("Content-Length", "" + POST_REQUEST_PAYLOAD.length);
			postRequest.addHeader("Accept-Encoding", "deflate, gzip");
			postRequest.addHeader("User-Agent", "git/1.7.3.1");
			postRequest.addHeader("Accept", "application/x-git-receive-pack-result");
			postRequest.addHeader("Content-Type", "application/x-git-receive-pack-request");

			postRequest.setContent(POST_REQUEST_PAYLOAD);

			Payload postPayload = new Payload();
			postPayload.setResponseCode(HttpServletResponse.SC_OK);
			postPayload.setCharacterContent(POST_RESPONSE_PAYLOAD);

			response = new MockHttpServletResponse();
			TestServlet.setResponsePayload(postPayload);
			protocol.forward(postRequest, response);
			assertRequestIsExpected(postRequest, TestServlet.getLastRequest());
			assertResponseIsExpected(postPayload, response);
		}

	}

	@Test
	public void testMultipleSuccessiveGets() throws IOException {
		GenericKeyedObjectPool uniSocketPool = new GenericKeyedObjectPool(new AjpPoolableConnectionFactory());
		uniSocketPool.setLifo(true);
		uniSocketPool.setMaxIdle(1);
		uniSocketPool.setMaxTotal(1);
		uniSocketPool.setMinIdle(1);
		protocol.setSocketPool(uniSocketPool);
		final int numRequests = 50;
		for (int x = 0; x < numRequests; ++x) {
			System.out.println("request: " + x);
			Payload payload = new Payload();
			MockHttpServletRequest request = new MockHttpServletRequest() {
				@Override
				public int getContentLength() {
					return -1;
				}
			};

			payload.setResponseCode(HttpServletResponse.SC_OK);
			payload.setBinaryContent(createData((x + 3) * 512));
			payload.setCharacterContent("some content " + x);
			payload.getResponseHeaders().put("foo", "bar");
			TestServlet.setResponsePayload(payload);

			request.setMethod("GET");
			request.setRequestURI("/testGet" + x);
			request.addHeader("Content-Type", "text/plain");

			MockHttpServletResponse response = new MockHttpServletResponse();
			protocol.forward(request, response);

			assertRequestIsExpected(request, TestServlet.getLastRequest());
			assertResponseIsExpected(payload, response);
		}
	}

	Random random = new Random();

	private byte[] createData(int size) {
		byte[] data = new byte[size];
		for (int x = 0; x < data.length; ++x) {
			data[x] = (byte) (Math.abs(random.nextInt()) % Byte.MAX_VALUE);
		}
		return data;
	}

	private void assertRequestIsExpected(MockHttpServletRequest request, Request lastRequest) {
		Assert.assertEquals(request.getRequestURI(), lastRequest.getRequestURI());
		Assert.assertEquals(request.getQueryString() == null ? "" : request.getQueryString(),
				lastRequest.getQueryString());

		int numParams = 0;
		for (Enumeration<String> paramName = request.getParameterNames(); paramName.hasMoreElements();) {
			++numParams;
			String name = paramName.nextElement();
			Assert.assertArrayEquals(request.getParameterValues(name), lastRequest.getParameters().get(name));
		}
		Assert.assertEquals(numParams, lastRequest.getParameters().size());

		int numHeaders = 0;
		for (Enumeration<String> headerName = request.getHeaderNames(); headerName.hasMoreElements();) {
			++numHeaders;
			String name = headerName.nextElement();
			Assert.assertEquals(request.getHeader(name), lastRequest.getHeaders().get(name));
		}
		Assert.assertEquals(numHeaders, lastRequest.getHeaders().size());
	}

	private void assertResponseIsExpected(Payload expectedPayload, MockHttpServletResponse response)
			throws UnsupportedEncodingException {
		Assert.assertEquals(expectedPayload.responseCode, response.getStatus());
		for (Entry<String, String> header : expectedPayload.getResponseHeaders().entrySet()) {
			Assert.assertEquals(header.getValue(), response.getHeader(header.getKey()));
		}
		if (expectedPayload.binaryContent != null) {
			Assert.assertArrayEquals(expectedPayload.binaryContent, response.getContentAsByteArray());
		} else if (expectedPayload.characterContent != null) {
			Assert.assertEquals(expectedPayload.characterContent, response.getContentAsString());
		}
	}
}
