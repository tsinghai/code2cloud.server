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
package com.tasktop.c2c.server.event.service;

import org.springframework.tenancy.context.TenancyContextHolder;

import com.tasktop.c2c.server.common.service.web.AbstractTrustedHostRestServiceClient;
import com.tasktop.c2c.server.event.domain.Event;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class EventServiceClient extends AbstractTrustedHostRestServiceClient implements EventService {

	public static final String PUBLISH_EVENT_URL = "publish";

	private String projectId = null;

	@Override
	public void publishEvent(Event event) {
		String currentProjectId;
		if (projectId == null) {
			currentProjectId = TenancyContextHolder.getContext().getTenant().getIdentity().toString();
		} else {
			currentProjectId = projectId;
		}
		currentProjectIdentifier.set(currentProjectId);
		template.postForObject(computeUrl(PUBLISH_EVENT_URL), event, Void.class);
		currentProjectIdentifier.remove();
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

}
