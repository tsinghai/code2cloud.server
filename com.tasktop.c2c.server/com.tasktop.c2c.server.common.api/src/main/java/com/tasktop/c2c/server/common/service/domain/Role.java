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
package com.tasktop.c2c.server.common.service.domain;

public final class Role {

	private Role() {
		// no instantiation of this class.
	}

	public static final String Anonymous = "ROLE_ANONYMOUS";
	public static final String System = "ROLE_SYSTEM";
	public static final String Admin = "ROLE_ADMIN";
	public static final String Community = "ROLE_COMMUNITY";
	public static final String Observer = "ROLE_OBSERVER";
	public static final String User = "ROLE_USER";
	/**
	 * This is a User who has pending legal agreements. As such the permissions are restricted to just agreeing to
	 * agreements.
	 */
	public static final String UserWithPendingAgreements = "ROLE_USER_PENDING_AGREEMENTS";
}
