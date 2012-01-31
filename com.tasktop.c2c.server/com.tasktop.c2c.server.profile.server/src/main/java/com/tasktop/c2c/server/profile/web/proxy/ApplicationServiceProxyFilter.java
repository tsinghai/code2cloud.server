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
package com.tasktop.c2c.server.profile.web.proxy;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.service.ProjectServiceService;

@Component
@Qualifier("applicationServiceProxyFilter")
public class ApplicationServiceProxyFilter extends GenericFilterBean {

	private static final String PREFIX = ApplicationServiceProxyFilter.class.getSimpleName();
	static final String ATTR_APPLICATION_SERVICE = PREFIX + "#ApplicationService";
	static final String ATTR_APPLICATION_SERVICE_URI = PREFIX + "#URI";

	private Pattern pathPattern = Pattern.compile("/([^/]+)(/.+)");

	@Autowired
	private ProjectServiceService projectServiceService;

	@Override
	public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) srequest;
		HttpServletResponse response = (HttpServletResponse) sresponse;

		String pathInfo = request.getPathInfo();
		if (pathInfo != null) {
			Matcher matcher = pathPattern.matcher(pathInfo);
			if (matcher.matches()) {
				String projectIdentifier = matcher.group(1);
				if (!projectIdentifier.equals(TenancyContextHolder.getContext().getTenant().getIdentity())) {
					throw new IllegalStateException("Tenancy context not set correctly");
				}
				String uri = matcher.group(2);
				ProjectService service;
				try {
					service = projectServiceService.findServiceByUri(projectIdentifier, uri);
					if (service != null) {
						if (service.getServiceHost() == null || !service.getServiceHost().isAvailable()
								|| service.getServiceHost().getInternalNetworkAddress() == null) {
							response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
							return;
						}
						srequest.setAttribute(ATTR_APPLICATION_SERVICE, service);
						srequest.setAttribute(ATTR_APPLICATION_SERVICE_URI, uri);

						chain.doFilter(srequest, sresponse);
						return;
					}
				} catch (EntityNotFoundException e) {
					// expected, ignore
				}
			}
		}

		// default response is 404 not found: if we reach here then we can't
		// proxy the request
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
