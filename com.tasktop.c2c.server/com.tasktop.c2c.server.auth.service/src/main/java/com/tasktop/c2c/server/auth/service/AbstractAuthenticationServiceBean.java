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

import java.util.Date;
import java.util.UUID;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.domain.Role;

/**
 * An abstract implementation of and {@link AuthenticationService}.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 */
public abstract class AbstractAuthenticationServiceBean<UserData> implements AuthenticationService {

	private long expiresInMillis = 1000L * 60L * 60L;

	@Override
	public final AuthenticationToken authenticate(String username, String password) throws AuthenticationException {
		UserData data = validateCredentials(username, password);
		if (data == null) {
			throw new AuthenticationException();
		}
		return createAuthenticationToken(username, data);
	}

	protected AuthenticationToken createAuthenticationToken(String username, UserData data) {
		AuthenticationToken token = new AuthenticationToken();
		token.setIssued(new Date());
		token.setUsername(username);
		token.setKey(UUID.randomUUID().toString());
		token.setExpiry(new Date(token.getIssued().getTime() + expiresInMillis));
		configureToken(data, token);
		addAuthorities(data, token);
		return token;
	}

	/**
	 * Configure the token with additional information, such as {@link AuthenticationToken#getFirstName() first name},
	 * and {@link AuthenticationToken#getLastName() last name}. The default implementation does nothing.
	 */
	protected void configureToken(UserData data, AuthenticationToken token) {

	}

	/**
	 * Add authorities to the given token. Authorities are analogous to roles. The default behaviour is to add the
	 * authority <code>"ROLE_USER"</code>. In most cases overriding methods should call
	 * <code>super.addAuthorities(data,token)</code>.
	 * 
	 * @param data
	 *            the implementation-specific user data as provided by {@link #validateCredentials(String, String)}
	 * @param token
	 *            the token to be produced
	 * 
	 * @see AuthenticationToken#getAuthorities()
	 */
	protected void addAuthorities(UserData data, AuthenticationToken token) {
		token.getAuthorities().add(Role.User);
	}

	/**
	 * Validate the provided credentials. Indicate invalid credentials by returning null, otherwise return an
	 * implementation-specific data object.
	 * 
	 * @return an implementation-specific data object, or null if the credentials are invalid.
	 */
	protected abstract UserData validateCredentials(String username, String password) throws AuthenticationException;

	/**
	 * the expiry of authentication tokens in milliseconds.
	 * 
	 * @see AuthenticationToken#getExpiry()
	 */
	public long getExpiresInMillis() {
		return expiresInMillis;
	}

	/**
	 * the expiry of authentication tokens in milliseconds.
	 * 
	 * @see AuthenticationToken#getExpiry()
	 */
	public void setExpiresInMillis(long expiresInMillis) {
		if (expiresInMillis < 1000L) {
			throw new IllegalArgumentException();
		}
		this.expiresInMillis = expiresInMillis;
	}

}
