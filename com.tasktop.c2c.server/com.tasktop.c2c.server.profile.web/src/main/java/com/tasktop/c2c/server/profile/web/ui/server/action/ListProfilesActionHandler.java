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
package com.tasktop.c2c.server.profile.web.ui.server.action;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.internal.profile.service.WebServiceDomain;
import com.tasktop.c2c.server.profile.web.shared.actions.ListProfilesAction;
import com.tasktop.c2c.server.profile.web.shared.actions.ListProfilesResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class ListProfilesActionHandler extends AbstractProfileActionHandler<ListProfilesAction, ListProfilesResult> {

	@Autowired
	private WebServiceDomain webServiceDomain;

	public ListProfilesActionHandler() {
		super();
	}

	@Override
	public ListProfilesResult execute(ListProfilesAction action, ExecutionContext context) throws DispatchException {
		return new ListProfilesResult(webServiceDomain.copyProfiles(profileService.listAllProfiles()));
	}

}
