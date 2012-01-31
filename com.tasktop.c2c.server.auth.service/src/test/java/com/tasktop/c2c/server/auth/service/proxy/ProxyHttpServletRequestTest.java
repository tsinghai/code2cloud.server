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
package com.tasktop.c2c.server.auth.service.proxy;

import java.util.Enumeration;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.tasktop.c2c.server.auth.service.proxy.ProxyHttpServletRequest;
import com.tasktop.c2c.server.common.service.web.HeaderConstants;


public class ProxyHttpServletRequestTest {

	private MockHttpServletRequest mockRequest = new MockHttpServletRequest();
	private ProxyHttpServletRequest proxyRequest = new ProxyHttpServletRequest(mockRequest);

	@Test
	public void testAllowsRandomHeader() {
		String randomHeader = "RandomHeader";
		mockRequest.addHeader(randomHeader, "WhoCares");

		boolean found = false;
		for (Enumeration<String> e = proxyRequest.getHeaderNames(); e.hasMoreElements();) {
			String headerName = e.nextElement();
			if (headerName.equals(randomHeader)) {
				found = true;
			}
		}
		Assert.assertTrue(found);

		String value = proxyRequest.getHeader(randomHeader);
		Assert.assertNotNull(value);

		Enumeration<String> values = proxyRequest.getHeaders(randomHeader);
		Assert.assertTrue(values.hasMoreElements());
	}

	@Test
	public void testBlocksTenantHeader() {

		mockRequest.addHeader(HeaderConstants.TENANT_HEADER, "SpoofedTenant");

		for (Enumeration<String> e = proxyRequest.getHeaderNames(); e.hasMoreElements();) {
			String headerName = e.nextElement();
			Assert.assertFalse(headerName.equals(HeaderConstants.TENANT_HEADER));
		}

		String value = proxyRequest.getHeader(HeaderConstants.TENANT_HEADER);
		Assert.assertNull(value);

		Enumeration<String> values = proxyRequest.getHeaders(HeaderConstants.TENANT_HEADER);
		Assert.assertFalse(values.hasMoreElements());
	}

	@Test
	public void testBlocksAlmAuthHeader() {

		mockRequest.addHeader(HeaderConstants.PREAUTH_AUTHORIZATION_HEADER,
				HeaderConstants.PRE_AUTH_AUTHORIZATION_HEADER_VALUE_PREFIX + "SpoofedToken");

		for (Enumeration<String> e = proxyRequest.getHeaderNames(); e.hasMoreElements();) {
			String headerName = e.nextElement();
			Assert.assertFalse(headerName.equals(HeaderConstants.PREAUTH_AUTHORIZATION_HEADER));
		}

		String value = proxyRequest.getHeader(HeaderConstants.PREAUTH_AUTHORIZATION_HEADER);
		Assert.assertNull(value);

		Enumeration<String> values = proxyRequest.getHeaders(HeaderConstants.PREAUTH_AUTHORIZATION_HEADER);
		Assert.assertFalse(values.hasMoreElements());
	}
}
