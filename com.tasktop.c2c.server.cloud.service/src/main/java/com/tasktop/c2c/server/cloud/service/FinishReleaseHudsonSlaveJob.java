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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.job.Job;

public class FinishReleaseHudsonSlaveJob extends Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(FinishReleaseHudsonSlaveJob.class);

	private static String cleaningServiceBeanName = "hudsonSlaveNodeCleaningService";

	private String projectId;
	private Long serviceHostId;

	public FinishReleaseHudsonSlaveJob(String projectId, Long serviceHostId) {
		this.serviceHostId = serviceHostId;
		this.projectId = projectId;
	}

	@Override
	public void execute(ApplicationContext applicationContext) {
		final ServiceHostService serviceHostService = applicationContext.getBean(ServiceHostService.class);
		NodeCleaningService nodeCleaningService = applicationContext.getBean(cleaningServiceBeanName,
				NodeCleaningService.class);
		try {

			// DO the node-deallocation first so it functions as an auth check that the projected did own it, and thus
			// it can be freed.
			final ServiceHost node = serviceHostService.retrieve(serviceHostId);
			serviceHostService.deallocateHostFromProject(node, projectId);

			nodeCleaningService.cleanNode(node);

		} catch (EntityNotFoundException e) {
			LOGGER.warn("", e);
		} catch (IOException e) {
			LOGGER.warn("", e);
		}

	}

	/**
	 * Set the name of the bean to be used for the node cleaning service. Usefull for testing.
	 * 
	 * @param name
	 */
	public static void setCleaningServiceBeanName(String name) {
		cleaningServiceBeanName = name;
	}

}
