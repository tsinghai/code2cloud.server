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

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.profile.web.shared.actions.ToggleProfileDisableResult;
import com.tasktop.c2c.server.profile.web.shared.actions.ToggleProfileDisabledAction;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class ToggleProfileDisabledActionHandler extends
		AbstractProfileActionHandler<ToggleProfileDisabledAction, ToggleProfileDisableResult> {

	public ToggleProfileDisabledActionHandler() {
		super();
	}

	@Override
	public ToggleProfileDisableResult execute(ToggleProfileDisabledAction action, ExecutionContext context)
			throws DispatchException {
		action.getProfile().setAccountDisabled(action.isDisable());
		try {
			profileWebService.updateProfile(action.getProfile());
		} catch (ValidationException e) {
			handle(e);
		} catch (EntityNotFoundException e) {
			handle(e);
		}
		return new ToggleProfileDisableResult(action.getProfile()); // FIXME
	}

}
