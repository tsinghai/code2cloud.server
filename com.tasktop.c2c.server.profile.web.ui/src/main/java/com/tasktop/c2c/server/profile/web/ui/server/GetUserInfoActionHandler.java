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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.profile.web.shared.actions.GetUserInfoAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetUserInfoResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class GetUserInfoActionHandler extends AbstractProfileActionHandler<GetUserInfoAction, GetUserInfoResult> {

	@Override
	public GetUserInfoResult execute(GetUserInfoAction action, ExecutionContext context) throws DispatchException {
		UserInfo ui = new UserInfo();
		ui.setCredentials(getCurrentUser());
		if (ui.getCredentials() != null && !ui.getCredentials().getProfile().getAccountDisabled()) {
			List<Agreement> agreements;
			try {
				agreements = profileWebService.getPendingAgreements();
				ui.setHasPendingAgreements(agreements.size() > 0 ? true : false);
			} catch (EntityNotFoundException e) {
				ui.setHasPendingAgreements(false);
			}
		} else {
			ui.setHasPendingAgreements(false);
		}

		return new GetUserInfoResult(ui);
	}

	private Credentials getCurrentUser() {
		com.tasktop.c2c.server.profile.domain.project.Profile profile = profileWebService.getCurrentProfile();
		if (profile == null) {
			return null;
		}
		List<String> roles = new ArrayList<String>();

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
				roles.add(authority.getAuthority());
			}
		}

		return new Credentials(profile, roles);
	}

}
