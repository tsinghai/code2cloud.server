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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExcludeHeaderFilter extends HeaderFilter {

	private Set<String> excludedRequestHeaders = new CaseInsensitiveSet();
	private Set<String> excludedResponseHeaders = new CaseInsensitiveSet();

	public String processRequestHeader(String headerName, String headerValue) {
		if (excludedRequestHeaders.contains(headerName)) {
			return null;
		}
		return super.processRequestHeader(headerName, headerValue);
	}

	public String processResponseHeader(String headerName, String headerValue) {
		if (excludedResponseHeaders.contains(headerName)) {
			return null;
		}
		return super.processResponseHeader(headerName, headerValue);
	}

	public Set<String> getExcludedRequestHeaders() {
		return excludedRequestHeaders;
	}

	public void setExcludedRequestHeaders(Set<String> excludedRequestHeaders) {
		this.excludedRequestHeaders = new CaseInsensitiveSet(excludedRequestHeaders);
	}

	public Set<String> getExcludedResponseHeaders() {
		return excludedResponseHeaders;
	}

	public void setExcludedResponseHeaders(Set<String> excludedResponseHeaders) {
		this.excludedResponseHeaders = new CaseInsensitiveSet(excludedResponseHeaders);
	}

	private static class CaseInsensitiveSet extends HashSet<String> {

		private CaseInsensitiveSet() {
			super();
			// TODO Auto-generated constructor stub
		}

		private CaseInsensitiveSet(Collection<? extends String> c) {
			super(c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean add(String e) {
			return super.add(e.toLowerCase());
		}

		@Override
		public boolean contains(Object o) {
			return super.contains(o == null ? null : o.toString().toLowerCase());
		}

		@Override
		public boolean remove(Object o) {
			return super.remove(o == null ? null : o.toString().toLowerCase());
		}
	}
}
