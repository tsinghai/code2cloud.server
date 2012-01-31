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
package com.tasktop.c2c.server.common.web.client.event;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class NavigationCompleteEvent extends GwtEvent<NavigationCompleteEventHandler> {
	public static Type<NavigationCompleteEventHandler> TYPE = new Type<NavigationCompleteEventHandler>();

	public static HandlerRegistration register(EventBus eventBus, NavigationCompleteEventHandler eventHandler) {
		return eventBus.addHandler(TYPE, eventHandler);
	}

	private final String path;

	public NavigationCompleteEvent(String path) {
		this.path = path;
	}

	@Override
	protected void dispatch(NavigationCompleteEventHandler handler) {
		handler.onNavigate(this);
	}

	@Override
	public Type<NavigationCompleteEventHandler> getAssociatedType() {
		return TYPE;
	}

	public String getPath() {
		return path;
	}
}
