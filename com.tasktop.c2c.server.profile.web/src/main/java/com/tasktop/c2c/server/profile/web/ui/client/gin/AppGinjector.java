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
package com.tasktop.c2c.server.profile.web.ui.client.gin;


import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.App;
import com.tasktop.c2c.server.profile.web.ui.client.AppShell;
import com.tasktop.c2c.server.profile.web.ui.client.BuildServiceAsync;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentServiceAsync;
import com.tasktop.c2c.server.profile.web.ui.client.activity.HeaderActivityMapper;
import com.tasktop.c2c.server.profile.web.ui.client.activity.MainActivityMapper;
import com.tasktop.c2c.server.profile.web.ui.client.resources.AppConstants;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
@GinModules({ AppGinModule.class })
public interface AppGinjector extends ProfileGinjector {

	PlaceHistoryHandler getPlaceHistoryHandler();

	PlaceHistoryMapper getPlaceHistoryMapper();

	AppShell getAppShell();

	PlaceController getPlaceController();

	App getApp();

	DeploymentServiceAsync getDeploymentService();

	BuildServiceAsync getBuildService();

	MainActivityMapper getMainActivityMapper();

	HeaderActivityMapper getHeaderActivityMapper();

	AppConstants getConstants();

	public static class get {
		private static AppGinjector instance = GWT.isClient() ? (AppGinjector) GWT.create(AppGinjector.class) : null;

		public static synchronized AppGinjector instance() {
			return instance;
		}

		public static synchronized AppGinjector override(AppGinjector appGinjector) {
			instance = appGinjector;
			CommonGinjector.get.override(appGinjector);
			ProfileGinjector.get.override(appGinjector);
			return appGinjector;
		}
	}
}
