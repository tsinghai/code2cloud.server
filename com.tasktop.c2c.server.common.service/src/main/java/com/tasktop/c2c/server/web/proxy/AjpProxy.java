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
package com.tasktop.c2c.server.web.proxy;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import com.tasktop.c2c.server.web.proxy.ajp.AjpPoolableConnectionFactory;
import com.tasktop.c2c.server.web.proxy.ajp.AjpProtocol;


public class AjpProxy extends WebProxy {

	private static final Pattern targetUrlPattern = Pattern.compile("ajp://([^:/]+)(?::(\\d+))?(/.*)?");

	private KeyedObjectPool socketPool = new GenericKeyedObjectPool(new AjpPoolableConnectionFactory());

	@Override
	public boolean canProxyRequest(String targetUrl, HttpServletRequest request) {
		return targetUrlPattern.matcher(targetUrl).matches();
	}

	@Override
	protected void proxy(String targetUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
		Matcher matcher = targetUrlPattern.matcher(targetUrl);
		if (!matcher.matches()) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IllegalStateException();
		}
		String uri = matcher.group(3);
		int query = uri.indexOf('?');
		if (query != -1) {
			uri = uri.substring(0, query);
		}
		final String requestUri = uri;
		request = new HttpServletRequestWrapper(request) {
			public String getRequestURI() {
				return requestUri;
			}
		};

		AjpProtocol ajpProtocol = new AjpProtocol();
		ajpProtocol.setSocketPool(socketPool);
		ajpProtocol.setProxyHost(matcher.group(1));
		String portPart = matcher.group(2);
		ajpProtocol.setProxyPort(portPart == null ? 8009 : Integer.parseInt(portPart));
		ajpProtocol.setHeaderFilter(headerFilter);

		ajpProtocol.forward(request, response);
	}

	public KeyedObjectPool getSocketPool() {
		return socketPool;
	}

	public void setSocketPool(KeyedObjectPool socketPool) {
		this.socketPool = socketPool;
	}

}
