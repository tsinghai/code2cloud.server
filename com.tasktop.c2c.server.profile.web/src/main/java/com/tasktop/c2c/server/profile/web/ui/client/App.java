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
package com.tasktop.c2c.server.profile.web.ui.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;
import com.tasktop.c2c.server.common.web.client.event.EmbeddedNavigationEvent;
import com.tasktop.c2c.server.common.web.client.event.EmbeddedNavigationEventHandler;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEvent;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEventHandler;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class App {

	private final AppGinjector injector = AppGinjector.get.instance();

	public void run(HasWidgets.ForIsWidget root) {
		addHandlers();

		root.add(injector.getAppShell());

		ActivityManager headerActivityManager = new ActivityManager(injector.getHeaderActivityMapper(),
				injector.getEventBus());
		headerActivityManager.setDisplay(injector.getAppShell().getHeaderRegion());
		ActivityManager mainActivityManager = new ActivityManager(injector.getMainActivityMapper(),
				injector.getEventBus());
		mainActivityManager.setDisplay(injector.getAppShell().getMainContentRegion());

		injector.getPlaceHistoryHandler().handleCurrentHistory();
	}

	public static native void registerGAPageVisit() /*-{
		var url = decodeURIComponent($wnd.location.toString());
		$wnd._gaq.push([ '_trackPageview', url.split('#')[1] ]);
	}-*/;

	private void addHandlers() {
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				injector.getNotifier().displayMessage(Message.createErrorMessage("A client-side error has occurred"));
				GWT.log("Uncaught exception escaped", e);
			}
		});

		final EventBus eventBus = injector.getEventBus();
		eventBus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
			public void onPlaceChange(PlaceChangeEvent event) {
				registerGAPageVisit();
			}
		});

		eventBus.addHandler(EmbeddedNavigationEvent.TYPE, new EmbeddedNavigationEventHandler() {
			@Override
			public void onNavigate(String navigation) {
				if (!navigation.equals(History.getToken())) {
					History.newItem(navigation, false);
				}
			}
		});

		eventBus.addHandler(LogoutEvent.TYPE, new LogoutEventHandler() {
			@Override
			public void onLogout() {
				injector.getProfileService().logout(new AsyncCallbackSupport<Boolean>() {
					@Override
					public void success(Boolean result) {
						injector.getAppState().setCredentials(null);
						ProjectsDiscoverPlace place = ProjectsDiscoverPlace.createPlaceForAfterLogout();
						place.displayOnArrival(Message.createSuccessMessage("Signed out"));
						place.go();
					}
				});

				// Now that we've made our call, wipe out our local credentials.
				injector.getAppState().setCredentials(null);
			}
		});

	}
}
