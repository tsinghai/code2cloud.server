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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.web.HttpRequestHandler;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.auth.service.proxy.AuthenticationTokenSerializer;
import com.tasktop.c2c.server.auth.service.proxy.ProxyHttpServletRequest;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.web.HeaderConstants;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.web.proxy.WebProxy;

/**
 * A service proxy. Must be used in conjunction with {@link ApplicationServiceProxyFilter}.
 * 
 * @author David Green
 * @see ApplicationServiceProxyFilter
 */
@Component
@Qualifier("applicationServiceProxy")
public class ApplicationServiceProxy implements HttpRequestHandler {

	private final Logger LOG = LoggerFactory.getLogger(ApplicationServiceProxy.class.getName());

	private List<WebProxy> proxies;

	@Autowired
	private InternalAuthenticationService internalAuthenticationService;

	private AuthenticationTokenSerializer tokenSerializer = new AuthenticationTokenSerializer();

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		ProjectService service = (ProjectService) request
				.getAttribute(ApplicationServiceProxyFilter.ATTR_APPLICATION_SERVICE);
		String uri = (String) request.getAttribute(ApplicationServiceProxyFilter.ATTR_APPLICATION_SERVICE_URI);

		if (service == null) {
			// default response is 404 not found: if we reach here then we can't
			// proxy the request
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			// Forward our call now.
			proxy(service, uri, request, response);
		}
	}

	private void proxy(ProjectService service, String uri, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		ProxyHttpServletRequest proxyRequest = new ProxyHttpServletRequest(request);
		String targetUrl = constructTargetUrl(service, uri, request);

		AuthenticationServiceUser user = AuthenticationServiceUser.getCurrent();
		AuthenticationToken authenticationToken = null;
		Project project = service.getProjectServiceProfile().getProject();

		if (user != null) {
			authenticationToken = user.getToken();
			if (authenticationToken.getAuthorities().contains(Role.UserWithPendingAgreements)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "You must accept licence agreements first.");
				return;
			}
		}

		// Get our token from the authentication context, and rewrite it to be in specialized form.
		authenticationToken = internalAuthenticationService.specializeAuthenticationToken(authenticationToken,
				project.getIdentifier(), project.getPublic());

		tokenSerializer.serialize(proxyRequest, authenticationToken);

		proxyRequest.addHeader(HeaderConstants.TENANT_HEADER, (String) TenancyContextHolder.getContext().getTenant()
				.getIdentity());

		LOG.info("Proxying service [" + service.getType() + "] to url [" + targetUrl + "]");

		boolean handlerFound = false;
		for (WebProxy webProxy : proxies) {
			if (webProxy.canProxyRequest(targetUrl, proxyRequest)) {
				webProxy.proxyRequest(targetUrl, proxyRequest, response);
				handlerFound = true;
				break;
			}
		}
		if (!handlerFound) {
			throw new IllegalStateException("Cannot find a proxy handler for " + targetUrl);
		}

	}

	private String constructTargetUrl(ProjectService service, String uri, HttpServletRequest request) {
		String queryString = request.getQueryString() == null || request.getQueryString().isEmpty() ? ""
				: ("?" + request.getQueryString());
		return service.computeInternalProxyUri(uri) + queryString;
	}

	public List<WebProxy> getProxies() {
		return proxies;
	}

	public void setProxies(List<WebProxy> proxies) {
		this.proxies = proxies;
	}

}
