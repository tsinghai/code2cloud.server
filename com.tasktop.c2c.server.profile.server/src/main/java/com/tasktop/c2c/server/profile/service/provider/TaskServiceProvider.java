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
package com.tasktop.c2c.server.profile.service.provider;

import org.springframework.stereotype.Service;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.tasks.service.TaskService;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;


@Service("taskServiceProvider")
public class TaskServiceProvider extends AbstractPreAuthServiceProvider<TaskServiceClient> {

	public TaskServiceProvider() {
		super("/tasks/");
	}

	@Override
	protected TaskServiceClient getNewService() {
		// can't be injected, must be new'd
		// TODO can ask the factory for a new bean (declared as prototype).
		TaskServiceClient service = new TaskServiceClient();
		return service;
	}

	public TaskService getTaskService(String projectIdentifier) {
		setTenancyContext(projectIdentifier);
		return getService(projectIdentifier);
	}

	private void setTenancyContext(String projectIdentifier) {
		TenancyContextHolder.createEmptyContext();
		DefaultTenant tenant = new DefaultTenant();
		tenant.setIdentity(projectIdentifier);
		TenancyContextHolder.getContext().setTenant(tenant);
	}

}
