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
package com.tasktop.c2c.server.common.web.server;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter which turns off/on caching for appropriate gwt resources.
 * 
 * 
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class GwtCacheFilter extends GenericFilterBean {

	private Pattern nocachePattern = Pattern.compile(".*\\.nocache\\..*");
	private Pattern cachePattern = Pattern.compile(".*\\.cache\\..*");
	private Long expiredAgo = 24l * 60l * 60l * 1000l;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String path = httpRequest.getRequestURI();
		if (nocachePattern.matcher(path).matches()) {
			long now = System.currentTimeMillis();
			httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setDateHeader("Date", now);
			httpResponse.setDateHeader("Expires", now - expiredAgo);
		} else if (cachePattern.matcher(path).matches()) {
			long now = System.currentTimeMillis();
			httpResponse.setHeader("Cache-control", "public");
			httpResponse.setDateHeader("Date", now);
			httpResponse.setDateHeader("Expires", now + expiredAgo);
		}

		chain.doFilter(request, response);
	}

}
