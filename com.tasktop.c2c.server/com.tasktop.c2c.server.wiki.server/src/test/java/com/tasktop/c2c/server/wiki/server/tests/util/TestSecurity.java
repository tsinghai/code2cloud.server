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
package com.tasktop.c2c.server.wiki.server.tests.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tasktop.c2c.server.auth.service.AuthUtils;
import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.wiki.domain.Person;
import com.tasktop.c2c.server.wiki.server.tests.service.TestAuthenticationServiceBean;

@Ignore
public class TestSecurity {

	public static void login(Person profile) {
		AuthenticationToken token = createToken(profile);
		login(token);
	}

	public static AuthenticationToken createToken(Person profile) {
		List<String> roles = new ArrayList<String>();
		// add the default role
		roles.add(Role.User);
		AuthenticationToken token = new AuthenticationToken();
		token.setAuthorities(roles);
		token.setExpiry(new Date(System.currentTimeMillis() + 100000L));
		token.setIssued(new Date());
		token.setFirstName(profile.getName().split("\\s")[0]);
		token.setLastName(profile.getName().split("\\s")[1]);
		token.setKey(UUID.randomUUID().toString());
		token.setUsername(profile.getLoginName());
		if (token.getUsername() == null) {
			token.setUsername(profile.getLoginName());
		}
		return token;
	}

	public static void login(AuthenticationToken token) {
		List<GrantedAuthority> authorities = AuthUtils.toGrantedAuthorities(token.getAuthorities());
		AuthenticationServiceUser user = new AuthenticationServiceUser(token.getUsername(),
				TestAuthenticationServiceBean.MAGIC_PASSWORD, token, authorities);
		Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
