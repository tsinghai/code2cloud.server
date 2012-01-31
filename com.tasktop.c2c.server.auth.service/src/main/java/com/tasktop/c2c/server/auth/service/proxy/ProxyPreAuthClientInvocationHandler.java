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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.auth.service.AuthenticationToken;


public class ProxyPreAuthClientInvocationHandler implements InvocationHandler {

	private AuthenticationToken authenticationToken;
	private Object implementation;

	@SuppressWarnings("unchecked")
	public static <T> T wrapWithAuthenticationToken(T service, AuthenticationToken token) {
		final ProxyPreAuthClientInvocationHandler handler = new ProxyPreAuthClientInvocationHandler(service);
		handler.setAuthenticationToken(token);
		return (T) Proxy.newProxyInstance(service.getClass().getClassLoader(), computeInterfaces(service), handler);
	}

	@SuppressWarnings("unchecked")
	public static <T> T wrap(T service, ProxyPreAuthClientInvocationHandler handler) {
		handler.implementation = service;
		return (T) Proxy.newProxyInstance(service.getClass().getClassLoader(), computeInterfaces(service), handler);
	}

	private static Class<?>[] computeInterfaces(Object o) {
		Class<? extends Object> clazz = o.getClass();
		List<Class<?>> allInterfaces = new ArrayList<Class<?>>();
		while (clazz != Object.class) {
			for (Class<?> c : clazz.getInterfaces()) {
				allInterfaces.add(c);
			}
			clazz = clazz.getSuperclass();
		}
		return allInterfaces.toArray(new Class<?>[allInterfaces.size()]);
	}

	public ProxyPreAuthClientInvocationHandler() {
	}

	public ProxyPreAuthClientInvocationHandler(Object implementation) {
		this.implementation = implementation;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		ProxyClient.setAuthenticationToken(getAuthenticationToken());
		try {
			return method.invoke(implementation, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} finally {
			ProxyClient.setAuthenticationToken(null);
		}
	}

	public AuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(AuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
	}
}
