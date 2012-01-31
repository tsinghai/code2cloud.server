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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.event.domain.Event;
import com.tasktop.c2c.server.event.service.EventService;
import com.tasktop.c2c.server.event.service.EventServiceClient;


public class EventServiceController extends AbstractRestService {
	@Autowired
	private EventService eventService;

	@RequestMapping(value = EventServiceClient.PUBLISH_EVENT_URL, method = RequestMethod.POST)
	public void publish(@RequestBody Event event) {
		eventService.publishEvent(event);
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

}
