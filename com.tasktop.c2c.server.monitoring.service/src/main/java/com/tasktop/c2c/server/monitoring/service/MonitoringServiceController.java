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

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.monitoring.domain.MonitoringEvent;
import com.tasktop.c2c.server.monitoring.service.MonitoringService;


public class MonitoringServiceController extends AbstractRestService {

	private MonitoringService monitoringService;

	@RequestMapping(value = "/process", method = RequestMethod.POST)
	public void process(@RequestBody MonitoringEvent event) {
		monitoringService.processEvent(event);
	}

	public void setMonitoringService(MonitoringService monitoringService) {
		this.monitoringService = monitoringService;
	}
}
