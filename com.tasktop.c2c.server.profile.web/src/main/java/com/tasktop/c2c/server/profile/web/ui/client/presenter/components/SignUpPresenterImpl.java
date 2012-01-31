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


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.DefaultPlace;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogonEvent;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectInvitationPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.SignUpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.SignUpView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.SignUpViewImpl;

public class SignUpPresenterImpl extends AbstractProfilePresenter implements SignUpPresenter, SplittableActivity {

	private final SignUpView view;
	private DefaultPlace afterSignupPlace;
	private String token;

	public SignUpPresenterImpl(SignUpView view) {
		super(view);
		this.view = view;
	}

	public SignUpPresenterImpl(SignUpView view, SignUpPlace place) {
		this(view);
		setPlace(place);
	}

	public SignUpPresenterImpl() {
		this(SignUpViewImpl.getInstance());
	}

	public void setPlace(Place aPlace) {
		SignUpPlace place = (SignUpPlace) aPlace;
		view.setPresenter(this); // This resets the view!

		this.token = place.getSignUpToken();
		boolean tokenRequired = place.isTokenRequired();

		afterSignupPlace = ProjectsDiscoverPlace.createPlace();

		if (place.getSignUpTokenData() != null) {
			view.setSignUpToken(place.getSignUpTokenData());
		} else if (place.getProjectInvitationTokenData() != null) {
			view.setProjectInvitationToken(place.getProjectInvitationTokenData());
			afterSignupPlace = ProjectInvitationPlace.createPlace(token);
		} else if (tokenRequired) {
			view.showSignUpInviteOnlyMessage();
		} else {
			view.setSignUpToken(null);
		}
		view.setGitHubProfileData(place.getGithubProfile());

	}

	@Override
	public void onStop() {
		view.setPresenter(null);
	}

	@Override
	public void onCancel() {
		view.setPresenter(null);
	}

	public void signup() {
		if (!passesBasicValidation()) {
			// If we encountered a basic error, bail out now before calling the server.
			return;
		}
		Profile profile = new Profile();
		profile.setUsername(view.getUsername().getValue());
		profile.setEmail(view.getEmail().getValue());
		profile.setFirstName(view.getFirstName().getValue());
		profile.setLastName(view.getLastName().getValue());
		profile.setPassword(view.getPassword().getValue());
		// this is a sign up token or a project invitation token.
		getProfileService().createProfileWithSignUpToken(profile, token, new AsyncCallbackSupport<UserInfo>() {

			@Override
			protected void success(UserInfo result) {
				AppGinjector.get.instance().getAppState().setHasPendingAgreements(result.getHasPendingAgreements());
				getEventBus().fireEvent(new LogonEvent(result.getCredentials()));
				afterSignupPlace.go();
			}
		});
	}

	@Override
	protected void bind() {

	}

	private boolean passesBasicValidation() {
		view.clearErrors();
		boolean isValid = true;

		String userPass = view.getPassword().getValue();
		if (userPass != null && userPass.length() > 0 && !userPass.equals(view.getPasswordConfirm().getValue())) {
			view.showPassMatchError();
			isValid = false;
		}

		return isValid;
	}

}
