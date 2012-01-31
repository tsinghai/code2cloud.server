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
package com.tasktop.c2c.server.hudson.plugin.auth;

import hudson.security.AccessDeniedException2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

/**
 * integrates with Acegi Security
 * 
 * @author David Green
 * 
 */
public class AcegiSecurityFilter implements Filter {

	private static class AuthenticationBridge implements org.acegisecurity.Authentication {

		private static final long serialVersionUID = 1L;

		private final Authentication delegate;
		private GrantedAuthority[] authorities;

		public AuthenticationBridge(Authentication springAuth) {
			this.delegate = springAuth;
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(springAuth.getAuthorities().size());
			for (org.springframework.security.core.GrantedAuthority authority : springAuth.getAuthorities()) {
				authorities.add(new GrantedAuthorityImpl(authority.getAuthority()));
			}
			this.authorities = authorities.toArray(new GrantedAuthority[authorities.size()]);
		}

		public String getName() {
			return delegate.getName();
		}

		public GrantedAuthority[] getAuthorities() {
			return authorities;
		}

		public Object getCredentials() {
			return delegate.getCredentials();
		}

		public Object getDetails() {
			return delegate.getDetails();
		}

		public Object getPrincipal() {
			return delegate.getPrincipal();
		}

		public boolean isAuthenticated() {
			return delegate.isAuthenticated();
		}

		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			delegate.setAuthenticated(isAuthenticated);
		}

	}

	public void init(FilterConfig arg0) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		try {
			Authentication springAuth = org.springframework.security.core.context.SecurityContextHolder.getContext()
					.getAuthentication();
			if (springAuth != null) {
				SecurityContextHolder.getContext().setAuthentication(bridge(springAuth));
			}
			chain.doFilter(request, response);
		} catch (AccessDeniedException2 e) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"" + "Code2Cloud" + "\"");
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	private org.acegisecurity.Authentication bridge(Authentication springAuth) {
		return new AuthenticationBridge(springAuth);
	}

}
