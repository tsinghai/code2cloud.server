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

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.job.Job;

@SuppressWarnings("serial")
public class ServiceHostProvisioningJob extends Job {

	private Set<ServiceType> serviceHostType;

	public ServiceHostProvisioningJob(Set<ServiceType> type) {
		serviceHostType = type;
	}

	@Override
	public void execute(ApplicationContext applicationContext) {

		final NodeLifecycleService nodeLifecycleService = applicationContext
				.getBean(NodeLifecycleServiceProvider.class).getServiceForType(serviceHostType.iterator().next());
		final ServiceHostService serviceHostService = applicationContext.getBean(ServiceHostService.class);

		final String newNodeIp = nodeLifecycleService.createNewNode();
		ServiceHost node = new ServiceHost();
		node.setSupportedServices(serviceHostType);
		node.setInternalNetworkAddress(newNodeIp);
		node.setAvailable(true);
		node = serviceHostService.createServiceHost(node);
		serviceHostService.recordAllocationComplete(serviceHostType);
	}

}
