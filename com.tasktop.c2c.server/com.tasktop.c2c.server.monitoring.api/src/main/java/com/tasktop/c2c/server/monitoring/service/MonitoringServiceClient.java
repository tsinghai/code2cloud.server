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
package com.tasktop.c2c.server.monitoring.service;


import com.tasktop.c2c.server.common.service.web.AbstractTrustedHostRestServiceClient;
import com.tasktop.c2c.server.monitoring.domain.MonitoringEvent;


public class MonitoringServiceClient extends
		AbstractTrustedHostRestServiceClient implements MonitoringService {

	@Override
	public void processEvent(MonitoringEvent event) {
		currentProjectIdentifier.set(getProjectIdentifier(event));
		try {
		template.postForObject(computeUrl("/process"), event, Void.class);
		} finally {
			currentProjectIdentifier.remove();
		}
	}

	public static String getProjectIdentifier(MonitoringEvent event) {
		int lastSlash = event.getEventApplication().lastIndexOf("/");
		if (lastSlash == -1) {
			return event.getEventApplication();
		}
		return event.getEventApplication().substring(lastSlash + 1);
	}

}
