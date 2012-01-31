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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

// REVIEW : I don't think we should be extending APAPF here. That is not really what we are doing, we 
// are handling everything ourself and not using the base class as its designed.
public class AuthenticationRoleProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

	private UserDetailsService userDetailsService;
	private String rememberMeKey;

	@Required
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Required
	public void setRememberMeKey(String rememberMeKey) {
		this.rememberMeKey = rememberMeKey;
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

		// Pull out our credentials and recalculate the associated roles for it.
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			return null;
		}

		// Push in the user's updated roles now.
		if (auth instanceof UsernamePasswordAuthenticationToken) {

			// Pull the latest roles for this user.
			UserDetails userDetails = userDetailsService.loadUserByUsername(auth.getName());

			if (auth.getDetails() instanceof AuthenticationToken) {
				// Dump these updated roles into our token.
				((AuthenticationToken) auth.getDetails()).setAuthorities(userDetails.getAuthorities());
			}

			// Recalculate and update our user roles.
			AuthUtils.insertNewAuthToken(auth.getPrincipal(), auth.getCredentials(),
					AuthUtils.toAuthorityStrings(userDetails.getAuthorities()), auth.getDetails());
		} else if (auth instanceof RememberMeAuthenticationToken) {
			// Collect the current roles for this user.
			UserDetails userDetails = userDetailsService.loadUserByUsername(auth.getName());

			// Construct a new RememberMe token to store this data.
			RememberMeAuthenticationToken newToken = new RememberMeAuthenticationToken(this.rememberMeKey, userDetails,
					userDetails.getAuthorities());

			// Push this into the Spring Security context.
			SecurityContextHolder.getContext().setAuthentication(newToken);
		}

		// No matter what, always return our original principal - that indicates no change to the authentication itself
		// (which is what we want - the user is still the same, it's only their roles which have been updated).
		return auth.getPrincipal();
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		// Dummy return value - this method needs non-null returns.
		return request;
	}
}
