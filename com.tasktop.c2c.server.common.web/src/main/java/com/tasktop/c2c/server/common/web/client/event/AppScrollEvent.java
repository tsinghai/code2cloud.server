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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class AppScrollEvent extends GwtEvent<AppScrollEvent.AppScrollEventHandler> {

	public interface AppScrollEventHandler extends EventHandler {
		void onScroll();
	}

	public static Type<AppScrollEventHandler> TYPE = new Type<AppScrollEventHandler>();

	@Override
	public Type<AppScrollEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(AppScrollEventHandler handler) {
		handler.onScroll();
	}
}
