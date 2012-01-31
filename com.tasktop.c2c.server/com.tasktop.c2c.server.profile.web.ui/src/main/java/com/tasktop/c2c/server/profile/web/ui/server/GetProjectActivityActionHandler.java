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

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.profile.service.ActivityService;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectActivityAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectActivityResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class GetProjectActivityActionHandler extends
		AbstractProfileActionHandler<GetProjectActivityAction, GetProjectActivityResult> {

	@Autowired
	@Qualifier("main")
	private ActivityService activityService;

	@Override
	public GetProjectActivityResult execute(GetProjectActivityAction action, ExecutionContext context)
			throws DispatchException {
		setTenancyContext(action.getProjectId());

		if (action.isShortList()) {
			return new GetProjectActivityResult(activityService.getShortActivityList(action.getProjectId()));
		} else {
			return new GetProjectActivityResult(activityService.getRecentActivity(action.getProjectId()));
		}

	}
}
