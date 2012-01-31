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
package com.tasktop.c2c.server.profile.web.proxy;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class AuthenticationProviderNotApplicableException extends AuthenticationException {

	public AuthenticationProviderNotApplicableException(String msg, Object extraInformation) {
		super(msg, extraInformation);
	}

	public AuthenticationProviderNotApplicableException(String msg, Throwable t) {
		super(msg, t);
	}

	public AuthenticationProviderNotApplicableException(String msg) {
		super(msg);
	}

}
