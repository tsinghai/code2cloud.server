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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.tasktop.c2c.server.common.web.tests.client.JRETestUtil;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.web.client.ProfileServiceAsync;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.SignUpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.SignUpPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.SignUpPresenterImpl;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.SignUpView;

/**
 * @author jhickey
 * 
 */
@SuppressWarnings("unchecked")
public class SignUpPresenterTest {

	private SignUpView view;

	private SignUpPlace place;

	private ProfileServiceAsync profileServiceAsync;

	@Before
	public void setUp() throws Exception {

		// Just a precaution as we don't want an GWT.create() code to spoil the fun.
		// Doing this causes any deferred binding initialisation to return null.
		GWTMockUtilities.disarm();

		// Using a Ginjector lets us redefine implementations throughout the codebase,
		// which is cool
		AppGinjector appGinjector = mock(AppGinjector.class, RETURNS_MOCKS);
		// The Ginjector must be overridden with the mocked version
		AppGinjector.get.override(appGinjector);
		// When creating the mock it was set to return mocks for each method, so instead
		// of creating a mock manually the returned ProfileServiceAsync mock is used.
		ProfileServiceAsync profileServiceAsync = appGinjector.getProfileService();
		// The ProfileServiceAsync must remain static, so it is forced to always return the
		// default here
		when(appGinjector.getProfileService()).thenReturn(profileServiceAsync);
		this.profileServiceAsync = AppGinjector.get.instance().getProfileService();

		// set the Instance
		new ProfileEntryPoint();

		// create mocks needed by Presenter
		view = mock(SignUpView.class);
		place = mock(SignUpPlace.class);
	}

	@Test
	public void testInitTokenNotRequired() {
		when(place.getSignUpToken()).thenReturn(null);
		when(place.isTokenRequired()).thenReturn(false);
		Profile gitHubProfileData = new Profile();
		when(place.getGithubProfile()).thenReturn(gitHubProfileData);
		new SignUpPresenterImpl(view, place);
		verify(view).setSignUpToken(null);
		verify(view).setGitHubProfileData(gitHubProfileData);
	}

	@Test
	public void testInitWithProjInvitationToken() {
		when(place.getSignUpToken()).thenReturn("abc");
		when(place.isTokenRequired()).thenReturn(true);
		ProjectInvitationToken token = new ProjectInvitationToken();
		when(place.getSignUpTokenData()).thenReturn(null);
		when(place.getProjectInvitationTokenData()).thenReturn(token);
		Profile gitHubProfileData = new Profile();
		when(place.getGithubProfile()).thenReturn(gitHubProfileData);
		new SignUpPresenterImpl(view, place);
		verify(view).setProjectInvitationToken(token);
		verify(view).setGitHubProfileData(gitHubProfileData);
	}

	@Test
	public void testInitNoToken() {
		when(place.getSignUpToken()).thenReturn("abc");
		when(place.isTokenRequired()).thenReturn(true);
		when(place.getSignUpTokenData()).thenReturn(null);
		when(place.getProjectInvitationTokenData()).thenReturn(null);
		new SignUpPresenterImpl(view, place);
		verify(view).showSignUpInviteOnlyMessage();
	}

	@Test
	public void testSignup() {
		HasValue<String> username = mock(HasValue.class);
		HasValue<String> password = mock(HasValue.class);
		HasValue<String> passwordConfirm = mock(HasValue.class);
		HasValue<String> firstName = mock(HasValue.class);
		HasValue<String> lastName = mock(HasValue.class);
		HasValue<String> email = mock(HasValue.class);
		when(view.getUsername()).thenReturn(username);
		when(view.getPassword()).thenReturn(password);
		when(view.getPasswordConfirm()).thenReturn(passwordConfirm);
		when(view.getFirstName()).thenReturn(firstName);
		when(view.getLastName()).thenReturn(lastName);
		when(view.getEmail()).thenReturn(email);
		when(username.getValue()).thenReturn("jtest");
		when(password.getValue()).thenReturn("mypass");
		when(passwordConfirm.getValue()).thenReturn("mypass");
		when(firstName.getValue()).thenReturn("Joe");
		when(lastName.getValue()).thenReturn("Tester");
		when(email.getValue()).thenReturn("joe@test.com");
		SignUpPresenter presenter = new SignUpPresenterImpl(view, place);
		when(place.getSignUpToken()).thenReturn("abc");
		when(place.isTokenRequired()).thenReturn(false);

		Profile expectedProfile = new Profile();
		expectedProfile.setEmail("joe@test.com");
		expectedProfile.setFirstName("Joe");
		expectedProfile.setLastName("Tester");
		expectedProfile.setPassword("mypass");
		expectedProfile.setUsername("jtest");
		JRETestUtil.onSuccess(new Credentials()).when(profileServiceAsync)
				.createProfileWithSignUpToken(refEq(expectedProfile), eq("abc"), any(AsyncCallback.class));
		presenter.signup();
		verify(view).clearErrors();
	}

	@Test
	public void testSignupPasswordsNotMatching() {
		SignUpPresenter presenter = new SignUpPresenterImpl(view, place);
		when(place.getSignUpToken()).thenReturn("abc");
		when(place.isTokenRequired()).thenReturn(false);
		HasValue<String> password = mock(HasValue.class);
		HasValue<String> passwordConfirm = mock(HasValue.class);
		when(view.getPassword()).thenReturn(password);
		when(view.getPasswordConfirm()).thenReturn(passwordConfirm);
		when(password.getValue()).thenReturn("mypass");
		when(passwordConfirm.getValue()).thenReturn("mypass234");
		presenter.signup();
		verify(view).clearErrors();
		verify(view).showPassMatchError();
	}

}
