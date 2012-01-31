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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.tasktop.c2c.server.common.service.web.HeaderConstants;


/**
 * A request that is to be proxied to the internal services. Allows headers to be added to the request and as well
 * filters out illegal headers from the original request.
 * 
 * 
 */
public class ProxyHttpServletRequest extends HttpServletRequestWrapper implements RequestHeaders {

	private Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();

	public ProxyHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	public void addHeader(String name, String value) {
		List<String> values = requestHeaders.get(name);
		if (values == null) {
			values = new ArrayList<String>(2);
			requestHeaders.put(name, values);
		}
		values.add(value);
	}

	@Override
	public String getHeader(String name) {
		List<String> headers = requestHeaders.get(name);
		if (headers == null) {
			String value = super.getHeader(name);
			if (HeaderConstants.isAlmInternalHeader(name, value)) {
				return null;
			} else {
				return value;
			}
		}
		return headers.isEmpty() ? null : headers.get(0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaderNames() {
		final Set<String> headerNames = new HashSet<String>();
		headerNames.addAll(requestHeaders.keySet());
		for (Enumeration s = super.getHeaderNames(); s.hasMoreElements();) {
			String headerName = (String) s.nextElement();
			if (this.getHeaders(headerName).hasMoreElements()) { // This does the header check
				headerNames.add(headerName);
			}
		}
		return new Enumeration<Object>() {
			Iterator<String> headersIt = headerNames.iterator();

			@Override
			public boolean hasMoreElements() {
				return headersIt.hasNext();
			}

			@Override
			public Object nextElement() {
				return headersIt.next();
			}
		};
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaders(String name) {
		List<String> headers = requestHeaders.get(name);
		if (headers == null) {
			headers = new ArrayList<String>();
			for (Enumeration s = super.getHeaders(name); s.hasMoreElements();) {
				String value = (String) s.nextElement();
				if (!HeaderConstants.isAlmInternalHeader(name, value)) {
					headers.add(value);
				}
			}
		}

		final Iterator<String> values = headers.iterator();
		return new Enumeration<Object>() {

			@Override
			public boolean hasMoreElements() {
				return values.hasNext();
			}

			@Override
			public Object nextElement() {
				return values.next();
			}
		};
	}
}
