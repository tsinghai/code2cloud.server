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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;


@Service
@Qualifier("webservice-client")
public class AuthenticationServiceClient extends AbstractRestServiceClient implements AuthenticationService {

	@SuppressWarnings("unused")
	private static class ServiceCallResult {
		private AuthenticationToken authenticationToken;

		public AuthenticationToken getAuthenticationToken() {
			return authenticationToken;
		}

		public void setAuthenticationToken(AuthenticationToken authenticationToken) {
			this.authenticationToken = authenticationToken;
		}
	}

	@Override
	public AuthenticationToken authenticate(String username, String password) throws AuthenticationException {
		try {
			Map<String, String> variables = new HashMap<String, String>();
			variables.put("username", username);
			variables.put("password", password);
			ServiceCallResult result = template.getForObject(
					computeUrl("authenticate?username={username}&password={password}"), ServiceCallResult.class,
					variables);
			if (result != null && result.getAuthenticationToken() != null) {
				return result.getAuthenticationToken();
			}
		} catch (WrappedCheckedException e) {
			convertAuthenticationException(e);
			throw e;
		}
		throw new IllegalStateException("Unexpected result");
	}

	@Autowired(required = false)
	// Just because this is getting instantiated where it is not needed.
	@Qualifier("minimal")
	@Override
	public void setRestTemplate(RestTemplate template) {
		super.setRestTemplate(template);
	}

}
