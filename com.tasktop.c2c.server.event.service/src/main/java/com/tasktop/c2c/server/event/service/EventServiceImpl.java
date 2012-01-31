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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.event.domain.Event;
import com.tasktop.c2c.server.event.service.EventListener;
import com.tasktop.c2c.server.event.service.EventService;

@Service("eventService")
public class EventServiceImpl implements EventService {

	private Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

	@Autowired
	private InternalAuthenticationService authService;

	private List<EventListener> eventListeners;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	@Secured(Role.System)
	@Override
	public void publishEvent(Event event) {
		executorService.submit(new EventCallable(event));
	}

	@Autowired
	public void setEventListeners(List<EventListener> eventListeners) {
		this.eventListeners = eventListeners;
	}

	private class EventCallable implements Callable<Object> {

		private Event event;

		public EventCallable(Event event) {
			this.event = event;
		}

		@Override
		public Object call() throws Exception {

			TenancyContextHolder.createEmptyContext();
			TenancyContextHolder.getContext().setTenant(new DefaultTenant(event.getProjectId(), null));

			authService.assumeSystemIdentity(event.getProjectId());

			try {
				for (EventListener listener : eventListeners) {
					try {
						listener.onEvent(event);
					} catch (Throwable t) {
						LOGGER.warn(
								String.format("Listener [%s] threw exception on event [%s]", listener.toString(),
										event.toString()), t);
						// continue;
					}
				}
			} finally {
				TenancyContextHolder.clearContext();
				SecurityContextHolder.clearContext();
			}
			return null;
		}

	}

}
