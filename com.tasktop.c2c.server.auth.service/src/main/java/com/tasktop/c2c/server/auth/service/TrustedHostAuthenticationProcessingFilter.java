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
package com.tasktop.c2c.server.auth.service;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class TrustedHostAuthenticationProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

	public static final String SYSTEM_USERNAME = "System";

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			return null;
		}
		AuthenticationServiceUser principle = new AuthenticationServiceUser(SYSTEM_USERNAME, request.getRemoteAddr(),
				null, Collections.EMPTY_LIST);
		return principle;
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return request;
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) {
		SecurityContextHolder.clearContext();
		// We override because we do not want to send any special response code here. This ensures that the basic auth
		// challenge response mechanism will still work
	}
}
