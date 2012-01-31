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
package com.tasktop.c2c.server.common.service.tests.http;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.tasktop.c2c.server.web.proxy.HttpProxy;


public class HttpProxyTest {
	private Mockery context = new Mockery();
	private ClientHttpRequestFactory requestFactory = context.mock(ClientHttpRequestFactory.class);
	private ClientHttpRequest proxyRequest = context.mock(ClientHttpRequest.class);
	private HttpHeaders proxyRequestHeaders = new HttpHeaders();
	private HttpHeaders proxyResponseHeaders = new HttpHeaders();
	private ClientHttpResponse proxyResponse = context.mock(ClientHttpResponse.class);
	private ByteArrayInputStream proxyResponseInputStream = new ByteArrayInputStream(new byte[] {});
	private ByteArrayOutputStream proxyRequestOutputStream = new ByteArrayOutputStream();

	private HttpProxy proxy = new HttpProxy();

	private void setupMock(final HttpMethod method, final HttpStatus status) throws IOException {

		proxy.setRequestFactory(requestFactory);

		context.checking(new Expectations() {
			{
				oneOf(requestFactory).createRequest(with(any(URI.class)), with(method));
				will(returnValue(proxyRequest));

				oneOf(proxyRequest).execute();
				will(returnValue(proxyResponse));
				allowing(proxyRequest).getHeaders();
				will(returnValue(proxyRequestHeaders));
				allowing(proxyRequest).getBody();
				will(returnValue(proxyRequestOutputStream));
				allowing(proxyRequest);
				allowing(proxyResponse).getHeaders();
				will(returnValue(proxyResponseHeaders));
				allowing(proxyResponse).getStatusCode();
				will(returnValue(status));
				allowing(proxyResponse).getBody();
				will(returnValue(proxyResponseInputStream));
				allowing(proxyResponse);

			}
		});

	}

	@Test
	public void testCanProxy() {
		assertTrue(proxy.canProxyRequest("http://foo.bar/baz", new MockHttpServletRequest()));
		assertTrue(proxy.canProxyRequest("https://foo.bar/baz", new MockHttpServletRequest()));
	}

