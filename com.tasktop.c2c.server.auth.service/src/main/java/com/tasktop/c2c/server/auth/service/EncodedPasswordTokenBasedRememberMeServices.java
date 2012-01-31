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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

public class EncodedPasswordTokenBasedRememberMeServices extends TokenBasedRememberMeServices {

	@Autowired
	PasswordEncoder passwordEncoder;

	/**
	 * @see org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices#retrievePassword(org.springframework.security.core.Authentication)
	 */
	@Override
	protected String retrievePassword(Authentication authentication) {
		String password = super.retrievePassword(authentication);
		// Encode the password we retrieve as we will only ever be able to load a encoded password in the future.
		return passwordEncoder.encodePassword(password, null);
	}
}
