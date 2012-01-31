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
package com.tasktop.c2c.server.profile.web.ui.server;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.common.web.server.AbstractActionHandler;
import com.tasktop.c2c.server.profile.service.ProfileWebService;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractProfileActionHandler<A extends Action<R>, R extends Result> extends
		AbstractActionHandler<A, R> {

	@Autowired
	@Qualifier("main")
	protected ProfileWebService profileWebService;

	// FIXME copied from AARS
	// REVIEW ugly that we have to do this in so many methods..
	protected void setTenancyContext(String projectIdentifier) {
		TenancyContextHolder.createEmptyContext();
		DefaultTenant tenant = new DefaultTenant();
		tenant.setIdentity(projectIdentifier);
		TenancyContextHolder.getContext().setTenant(tenant);
	}
}
