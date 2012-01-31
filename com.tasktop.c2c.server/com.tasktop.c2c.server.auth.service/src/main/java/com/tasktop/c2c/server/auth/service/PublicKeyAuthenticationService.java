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

import java.security.PublicKey;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.logging.NoLog;


public interface PublicKeyAuthenticationService {

	/**
	 * authenticate using username and PublicKey credentials
	 * 
	 * @param username
	 *            the username that uniquely identifies a user
	 * @param publicKey
	 *            the public key credentials
	 * @return an authentication token
	 * @throws AuthenticationException
	 *             if the username and/or public key are not recognized
	 */
	public AuthenticationToken authenticate(String username, @NoLog PublicKey publicKey) throws AuthenticationException;
}
