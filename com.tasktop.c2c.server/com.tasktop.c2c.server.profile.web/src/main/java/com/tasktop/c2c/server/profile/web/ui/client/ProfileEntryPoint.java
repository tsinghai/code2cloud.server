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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.customware.gwt.dispatch.shared.DispatchException;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.RootPanel;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.shared.AuthenticationFailedException;
import com.tasktop.c2c.server.common.web.shared.AuthenticationRequiredException;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.common.web.shared.ValidationFailedException;
import com.tasktop.c2c.server.profile.web.client.AppState;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.ProfileServiceAsync;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;
import com.tasktop.c2c.server.tasks.client.TaskPageMappings;
import com.tasktop.c2c.server.wiki.web.ui.client.WikiPageMappings;

public class ProfileEntryPoint implements EntryPoint {

	private static ProfileEntryPoint instance;
	private final AppGinjector injector = AppGinjector.get.instance();

	public ProfileEntryPoint() {
		instance = this;

		// FIXME this could be in a better place
		AsyncCallbackSupport.setErrorHandler(new AsyncCallbackSupport.ErrorHandler() {

			@Override
			public List<String> getErrors(Throwable exception) {
				// FIXME: NLS
				if (exception instanceof ValidationFailedException) {
					return ((ValidationFailedException) exception).getMessages();
				} else if (exception instanceof AuthenticationFailedException) {
					if (exception.getMessage() == null) {
						return Collections.singletonList("Invalid username or password. Please try again.");
					} else {
						return Collections.singletonList(exception.getMessage());
					}
				} else if (exception instanceof NoSuchEntityException) {
					return Collections.singletonList("The referenced entity cannot be found or no longer exists.");
				} else if (exception instanceof AuthenticationRequiredException) {
					SignInPlace.createPlace().go();
					return Collections.singletonList(exception.getMessage());
				} else if (exception instanceof StatusCodeException) {
					StatusCodeException statusCodeException = (StatusCodeException) exception;
					if (statusCodeException.getStatusCode() == 500) {
						return Collections.singletonList("Unexpected error (HTTP 500)");
					} else {
						return Collections.singletonList(statusCodeException.getMessage());
					}
				} else if (exception instanceof DispatchException) {
					DispatchException dispatchException = (DispatchException) exception;
					if (dispatchException.getCauseClassname() != null
							&& dispatchException.getCauseClassname().equals(ValidationFailedException.class.getName())) {
						return Arrays.asList(dispatchException.getMessage().split(","));
					}
				}

				return Collections.singletonList(exception.getClass() + ": " + exception.getMessage());

			}
		});

		// FIXME HACK to register page mappings
		TaskPageMappings.ProjectTask.hashCode();
		WikiPageMappings.ProjectWiki.hashCode();
		PageMappings.Discover.hashCode();

		// FIXME better way?
		CommonGinjector.get.override(injector);
		ProfileGinjector.get.override(injector);
	}

	public static ProfileEntryPoint getInstance() {
		return instance;
	}

	public EventBus getEventBus() {
		return injector.getEventBus();
	}

	public ProfileServiceAsync getProfileService() {
		return injector.getProfileService();
	}

	public BuildServiceAsync getBuildService() {
		return injector.getBuildService();
	}

	public DeploymentServiceAsync getDeploymentService() {
		return injector.getDeploymentService();
	}

	public AppState getAppState() {
		return injector.getAppState();
	}

	@Override
	public void onModuleLoad() {
		RootPanel.get().add(injector.getNotificationPanel());
		injector.getApp().run(RootPanel.get());
	}
}
