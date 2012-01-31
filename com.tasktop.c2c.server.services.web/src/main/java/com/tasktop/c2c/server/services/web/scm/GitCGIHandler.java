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
package com.tasktop.c2c.server.services.web.scm;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.tenancy.context.TenancyContextHolder;


import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.web.cgi.CGIHandler;
import com.tasktop.c2c.server.services.web.scm.GitConstants;

public class GitCGIHandler extends CGIHandler {

	private static final String GIT_ROOT_PATH_ENV_VAR = "GIT_PROJECT_ROOT";
	public static final String GIT_RECEIVE_PACK_URL = "git-receive-pack";
	public static final String GIT_UPLOAD_PACK_URL = "git-upload-pack";

	private String gitRoot;

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (!validRequest(request)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"The given Git request was not considered a valid Git command which is recognized by this service.");

			// We're done here, so bail out.
			return;
		}

		if (!isRequestPermitted(request)) {

			AuthenticationToken token = AuthenticationServiceUser.getCurrent().getToken();
			List<String> roles = token.getAuthorities();

			// Our request was rejected - time to send back an appropriate error.
			if (roles.contains(Role.Anonymous)) {
				// This was an anonymous request, so prompt the user for credentials - perhaps they can still do this.
				response.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", TenancyContextHolder
						.getContext().getTenant().getIdentity()));
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login to continue");
			} else {
				// This user was authenticated, but this request is not allowed for permissions reasons - reject it.
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Insufficient permissions to perform this Git request");
			}

			// We're done here, so bail out.
			return;
		}

		// If we got here, we have a valid request and sufficient permissions to perform it - go ahead and do it now.
		super.service(request, response);
	}

	private boolean validRequest(HttpServletRequest request) {
		// Confirm that the Git request we have received is one we are expecting.
		if (request.getMethod().equals("GET")) {
			return true;
		}

		if (request.getMethod().equals("POST")) {
			String uri = request.getRequestURI();

			if (uri.endsWith(GIT_RECEIVE_PACK_URL) || uri.endsWith(GIT_UPLOAD_PACK_URL)) {
				return true;
			}
		}

		// This wasn't recognized, so bail out.
		return false;
	}

	private boolean isWriteRequest(HttpServletRequest request) {
		// All Git writes are done as calls which end with "git-receive-pack", so if it's not that, it's a read.

		if (request.getMethod().equals("GET")) {
			return GIT_RECEIVE_PACK_URL.equals(request.getParameter("service"));
		} else if (request.getMethod().equals("POST")) {
			return request.getRequestURI().endsWith(GIT_RECEIVE_PACK_URL);
		}

		return false;
	}

	private boolean isRequestPermitted(HttpServletRequest request) {
		// Determine whether this request is a write.
		boolean isWrite = isWriteRequest(request);

		// Do our security checking now.
		AuthenticationServiceUser user = AuthenticationServiceUser.getCurrent();

		if (user == null) {
			// Something's wrong if our authentication is missing - prevent the request.
			return false;
		}

		// If the Observer role is present, then this is a public project.
		boolean isPublic = user.getToken().getAuthorities().contains(Role.Observer);
		List<String> userRoles = user.getToken().getAuthorities();

		// Now that we have our necessary info, determine whether this is an allowed operation.
		if (isWrite) {
			// Since this is a write, only ever allow project users to do it (regardless of whether the project is
			// public or not).
			return (userRoles.contains(Role.User));
		} else {
			// This is a read operation
			if (isPublic) {
				// If it's a public project, we always allow reading.
				return true;
			} else {
				// If it's a private project, we only want project users seeing it.
				return (userRoles.contains(Role.User));
			}
		}
	}

	@Override
	protected void configureEnvironment(HttpServletRequest request) {
		super.configureEnvironment(request);
		getEnvironment().put(
				GIT_ROOT_PATH_ENV_VAR,
				gitRoot + "/" + TenancyContextHolder.getContext().getTenant().getIdentity() + "/"
						+ GitConstants.HOSTED_GIT_DIR);
	}

	@Override
	public CGIHandler clone() {
		GitCGIHandler copy = (GitCGIHandler) super.clone();
		copy.gitRoot = this.gitRoot;
		return copy;

	}

	public String getGitRoot() {
		return gitRoot;
	}

	public void setGitRoot(String gitRoot) {
		this.gitRoot = gitRoot;
	}
}
