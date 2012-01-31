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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tasktop.c2c.server.common.service.http.MultiUserClientHttpRequestFactory;


public class MultiUserClientHttpRequestFactoryTest {

	private MultiUserClientHttpRequestFactory requestFactory;

	@Before
	public void before() {
		requestFactory = new MultiUserClientHttpRequestFactory();
		requestFactory.setConnectionManager(new SimpleHttpConnectionManager());
	}

	@After
	public void after() throws Exception {
		if (requestFactory != null) {
			requestFactory.destroy();
		}
	}

	@Test
	public void testMultipleUsers() {
		requestFactory.setThreshold(3);

		CommonsClientHttpRequestFactory fooFactory = requestFactory.computeRequestFactory("foo");
		assertNotNull(fooFactory);
		CommonsClientHttpRequestFactory fooFactory2 = requestFactory.computeRequestFactory("foo");
		assertNotNull(fooFactory2);
		assertSame(fooFactory, fooFactory2);

		for (int x = 0; x < requestFactory.getThreshold(); ++x) {
			CommonsClientHttpRequestFactory factory = requestFactory.computeRequestFactory("user" + x);
			assertNotNull(factory);
			CommonsClientHttpRequestFactory factory2 = requestFactory.computeRequestFactory("user" + x);
			assertSame(factory, factory2);
		}
		assertNotSame(fooFactory, requestFactory.computeRequestFactory("foo"));
	}

	@Test
	public void testAuthCredentialsChanged() throws URISyntaxException, IOException {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("foo", "123"));

		URI uri = new URI("http://localhost/test");

		ClientHttpRequest request = requestFactory.createRequest(uri, HttpMethod.GET);
		HttpClient client = getHttpClient(request);
		assertNotNull(client);
		client.getState().addCookie(new Cookie("test.com", "sessionid", "123"));
		assertEquals(1, client.getState().getCookies().length);

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("foo", "1234"));
		ClientHttpRequest request2 = requestFactory.createRequest(uri, HttpMethod.GET);
		HttpClient client2 = getHttpClient(request);

		assertSame(client, client2);
		assertEquals(0, client.getState().getCookies().length);
	}

	private HttpClient getHttpClient(ClientHttpRequest request) {
		Class<?> clazz = request.getClass();
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				if (HttpClient.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					try {
						return (HttpClient) field.get(request);
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		throw new IllegalStateException();
	}

}
