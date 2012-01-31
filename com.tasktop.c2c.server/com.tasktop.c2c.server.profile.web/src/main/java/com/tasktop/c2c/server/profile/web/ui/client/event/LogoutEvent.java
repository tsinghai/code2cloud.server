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
package com.tasktop.c2c.server.profile.web.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class LogoutEvent extends GwtEvent<LogoutEventHandler> {
	public static Type<LogoutEventHandler> TYPE = new Type<LogoutEventHandler>();

	public LogoutEvent() {
	}

	@Override
	protected void dispatch(LogoutEventHandler handler) {
		handler.onLogout();
	}

	@Override
	public Type<LogoutEventHandler> getAssociatedType() {
		return TYPE;
	}
}
