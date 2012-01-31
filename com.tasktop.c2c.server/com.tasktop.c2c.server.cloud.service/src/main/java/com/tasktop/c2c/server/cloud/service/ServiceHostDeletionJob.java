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
package com.tasktop.c2c.server.cloud.service;

import java.util.Set;

import org.springframework.context.ApplicationContext;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.job.Job;

@SuppressWarnings("serial")
public class ServiceHostDeletionJob extends Job {

	private Set<ServiceType> serviceHostType;
	private String internalNetAddress;

	public ServiceHostDeletionJob(Set<ServiceType> serviceHostType, String internalNetAddress) {
		this.serviceHostType = serviceHostType;
		this.internalNetAddress = internalNetAddress;
	}

	@Override
	public void execute(ApplicationContext applicationContext) {

		NodeLifecycleService nodeLifecycleService = applicationContext.getBean(NodeLifecycleServiceProvider.class)
				.getServiceForType(serviceHostType.iterator().next());
		nodeLifecycleService.decomissionNode(internalNetAddress);
	}

}
