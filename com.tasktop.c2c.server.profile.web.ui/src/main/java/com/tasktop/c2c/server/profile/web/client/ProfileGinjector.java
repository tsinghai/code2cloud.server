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
package com.tasktop.c2c.server.profile.web.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.place.shared.PlaceController;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;

public interface ProfileGinjector extends CommonGinjector {

	AppResources getAppResources();

	ProfileServiceAsync getProfileService();

	AppState getAppState();

	PlaceController getPlaceController();

	Scheduler getScheduler();

	public static class get {
		private static ProfileGinjector instance;

		public static synchronized ProfileGinjector instance() {
			return instance;
		}

		public static synchronized ProfileGinjector override(ProfileGinjector appGinjector) {
			instance = appGinjector;
			return appGinjector;
		}
	}
}
