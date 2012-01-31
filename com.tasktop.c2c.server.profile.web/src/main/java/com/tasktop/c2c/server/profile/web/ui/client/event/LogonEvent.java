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
import com.tasktop.c2c.server.profile.web.shared.Credentials;

public class LogonEvent extends GwtEvent<LogonEventHandler> {
	public static Type<LogonEventHandler> TYPE = new Type<LogonEventHandler>();
	
	private Credentials credentials;
	
	public LogonEvent(Credentials credentials) {
		this.credentials = credentials;
	}

	@Override
	protected void dispatch(LogonEventHandler handler) {
		handler.onLogon(credentials);
	}

	@Override
	public Type<LogonEventHandler> getAssociatedType() {
		return TYPE;
	}

}
