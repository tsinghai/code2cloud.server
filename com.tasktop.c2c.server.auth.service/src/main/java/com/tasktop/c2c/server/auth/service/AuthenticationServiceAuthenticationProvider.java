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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.ResourceAccessException;

public class AuthenticationServiceAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	protected static final String MSG_BAD_CREDENTIALS = "AbstractUserDetailsAuthenticationProvider.badCredentials";
	private static final String MSG_TOKEN_EXPIRED = "AuthenticationServiceServiceProvider.tokenExpired";
	private AuthenticationService authenticationService;

	public AuthenticationServiceAuthenticationProvider() {
	}

	@Autowired
	@Qualifier("main")
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// sanity
		if (!(userDetails instanceof AuthenticationServiceUser)) {
			throw new IllegalStateException();
		}
		// don't allow empty passwords
		if (userDetails.getPassword() == null || userDetails.getPassword().length() == 0) {
			throw new InsufficientAuthenticationException(messages.getMessage(MSG_BAD_CREDENTIALS, "Bad Credentials"));
		}
		AuthenticationServiceUser user = (AuthenticationServiceUser) userDetails;
		// enforce token expiry policy
		AuthenticationToken token = user.getToken();
		if (token.getExpiry() == null || token.getExpiry().getTime() < System.currentTimeMillis()) {
			throw new InsufficientAuthenticationException(messages.getMessage(MSG_TOKEN_EXPIRED, "Token Expired"));
		}
		// verify password
		String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();
		if (!password.equals(userDetails.getPassword())) {
			throw new BadCredentialsException(messages.getMessage(MSG_BAD_CREDENTIALS, "Bad Credentials"));
		}
	}

	private String decodeUserName(String username) {
		try {
			return URLDecoder.decode(username, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected AuthenticationServiceUser retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();
		username = decodeUserName(username);
		AuthenticationToken token;
		try {
			token = authenticationService.authenticate(username, password);
		} catch (com.tasktop.c2c.server.common.service.AuthenticationException e) {
			throw new BadCredentialsException(messages.getMessage(MSG_BAD_CREDENTIALS, "Bad Credentials"), e);
		} catch (ResourceAccessException e) {
			throw new AuthenticationServiceException("Service Temporarily Unavailable", e);
		} catch (Throwable t) {
			throw new AuthenticationServiceException(t.getMessage(), t);
		}
		// the service won't provide the password however we know that it's good if we reach this point
		// (the service verifies that the password is ok)
		AuthenticationServiceUser user = new AuthenticationServiceUser(username, password, token,
				AuthUtils.toGrantedAuthorities(token.getAuthorities()));
		if (user.getAuthorities().isEmpty()) {
			throw new BadCredentialsException(messages.getMessage(MSG_BAD_CREDENTIALS, "Bad Credentials"));
		}
		return user;
	}
}
