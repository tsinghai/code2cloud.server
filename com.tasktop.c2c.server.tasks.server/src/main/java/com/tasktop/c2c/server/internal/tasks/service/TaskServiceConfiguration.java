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
package com.tasktop.c2c.server.internal.tasks.service;

import org.springframework.tenancy.context.TenancyContextHolder;

import com.tasktop.c2c.server.common.service.BaseProfileConfiguration;

/**
 * Simple container class for the configuration needed by task service.
 */
public class TaskServiceConfiguration extends BaseProfileConfiguration {

	public String getProfileProjectIdentifier() {
		return (String) TenancyContextHolder.getContext().getTenant().getIdentity();
	}

	public String getExternalTaskServiceUrl() {
		return getServiceUrlPrefix(getProfileProjectIdentifier()) + "tasks";
	}

	public String getWebUrlForTask(Integer taskId) {
		return String.format("%s/#projects/%s/task/%s", getProfileBaseUrl(), getProfileProjectIdentifier(), taskId);
	}

	public String getWebUrlForAttachment(Integer attachmentId) {
		return getExternalTaskServiceUrl() + "/attachment/" + attachmentId;
	}

}
