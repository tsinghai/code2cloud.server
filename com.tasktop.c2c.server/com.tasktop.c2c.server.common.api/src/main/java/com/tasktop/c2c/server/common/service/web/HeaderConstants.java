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
package com.tasktop.c2c.server.common.service.web;

public class HeaderConstants {

	/** Prefix for headers that can be used from outside requests. */
	public static final String ALM_PUBLIC_HEADER_PREFIX = "C2C-";

	/**
	 * Header for a trusted host's applicationId. This is used to authorize a request from a trusted host (hudson
	 * master) for a particular application.
	 */
	public static final String TRUSTED_HOST_PROJECT_ID_HEADER = ALM_PUBLIC_HEADER_PREFIX
			+ "TrustedHostApplicationId";

	/** Prefix for headers that can only be used with internal requests. */
	public static final String ALM_PRIVATE_HEADER_PREFIX = "C2C-Internal";

	/** Header for the tenant identifier of the request. Used in the HeaderTenantIdentificationStrategy. */
	public static final String TENANT_HEADER = ALM_PRIVATE_HEADER_PREFIX + "ApplicationId";

	/** Pre-Auth header. */
	public static final String PREAUTH_AUTHORIZATION_HEADER = "Authorization";

	/** Pre-Auth header value prefix. */
	public static final String PRE_AUTH_AUTHORIZATION_HEADER_VALUE_PREFIX = "almtoken ";

	/**
	 * Check if the given header name/value is an internal-only header, and should not be accepted from external
	 * requests.
	 * 
	 * @param headerName
	 * @param headerValue
	 * @return true if is internal
	 */
	public static boolean isAlmInternalHeader(String headerName, String headerValue) {
		if (headerName.startsWith(ALM_PRIVATE_HEADER_PREFIX)) {
			return true;
		}
		if (headerName.equals(PREAUTH_AUTHORIZATION_HEADER)
				&& headerValue.startsWith(PRE_AUTH_AUTHORIZATION_HEADER_VALUE_PREFIX)) {
			return true;
		}
		return false;
	}

}
