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
package com.tasktop.c2c.server.common.web.client.view;

import net.customware.gwt.dispatch.client.DispatchAsync;


import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.Ginjector;
import com.tasktop.c2c.server.common.web.client.notification.NotificationPanel;
import com.tasktop.c2c.server.common.web.client.notification.Notifier;

public interface CommonGinjector extends Ginjector {

	EventBus getEventBus();

	NotificationPanel getNotificationPanel();

	Notifier getNotifier();

	DispatchAsync getDispatchService();

	public static class get {
		private static CommonGinjector instance;

		public static synchronized CommonGinjector instance() {
			return instance;
		}

		public static synchronized CommonGinjector override(CommonGinjector appGinjector) {
			instance = appGinjector;
			return appGinjector;
		}
	}
}
