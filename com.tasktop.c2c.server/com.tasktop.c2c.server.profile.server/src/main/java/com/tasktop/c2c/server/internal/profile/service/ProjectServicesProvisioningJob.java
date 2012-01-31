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
package com.tasktop.c2c.server.internal.profile.service;

import org.springframework.context.ApplicationContext;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.profile.domain.internal.Project;

@SuppressWarnings("serial")
public class ProjectServicesProvisioningJob extends Job {

	private Long projectId;
	private ServiceType serviceType;

	public ProjectServicesProvisioningJob(Project project, ServiceType serviceType) {
		this.projectId = project.getId();
		this.serviceType = serviceType;
	}

	@Override
	public void execute(ApplicationContext applicationContext) {
		InternalApplicationService service = applicationContext.getBean("projectServiceService",
				InternalApplicationService.class);
		try {
			service.doProvisionServices(projectId, serviceType);
		} catch (EntityNotFoundException e) {
			throw new IllegalStateException(e);
		} catch (ProvisioningException e) {
			throw new IllegalStateException(e);
		}
	}

}
