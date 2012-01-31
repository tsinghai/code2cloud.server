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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.proxy.AuthenticationTokenSerializer;
import com.tasktop.c2c.server.auth.service.proxy.ProxyHttpServletRequest;

@RunWith(BlockJUnit4ClassRunner.class)
public class AuthenticationTokenSerializerTest {

	private static final String HTTP_HEADER_NAME_AUTHORIZATION = "Authorization";
	private MockHttpServletRequest mockRequest;
	private ProxyHttpServletRequest proxyRequest;
	private AuthenticationToken token;
	private AuthenticationTokenSerializer serializer;

	@Before
	public void before() {
		mockRequest = new MockHttpServletRequest();
		proxyRequest = new ProxyHttpServletRequest(mockRequest);
		token = new AuthenticationToken();
		token.setFirstName("Joe");
		token.setLastName("Tester");
		token.setUsername("jtester");
		token.setIssued(new Date());
		token.setKey(UUID.randomUUID().toString());
		token.setExpiry(new Date(System.currentTimeMillis() + 1000000L));
		for (int x = 0; x < 10; ++x) {
			token.getAuthorities().add("A_ROLE_NAME_THAT_IS_RELATIVELY_LONG_" + x);
		}
		serializer = new AuthenticationTokenSerializer();
	}

	@Test
	public void testSerialization() {
		serializer.serialize(proxyRequest, token);
		String authorization = proxyRequest.getHeader(HTTP_HEADER_NAME_AUTHORIZATION);
		assertNotNull(authorization);
		assertTrue(authorization.startsWith("almtoken "));
		System.out.println("token length: " + authorization.length());
	}

	@Test
	public void testDeserialization() {
		serializer.serialize(proxyRequest, token);
		AuthenticationToken receivedToken = serializer.deserialize(proxyRequest);

		assertEquals(token, receivedToken);
		assertEquals(token.getFirstName(), receivedToken.getFirstName());
		assertEquals(token.getLastName(), receivedToken.getLastName());
		assertEquals(token.getUsername(), receivedToken.getUsername());
		assertEquals(token.getExpiry(), receivedToken.getExpiry());
		assertEquals(token.getIssued(), receivedToken.getIssued());
		assertEquals(token.getKey(), receivedToken.getKey());
		assertEquals(token.getAuthorities(), receivedToken.getAuthorities());
	}

	@Test
	public void testDeserializationNoToken() {
		assertNull(serializer.deserialize(mockRequest));
	}
}
