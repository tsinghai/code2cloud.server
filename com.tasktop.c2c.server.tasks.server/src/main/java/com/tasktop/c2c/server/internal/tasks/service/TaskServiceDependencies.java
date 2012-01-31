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


import com.tasktop.c2c.server.event.service.EventService;
import com.tasktop.c2c.server.tasks.service.TaskService;

/**
 * Interface for {@link TaskService} which exposes the dependencies. Useful for testing.
 * 
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * 
 */
public interface TaskServiceDependencies {
	void setEventService(EventService eventService);
}
