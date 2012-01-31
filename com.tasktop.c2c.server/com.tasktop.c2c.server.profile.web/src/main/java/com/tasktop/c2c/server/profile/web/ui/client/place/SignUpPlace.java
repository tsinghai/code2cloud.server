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

import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AnonymousPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.DefaultPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProfileDataFromGitubConnectionAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProfileResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectForInvitationTokenAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectInvitationTokenAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectInvitationTokenResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetSignupTokenAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetSignupTokenRequiredAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetSignupTokenRequiredResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetSignupTokenResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class SignUpPlace extends AnonymousPlace implements HeadingPlace, WindowTitlePlace {
	public static final String TOKEN = "token";

	public static class Tokenizer implements PlaceTokenizer<SignUpPlace> {

		@Override
		public SignUpPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String signupToken = pathArgs.getString(TOKEN);

			if (StringUtils.hasText(signupToken)) {
				return createPlace(signupToken);
			} else {
				return createPlace();
			}
		}

		@Override
		public String getToken(SignUpPlace place) {
			return place.getToken();
		}
	}

	private boolean tokenRequired;
	private final String signUpToken;
	private DefaultPlace postSignUpPlace;
	private SignUpToken signUpTokenData;
	private ProjectInvitationToken projectInvitationTokenData;
	private Project projectForInvitationToken;
	private Profile githubProfile;

	@Override
	public String getToken() {
		return null;
	}

	public String getPrefix() {
		if (StringUtils.hasText(signUpToken)) {
			LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();
			tokenMap.put(TOKEN, signUpToken);
			return PageMappings.SignUp.getUrlForNamedArgs(tokenMap);
		} else {
			return PageMappings.SignUp.getUrl();
		}
	}

	public DefaultPlace getPostSignUpPlace() {
		return postSignUpPlace;
	}

	public boolean isTokenRequired() {
		return tokenRequired;
	}

	public SignUpToken getSignUpTokenData() {
		return signUpTokenData;
	}

	public ProjectInvitationToken getProjectInvitationTokenData() {
		return projectInvitationTokenData;
	}

	public Profile getGithubProfile() {
		return githubProfile;
	}

	@Override
	public String getHeading() {
		return "Sign Up";
	}

	private SignUpPlace(String signUpToken, DefaultPlace postSignUpPlace) {
		if (postSignUpPlace == null) {
			postSignUpPlace = ProjectsDiscoverPlace.createPlace();
		}

		this.signUpToken = signUpToken;
		this.postSignUpPlace = postSignUpPlace;
	}

	public static SignUpPlace createPlace() {
		return new SignUpPlace(null, null);
	}

	public static SignUpPlace createPlace(String signUpToken) {
		return new SignUpPlace(signUpToken, null);
	}

	public static SignUpPlace createPlace(String signUpToken, DefaultPlace postSignUpPlace) {
		return new SignUpPlace(signUpToken, postSignUpPlace);
	}

	public String getSignUpToken() {
		return signUpToken;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetSignupTokenRequiredAction());
		if (signUpToken != null) {
			actions.add(new GetSignupTokenAction(signUpToken));
			actions.add(new GetProjectInvitationTokenAction(signUpToken));
			actions.add(new GetProjectForInvitationTokenAction(signUpToken));
		}
		actions.add(new GetProfileDataFromGitubConnectionAction());
	}

	protected boolean handleExceptionInResults() {
		return true;
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		tokenRequired = getResult(GetSignupTokenRequiredResult.class).get();

		GetSignupTokenResult signupTokenResult = getResult(GetSignupTokenResult.class);
		if (signupTokenResult != null) {
			signUpTokenData = signupTokenResult.get();
		}
		GetProjectInvitationTokenResult projectTokenResult = getResult(GetProjectInvitationTokenResult.class);
		if (projectTokenResult != null) {
			projectInvitationTokenData = projectTokenResult.get();
		}
		GetProjectResult projectResult = getResult(GetProjectResult.class);
		if (projectResult != null) {
			projectForInvitationToken = projectResult.get();
			postSignUpPlace = ProjectInvitationPlace.createPlace(signUpToken);
		}
		GetProfileResult gitHubProfileResult = getResult(GetProfileResult.class);
		if (gitHubProfileResult != null) {
			githubProfile = gitHubProfileResult.get();
		}

		if (tokenRequired && !StringUtils.hasText(signUpToken)) {
			ProjectsDiscoverPlace.createPlace()
					.displayOnArrival(Message.createErrorMessage("Token required for sign up.")).go();
			return;
		} else if (tokenRequired && projectInvitationTokenData == null && signUpTokenData == null) {
			ProjectsDiscoverPlace.createPlace()
					.displayOnArrival(Message.createErrorMessage("Invitation token is not valid.")).go();
			return;
		}

		onPlaceDataFetched();
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Sign Up");
	}

	/**
	 * @return the projectForInvitationToken
	 */
	public Project getProjectForInvitationToken() {
		return projectForInvitationToken;
	}

}
