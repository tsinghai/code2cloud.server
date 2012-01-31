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
package com.tasktop.c2c.server.internal.auth.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.common.service.domain.Role;

@Service("internalAuthenticationService")
public class InternalAuthenticationServiceBean implements InternalAuthenticationService {

	@Override
	public String toCompoundRole(String roleName, String projectIdentifier) {
		if (roleName == null || roleName.contains("/")) {
			throw new IllegalArgumentException();
		}
		return String.format("%s/%s", roleName, projectIdentifier);
	}

	@Override
	public String fromCompoundRole(String compoundRole, String projectIdentifier) {
		List<String> retList = this.fromCompoundRole(Collections.singletonList(compoundRole), projectIdentifier);
		String retStr = null;

		if (retList.size() > 0) {
			retStr = retList.get(0);
		}

		return retStr;
	}

	@Override
	public List<String> fromCompoundRole(List<String> compoundRoles, String projectIdentifier) {
		Pattern rolePattern = computeRolePattern(projectIdentifier);
		List<String> roleNames = new ArrayList<String>(compoundRoles.size());
		for (String authority : compoundRoles) {
			Matcher matcher = rolePattern.matcher(authority);
			if (matcher.matches()) {
				String role = matcher.group(1);
				roleNames.add(role);
			}
		}
		return roleNames;
	}

	protected Pattern computeRolePattern(String projectIdentifier) {
		return Pattern.compile("([^/]+)/" + Pattern.quote(projectIdentifier));
	}

	@Override
	public AuthenticationToken specializeAuthenticationToken(AuthenticationToken originalToken,
			String projectIdentifier, boolean projectIsPublic) {

		AuthenticationToken token = originalToken;

		// If we have a public project, we want to pass along a token which indicates that the observer role is present.
		if (token == null) {
			// This is an anonymous user, so create a new token for them.
			token = new AuthenticationToken();

			// Plug in our anonymous role, since this is an unauthenticated user.
			token.getAuthorities().add(Role.Anonymous);
		} else {
			token = originalToken.clone();
			token.setAuthorities(fromCompoundRole(token.getAuthorities(), projectIdentifier));

			// we already had an authentication token - if we have a public project, add in our community role now.
			if (projectIsPublic) {
				token.getAuthorities().add(Role.Community);
			}
		}

		if (projectIsPublic) {
			// Plug in our observer role since we have a public project.
			token.getAuthorities().add(Role.Observer);
		}

		return token;
	}

	private AuthenticationToken createSystemAuthenticationToken(String projectIdentifier) {
		AuthenticationToken token = new AuthenticationToken();
		token.setUsername("SYSTEM");
		token.setKey(UUID.randomUUID().toString());
		token.getAuthorities().add(Role.System);
		token.getAuthorities().add(Role.User);
		if (projectIdentifier != null) {
			token.getAuthorities().add(toCompoundRole(Role.System, projectIdentifier));
			token.getAuthorities().add(toCompoundRole(Role.User, projectIdentifier));
		}
		return token;
	}

	@Override
	public void assumeSystemIdentity(String projectIdentifier) {
		AuthenticationToken authenticationToken = createSystemAuthenticationToken(projectIdentifier);
		AuthenticationServiceUser user = AuthenticationServiceUser.fromAuthenticationToken(authenticationToken);
		PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(user,
				authenticationToken, user.getAuthorities());
		authentication.setAuthenticated(true);
		SecurityContextHolder.createEmptyContext();
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
