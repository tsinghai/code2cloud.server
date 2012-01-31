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

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.service.ProfileServiceConfiguration;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectScmRepositoriesAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectScmRepositoriesResult;
import com.tasktop.c2c.server.profile.web.ui.server.AbstractProfileActionHandler;
import com.tasktop.c2c.server.scm.service.ScmService;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class GetProjectScmRepositoriesActionHandler extends
		AbstractProfileActionHandler<GetProjectScmRepositoriesAction, GetProjectScmRepositoriesResult> {

	@Autowired
	@Qualifier("main")
	private ScmService scmService;

	@Autowired
	private ProfileServiceConfiguration profileServiceConfiguration;

	@Override
	public GetProjectScmRepositoriesResult execute(GetProjectScmRepositoriesAction action, ExecutionContext context)
			throws DispatchException {
		try {
			setTenancyContext(action.getProjectId());
			return new GetProjectScmRepositoriesResult(new ArrayList<ScmRepository>(scmService.getScmRepositories()),
					profileServiceConfiguration.getHostedScmUrlPrefix(action.getProjectId()));
		} catch (EntityNotFoundException e) {
			handle(e);
		}
		throw new IllegalStateException();
	}
}
