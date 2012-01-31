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
package com.tasktop.c2c.server.profile.service.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tasktop.c2c.server.auth.service.AuthenticationService;
import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.auth.service.proxy.ProxyPreAuthClientInvocationHandler;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.service.ProfileService;


public abstract class AbstractPreAuthServiceProvider<T> extends AbstractServiceProvider<T> {

	protected AbstractPreAuthServiceProvider(String serviceUri) {
		super(serviceUri);
	}

	@Autowired
	private InternalAuthenticationService internalAuthenticationService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ProfileService profileService;

	private AuthenticationToken systemAuthToken;

	@Override
	public T getService(String projectIdentifier) {
		T service = super.getService(projectIdentifier);
		return ProxyPreAuthClientInvocationHandler.wrapWithAuthenticationToken(service,
				computeAuthenticationToken(projectIdentifier));
	}

	private AuthenticationToken computeAuthenticationToken(String projectIdentifier) {
		if (systemAuthToken != null) {
			return systemAuthToken;
		}
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationToken authenticationToken = null;
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof AuthenticationServiceUser) {
				authenticationToken = ((AuthenticationServiceUser) principal).getToken();
			} else if (authentication instanceof UsernamePasswordAuthenticationToken) {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
				String username = token.getName();
				String password = token.getCredentials() == null ? "" : token.getCredentials().toString();

				try {
					authenticationToken = authenticationService.authenticate(username, password);
				} catch (AuthenticationException e) {
					throw new InsufficientPermissionsException(e);
				}
			}
		}
		try {
			Project project = profileService.getProjectByIdentifier(projectIdentifier);

			return internalAuthenticationService.specializeAuthenticationToken(authenticationToken,
					project.getIdentifier(), project.getPublic());

		} catch (EntityNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setSystemAuthToken(AuthenticationToken systemAuthToken) {
		this.systemAuthToken = systemAuthToken;
	}
}
