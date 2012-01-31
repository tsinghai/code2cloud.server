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
package com.tasktop.c2c.server.wiki.server.tests.service;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tasktop.c2c.server.auth.service.AbstractAuthenticationServiceBean;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.domain.Role;

@Ignore
@Qualifier("main")
@Service("authenticationService")
public class TestAuthenticationServiceBean extends AbstractAuthenticationServiceBean<Object> {

	public static final String MAGIC_PASSWORD = "test123";

	@Override
	protected Object validateCredentials(String username, String password) {
		if (MAGIC_PASSWORD.equals(password)) {
			return new Object();
		}
		return null;
	}

	@Override
	protected void configureToken(Object data, AuthenticationToken token) {
		token.setFirstName("First");
		token.setLastName("Last");
	}

	@Override
	protected void addAuthorities(Object data, AuthenticationToken token) {
		super.addAuthorities(data, token);
		token.getAuthorities().add(Role.User + "/1");
	}
}
