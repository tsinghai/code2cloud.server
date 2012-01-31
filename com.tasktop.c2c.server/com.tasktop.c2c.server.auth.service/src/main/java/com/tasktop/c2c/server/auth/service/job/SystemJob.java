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
package com.tasktop.c2c.server.auth.service.job;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.common.service.job.Job;

public abstract class SystemJob extends Job {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param applicationContext
	 * @param projectIdentifier
	 *            the project identifier or null
	 * @param runnable
	 *            the operation to perform
	 */
	protected void executeAsSystem(ApplicationContext applicationContext, String projectIdentifier, Runnable runnable) {
		assumeSystemIdentity(applicationContext, projectIdentifier);
		try {
			runnable.run();
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	protected final void assumeSystemIdentity(ApplicationContext applicationContext, String projectIdentifier) {
		InternalAuthenticationService authenticationService = applicationContext.getBean(
				"internalAuthenticationService", InternalAuthenticationService.class);
		authenticationService.assumeSystemIdentity(projectIdentifier);
	}
}
