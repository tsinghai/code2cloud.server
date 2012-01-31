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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.event.domain.Event;
import com.tasktop.c2c.server.event.domain.TaskActivityEvent;
import com.tasktop.c2c.server.event.service.EventListener;
import com.tasktop.c2c.server.profile.service.NotificationService;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Component("taskActivityEventListener")
public class TaskActivityEventListener implements EventListener {

	@Autowired
	private NotificationService notificationService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.event.service.EventListener#onEvent(com.tasktop.c2c.server.event.domain.Event)
	 */
	@Override
	public void onEvent(Event event) {
		if (!(event instanceof TaskActivityEvent)) {
			return;
		}
		TaskActivityEvent taskActivityEvent = (TaskActivityEvent) event;
		notificationService.processTaskActivity(taskActivityEvent.getTaskActivities());
	}

}
