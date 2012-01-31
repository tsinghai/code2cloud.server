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
package com.tasktop.c2c.server.profile.web.client.place;


import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class SignInPlace extends AnonymousPlace implements WindowTitlePlace, HeadingPlace {

	private static final String AFTER_TOKEN = "?after=";

	public static PageMapping SignIn = new PageMapping(new SignInPlace.Tokenizer(), "signin", "signin" + AFTER_TOKEN
			+ "{after}");

	public static class Tokenizer implements PlaceTokenizer<SignInPlace> {

		@Override
		public SignInPlace getPlace(String token) {

			if (token.contains(AFTER_TOKEN)) {
				String afterUrl = token.substring(token.lastIndexOf(AFTER_TOKEN) + AFTER_TOKEN.length());
				if (afterUrl != null) {
					Place afterPlace = PageMapping.getPlaceForUrl(afterUrl);
					if (afterPlace != null) {
						return SignInPlace.createPlace((DefaultPlace) afterPlace);
					}
				}
			}

			return SignInPlace.createPlace();
		}

		@Override
		public String getToken(SignInPlace place) {
			return place.getToken();
		}
	}

	private final DefaultPlace afterSuccessfulSignIn;

	public DefaultPlace getAfterSuccessfulSignIn() {
		return afterSuccessfulSignIn;
	}

	@Override
	public String getHeading() {
		return "Sign In";
	}

	public String getPrefix() {
		if (afterSuccessfulSignIn != null && !(afterSuccessfulSignIn instanceof ProjectsDiscoverPlace)) {
			return SignIn.getUrl() + AFTER_TOKEN + afterSuccessfulSignIn.getPrefix();
		}
		return SignIn.getUrl();
	}

	private SignInPlace(DefaultPlace afterSuccessfulSignIn) {
		if (afterSuccessfulSignIn == null) {
			afterSuccessfulSignIn = ProjectsDiscoverPlace.createPlace();
		}
		this.afterSuccessfulSignIn = afterSuccessfulSignIn;
	}

	public static SignInPlace createPlace() {
		return new SignInPlace(ProjectsDiscoverPlace.createPlace());
	}

	public static SignInPlace createPlace(DefaultPlace afterSuccessfulSignIn) {
		if (afterSuccessfulSignIn == null) {
			afterSuccessfulSignIn = ProjectsDiscoverPlace.createPlace();
		}
		return new SignInPlace(afterSuccessfulSignIn);
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Sign In");
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		onPlaceDataFetched();
	}

}
