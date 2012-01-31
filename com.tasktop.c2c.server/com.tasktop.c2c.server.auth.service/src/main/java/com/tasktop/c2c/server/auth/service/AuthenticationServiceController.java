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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;


@Controller
@Qualifier("webservice")
public class AuthenticationServiceController extends AbstractRestService implements AuthenticationService {

	@Qualifier("main")
	@Autowired
	private AuthenticationService delegate;

	@Override
	@RequestMapping(value = "/authenticate", method = RequestMethod.GET)
	public AuthenticationToken authenticate(//
			@RequestParam("username") String username, @RequestParam("password") String password)
			throws AuthenticationException {
		return delegate.authenticate(username, password);
	}

}
