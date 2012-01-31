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
package com.tasktop.c2c.server.ssh.server.tests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.springframework.stereotype.Service;

import com.tasktop.c2c.server.auth.service.AuthenticationService;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.domain.Role;


@Ignore
@Service
public class TestAuthenticationService implements AuthenticationService {

	private static Map<String, AuthenticationToken> authorizations = new HashMap<String, AuthenticationToken>();
	static {
		AuthenticationToken token = new AuthenticationToken();
		token.setUsername("test");
		token.setAuthorities(Arrays.asList(Role.User));
		authorizations.put("test:test", token);
	}

	public static void clearAuthorizations() {
		authorizations.clear();
	}

	public static void authorize(String username, String password, String[] roles) {
		authorizations.put(username + ":" + password, createAuthenticationToken(username, roles));
	}

	private static AuthenticationToken createAuthenticationToken(String username, String[] roles) {
		AuthenticationToken token = new AuthenticationToken();
		token.setAuthorities(Arrays.asList(roles));
		token.setUsername(username);
		return token;
	}

	@Override
	public AuthenticationToken authenticate(String username, String password) throws AuthenticationException {
		return authorizations.get(username + ":" + password);
	}

}
