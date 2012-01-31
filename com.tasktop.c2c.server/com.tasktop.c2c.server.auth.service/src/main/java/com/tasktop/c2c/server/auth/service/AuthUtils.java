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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public final class AuthUtils {

	// No instantiation of this class
	private AuthUtils() {

	}

	// FIXME this token will not work when used with AbstractPreAuthServiceProvider.
	public static void insertSystemAuthToken(String role) {
		// Create our standard system token now.
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("SYSTEM", "",
				Collections.singletonList(new SimpleGrantedAuthority(role)));

		// Push it into the Spring Security context.
		SecurityContextHolder.getContext().setAuthentication(token);
	}

	// Ensure that auth tokens are inserted and handled in a consistent manner.
	public static void insertNewAuthToken(Object principal, Object credentials, List<String> authorities, Object details) {

		// Calculate our set of authorities
		Collection<GrantedAuthority> authList = toGrantedAuthorities(authorities);

		// Create a new authToken for this information
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
				credentials, authList);

		// Ensure that our token is saved as Details
		authentication.setDetails(details);

		// Push this into the Spring Security context.
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public static List<GrantedAuthority> toGrantedAuthorities(Collection<String> roles) {
		ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		if (roles != null) {
			for (String roleName : roles) {
				authorities.add(new SimpleGrantedAuthority(roleName));
			}
		}

		return authorities;
	}

	public static List<String> toAuthorityStrings(Collection<? extends GrantedAuthority> roles) {
		ArrayList<String> authorities = new ArrayList<String>();
		if (roles != null) {
			for (GrantedAuthority role : roles) {
				authorities.add(role.getAuthority());
			}
		}
		return authorities;
	}
}
