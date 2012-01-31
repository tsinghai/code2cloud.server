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
package com.tasktop.c2c.server.profile.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.tasktop.c2c.server.auth.service.AbstractAuthenticationServiceBean;
import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.web.HeaderConstants;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHost;
import com.tasktop.c2c.server.profile.web.proxy.AuthenticationProviderNotApplicableException;

// XXX Need to subclass AASB so that I can create auth tokens.
public class TrustedHostAuthenticationProvider extends AbstractAuthenticationServiceBean<AuthenticationServiceUser>
		implements AuthenticationProvider {

	@Autowired
	private com.tasktop.c2c.server.profile.service.ProjectServiceService projectServiceService;
	@Autowired
	private InternalAuthenticationService internalAuthenticationService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof PreAuthenticatedAuthenticationToken)) {
			throw new AuthenticationProviderNotApplicableException("Expcect a PreAutheticatedAuthToken");
		}

		PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;

		HttpServletRequest request = (HttpServletRequest) token.getCredentials();
		WebAuthenticationDetails details = (WebAuthenticationDetails) token.getDetails();
		String remoteAddr = computeTrustedOrigin(details.getRemoteAddress(), request);

		if (remoteAddr == null) {
			throw new AuthenticationCredentialsNotFoundException("Could not locate trusted host");
		}

		List<GrantedAuthority> authorities = getTrustedHostAuthorities(remoteAddr, request);

		if (authorities.isEmpty()) {
			throw new AuthenticationCredentialsNotFoundException("No trusted host credentials");
		}

		AuthenticationServiceUser user = (AuthenticationServiceUser) token.getPrincipal();
		AuthenticationToken authToken;
		try {
			authToken = authenticate(user.getUsername(), user.getPassword());
		} catch (com.tasktop.c2c.server.common.service.AuthenticationException e) {
			throw new AuthenticationServiceException(", e");
		}
		authToken.setAuthorities(convertToStringAuthorities(authorities));
		user = new AuthenticationServiceUser(user.getUsername(), user.getPassword(), authToken, authorities);
		token = new PreAuthenticatedAuthenticationToken(user, token.getCredentials(), authorities);
		token.setAuthenticated(true);

		return token;

	}

	private static final String FORWARD_HEADER = "X-Forwarded-For";

	private String computeTrustedOrigin(String remoteAddress, HttpServletRequest request) {
		// List of forwards, first is most recent.
		List<String> forwards = new ArrayList<String>();
		forwards.add(remoteAddress);
		String forwardedFors = request.getHeader(FORWARD_HEADER);
		if (forwardedFors != null) {
			String[] forwardHosts = forwardedFors.split(",");
			for (int i = forwardHosts.length - 1; i >= 0; i--) {
				forwards.add(forwardHosts[i].trim());
			}
		}

		for (String host : forwards) {
			if (isTrustedForward(host)) {
				continue;
			} else {
				return host;
			}
		}
		// All trusted forwards. This could happen when nodes take on multiple roles. (EG apache/hub and service on same
		// node)
		return forwards.get(forwards.size() - 1);
	}

	private boolean isTrustedForward(String hostAddr) {
		List<ServiceHost> hosts = projectServiceService.findHostsForAddress(hostAddr);
		for (ServiceHost host : hosts) {
			if (host.getServiceHostConfiguration().getSupportedServices().contains(ServiceType.TRUSTED_PROXY)) {
				return true;
			}
		}
		return false;
	}

	private List<String> convertToStringAuthorities(List<GrantedAuthority> authorities) {
		List<String> result = new ArrayList<String>(authorities.size());
		for (GrantedAuthority a : authorities) {
			result.add(a.getAuthority());
		}
		return result;
	}

	private List<GrantedAuthority> getTrustedHostAuthorities(String remoteAddr, HttpServletRequest request) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		// Should only really have multiple hosts in the local dev setup.
		List<ServiceHost> hosts = projectServiceService.findHostsForAddress(remoteAddr);

		for (ServiceHost host : hosts) {
			if (host.getServiceHostConfiguration().getSupportedServices().contains(ServiceType.BUILD_SLAVE)
					&& host.getProjectServices().size() == 1) {
				// Grant the builder access to the project its building for
				authorities.add(new SimpleGrantedAuthority(internalAuthenticationService.toCompoundRole(Role.User, host
						.getProjectServices().get(0).getProjectServiceProfile().getProject().getIdentifier())));
			}

			if (host.getServiceHostConfiguration().getSupportedServices().contains(ServiceType.BUILD)) {
				// Grant the master access only if it has a special header
				String appId = request.getHeader(HeaderConstants.TRUSTED_HOST_PROJECT_ID_HEADER);
				if (appId != null) {
					authorities.add(new SimpleGrantedAuthority(internalAuthenticationService.toCompoundRole(Role.User,
							appId)));
				}
			}

			if (host.getServiceHostConfiguration().getSupportedServices().contains(ServiceType.TASKS)) {
				String appId = request.getHeader(HeaderConstants.TRUSTED_HOST_PROJECT_ID_HEADER);
				if (appId != null) {
					authorities.add(new SimpleGrantedAuthority(Role.System));
				}
			}
		}
		return authorities;

	}

	@Override
	public boolean supports(Class<? extends Object> authentication) {
		return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AuthenticationServiceUser validateCredentials(String username, String password) {
		return new AuthenticationServiceUser(username, password, null, Collections.EMPTY_LIST);
	}

}
