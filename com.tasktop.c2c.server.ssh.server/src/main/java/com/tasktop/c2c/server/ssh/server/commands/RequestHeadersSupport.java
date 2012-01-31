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
package com.tasktop.c2c.server.ssh.server.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tasktop.c2c.server.auth.service.proxy.RequestHeaders;


/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class RequestHeadersSupport implements RequestHeaders {

	private Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();

	public void addHeader(String name, String value) {
		List<String> values = requestHeaders.get(name);
		if (values == null) {
			values = new ArrayList<String>(2);
			requestHeaders.put(name, values);
		}
		values.add(value);
	}

	public Map<String, List<String>> getRequestHeaders() {
		return requestHeaders;
	}

}
