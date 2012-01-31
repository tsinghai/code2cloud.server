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
package com.tasktop.c2c.server.ssh.server;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.auth.service.AuthenticationService;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.domain.Role;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
@Component
@Qualifier("sshPasswordAuthenticator")
public class SshPasswordAuthenticator implements PasswordAuthenticator {

	@Autowired
	private AuthenticationService authenticationService;

	@Override
	public boolean authenticate(String username, String password, ServerSession session) {
		try {
			AuthenticationToken token = authenticationService.authenticate(username, password);
			if (token.getAuthorities().contains(Role.UserWithPendingAgreements)) {
				return false;
			}
			session.setAttribute(Constants.SESSION_KEY_AUTHENTICATION_TOKEN, token);
			return true;
		} catch (AuthenticationException e) {
			return false;
		}
	}

}
