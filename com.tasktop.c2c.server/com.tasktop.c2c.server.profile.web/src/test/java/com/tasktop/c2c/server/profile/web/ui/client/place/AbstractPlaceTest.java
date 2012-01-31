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
package com.tasktop.c2c.server.profile.web.ui.client.place;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.BatchAction;
import net.customware.gwt.dispatch.shared.BatchResult;
import net.customware.gwt.dispatch.shared.DispatchException;
import net.customware.gwt.dispatch.shared.Result;

import org.junit.Before;
import org.mockito.Mockito;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceController.Delegate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.web.tests.client.JRETestUtil;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.AppState;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractPlaceTest {

	protected AppGinjector appGinjector;
	protected DispatchAsync dispatchService;
	protected EventBus eventBus;
	protected PlaceController placeController;
	protected AppState appState;

	protected String projectId = "projectId";

	/**
	 * 
	 */
	public AbstractPlaceTest() {
		super();
	}

	@Before
	public void setUp() throws Exception {
		GWTMockUtilities.disarm();
		appGinjector = mock(AppGinjector.class, RETURNS_MOCKS);
		AppGinjector.get.override(appGinjector);

		dispatchService = appGinjector.getDispatchService();
		when(appGinjector.getDispatchService()).thenReturn(dispatchService); // return the same mock each time;

		eventBus = new CountingEventBus();
		when(appGinjector.getEventBus()).thenReturn(eventBus);

		placeController = new AppPlaceController(eventBus, mock(Delegate.class));
		placeController = Mockito.spy(placeController);
		when(appGinjector.getPlaceController()).thenReturn(placeController);

		appState = new AppState();
		when(appGinjector.getAppState()).thenReturn(appState);

		// set the Instance
		new ProfileEntryPoint();
	}

	protected UserInfo getAnonUserInfo() {
		UserInfo info = new UserInfo();
		info.setCredentials(null);
		info.setHasPendingAgreements(false);
		return info;
	}

	protected UserInfo getRegularUserInfo() {
		UserInfo info = new UserInfo();
		Profile p = new Profile();
		p.setEmail("user@example.com");
		p.setFirstName("First");
		p.setGravatarHash("gravhash");
		p.setLastName("Last");
		p.setUsername("username");
		p.setAccountDisabled(false);
		Credentials c = new Credentials();
		c.setProfile(p);
		info.setCredentials(c);
		info.setHasPendingAgreements(false);
		return info;
	}

	protected void mockDispatchResult(Result... successResults) {
		mockDispatchResult(Arrays.asList(successResults), null);
	}

	protected void mockDispatchExceptions(Result successResult, DispatchException... exceptions) {
		mockDispatchResult(Arrays.asList(successResult), Arrays.asList(exceptions));
	}

	protected void mockDispatchResult(List<Result> successResults, List<DispatchException> failureExceptions) {
		JRETestUtil.onSuccess(getBatchResult(successResults, failureExceptions)).when(dispatchService)
				.execute(any(BatchAction.class), any(AsyncCallback.class));
	}

	protected void mockDispatchResults(BatchResult... results) {
		JRETestUtil.callMultiple(Arrays.asList(results)).when(dispatchService)
				.execute(any(BatchAction.class), any(AsyncCallback.class));
	}

	protected BatchResult getBatchResult(DispatchException exception, Result... results) {
		return getBatchResult(Arrays.asList(results), Arrays.asList(exception));
	}

	protected BatchResult getBatchResult(Result... results) {
		return getBatchResult(Arrays.asList(results), null);
	}

	protected BatchResult getBatchResult(List<Result> results, List<DispatchException> exceptions) {
		if (results == null) {
			results = new ArrayList<Result>();
		}
		if (exceptions == null) {
			exceptions = new ArrayList<DispatchException>();
		}
		return new BatchResult(results, exceptions);
	}

	protected Project getMockProject() {
		Project project = new Project();
		project.setIdentifier(projectId);
		project.setName("Project Name");
		project.setPublic(false);
		project.setDescription("desc");
		return project;
	}

}
