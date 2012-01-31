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
package com.tasktop.c2c.server.auth.service.proxy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.domain.Role;


public class AuthenticationTokenUserDetailsService implements AuthenticationUserDetailsService {

	public AuthenticationTokenUserDetailsService() {
	}

	@Override
	public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
		Object principal = token.getPrincipal();
		if (principal instanceof AuthenticationToken) {
			AuthenticationToken authenticationToken = (AuthenticationToken) principal;

			if (authenticationToken.getUsername() == null
					&& authenticationToken.getAuthorities().contains(Role.Anonymous)) {
				// We have an anonymous user - plug in our role as our username so that Spring's User object doesn't
				// throw an exception (it hates empty usernames).
				authenticationToken.setUsername(Role.Anonymous);
				authenticationToken.setKey(Role.Anonymous);
			}

			return AuthenticationServiceUser.fromAuthenticationToken(authenticationToken);
		}
		throw new UsernameNotFoundException(principal.toString());
	}
}
