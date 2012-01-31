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

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.logging.NoLog;

/**
 * Provides authentication.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 */
public interface AuthenticationService {
	/**
	 * authenticate using username/password credentials
	 * 
	 * @param username
	 *            the username that uniquely identifies a user
	 * @param password
	 *            the password credentials
	 * @return an authentication token
	 * @throws AuthenticationException
	 *             if the username and/or password are not recognized
	 */
	public AuthenticationToken authenticate(String username, @NoLog String password) throws AuthenticationException;
}