	@Test
	public void testBasicGet() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);
		MockHttpServletRequest clientRequest = new MockHttpServletRequest("GET", "unused");
		String randomRequestHeader = "RandomRequestHeader";
		String randomResponseHeader = "RandomResponseHeader";
		String connectionHeader = "Connection";

		clientRequest.addHeader(randomRequestHeader, "RandomHeaderValue");
		clientRequest.addHeader(connectionHeader, "ConnectionValue");
		proxyResponseHeaders.add(randomResponseHeader, "RandomHeaderValue");
		proxyResponseHeaders.add(connectionHeader, "ConnectionValue"); // Should not be passed along.
		MockHttpServletResponse clientResponse = new MockHttpServletResponse();

		proxy.proxyRequest("foo", clientRequest, clientResponse);

		Assert.assertTrue(proxyRequestHeaders.containsKey(randomRequestHeader));
		Assert.assertFalse(proxyRequestHeaders.containsKey(connectionHeader));
		Assert.assertTrue(clientResponse.containsHeader(randomResponseHeader));
		Assert.assertFalse(clientResponse.containsHeader(connectionHeader)); // FIXME, unsure if this req is correct

		context.assertIsSatisfied();
	}

	@Test
	public void testFailedGet() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.SERVICE_UNAVAILABLE);
		MockHttpServletRequest clientRequest = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse clientResponse = new MockHttpServletResponse();

		proxy.proxyRequest("foo", clientRequest, clientResponse);

		Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), clientResponse.getStatus());

		context.assertIsSatisfied();
	}

	@Test
	public void testPost() throws IOException {
		byte[] proxyResponseContent = "ProxyResponse".getBytes();
		proxyResponseInputStream = new ByteArrayInputStream(proxyResponseContent);

		setupMock(HttpMethod.POST, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		byte[] requestContect = "RequestContent".getBytes();
		request.setContent(requestContect);
		proxy.proxyRequest("foo", request, response);

		Assert.assertArrayEquals(proxyRequestOutputStream.toByteArray(), requestContect);
		Assert.assertArrayEquals(proxyResponseContent, response.getContentAsByteArray());

		context.assertIsSatisfied();
	}

	@Test
	public void testSetCookieNameMapping() throws IOException {
		doTestSetCookieNameTranslated("Set-Cookie");
	}

	@Test
	public void testSetCookie2NameMapping() throws IOException {
		// per http://tools.ietf.org/html/rfc2965
		doTestSetCookieNameTranslated("Set-Cookie2");
	}

	protected void doTestSetCookieNameTranslated(String setCookieHeaderName) throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		final String jsessionId = UUID.randomUUID().toString();
		final String originalHeaderValue = "JSESSIONID=\"" + jsessionId + "\"; Version=\"1\"; Path=\"/acme\"";

		proxyResponseHeaders.add(setCookieHeaderName, originalHeaderValue);

		proxy.proxyRequest("foo", request, response);

		String setCookieValue = (String) response.getHeader(setCookieHeaderName);
		Assert.assertNotNull(setCookieValue);
		Assert.assertEquals("almp." + originalHeaderValue, setCookieValue);

		context.assertIsSatisfied();
	}

	@Test
	public void testCookieRequestNonPrefixedCookiesFiltered() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		final String cookieHeaderValue = "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\";Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"; Shipping=\"FedEx\"; $Path=\"/acme\"";
		request.addHeader("Cookie", cookieHeaderValue);

		proxy.proxyRequest("foo", request, response);

		List<String> proxyCookie = proxyRequestHeaders.get("Cookie");
		Assert.assertNull(proxyCookie);
		context.assertIsSatisfied();
	}

	@Test
	public void testCookieRequestNonPrefixedCookiesFiltered2() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		final String cookieHeaderValue = "$Version=\"1\"; almp.Customer=\"WILE_E_COYOTE\"; $Path=\"/acme1\";Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme2\"; Shipping=\"FedEx\"; $Path=\"/acme\"";
		request.addHeader("Cookie", cookieHeaderValue);

		proxy.proxyRequest("foo", request, response);

		List<String> proxyCookie = proxyRequestHeaders.get("Cookie");
		Assert.assertNotNull(proxyCookie);
		Assert.assertEquals(1, proxyCookie.size());
		Assert.assertEquals("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme1\";", proxyCookie.get(0));
		context.assertIsSatisfied();
	}

	@Test
	public void testCookieRequestNonPrefixedCookiesFiltered3() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		final String cookieHeaderValue = "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme1\"; almp.Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme2\"; Shipping=\"FedEx\"; $Path=\"/acme\"";
		request.addHeader("Cookie", cookieHeaderValue);

		proxy.proxyRequest("foo", request, response);

		List<String> proxyCookie = proxyRequestHeaders.get("Cookie");
		Assert.assertNotNull(proxyCookie);
		Assert.assertEquals(1, proxyCookie.size());
		Assert.assertEquals("$Version=\"1\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme2\";",
				proxyCookie.get(0));
		context.assertIsSatisfied();
	}

	@Test
	public void testCookieRequestCookieValueUnquoted() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		final String cookieHeaderValue = "$Version=\"1\"; almp.Part_Number=Rocket_Launcher_0001; $Path=\"/acme2\";";
		request.addHeader("Cookie", cookieHeaderValue);

		proxy.proxyRequest("foo", request, response);

		List<String> proxyCookie = proxyRequestHeaders.get("Cookie");
		Assert.assertNotNull(proxyCookie);
		Assert.assertEquals(1, proxyCookie.size());
		Assert.assertEquals("$Version=\"1\"; Part_Number=Rocket_Launcher_0001; $Path=\"/acme2\";", proxyCookie.get(0));
		context.assertIsSatisfied();
	}

	@Test
	public void testCookieRequestCookieNoTrailingSemi() throws IOException {
		setupMock(HttpMethod.GET, HttpStatus.OK);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "unused");
		MockHttpServletResponse response = new MockHttpServletResponse();

		final String cookieHeaderValue = "$Version=\"1\"; almp.Part_Number=\"Rocket_Launcher_0001\"";
		request.addHeader("Cookie", cookieHeaderValue);

		proxy.proxyRequest("foo", request, response);

		List<String> proxyCookie = proxyRequestHeaders.get("Cookie");
		Assert.assertNotNull(proxyCookie);
		Assert.assertEquals(1, proxyCookie.size());
		Assert.assertEquals("$Version=\"1\"; Part_Number=\"Rocket_Launcher_0001\";", proxyCookie.get(0));
		context.assertIsSatisfied();
	}
}
