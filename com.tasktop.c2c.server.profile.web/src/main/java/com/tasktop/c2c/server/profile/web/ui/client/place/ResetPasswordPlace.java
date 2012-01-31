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
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AnonymousPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.shared.actions.CheckPasswordResetTokenAction;
import com.tasktop.c2c.server.profile.web.shared.actions.CheckPasswordResetTokenResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class ResetPasswordPlace extends AnonymousPlace implements HeadingPlace {

	public static class Tokenizer implements PlaceTokenizer<ResetPasswordPlace> {

		@Override
		public ResetPasswordPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);
			return createPlace(pathArgs.getString(SignUpPlace.TOKEN));
		}

		@Override
		public String getToken(ResetPasswordPlace place) {
			return place.getToken();
		}
	}

	public static ResetPasswordPlace createPlace(String resetToken) {
		return new ResetPasswordPlace(resetToken);
	}

	private String resetToken;

	private ResetPasswordPlace(String resetToken) {
		this.resetToken = resetToken;
	}

	public String getResetToken() {
		return resetToken;
	}

	@Override
	public String getHeading() {
		return "Password Reset";
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();
		tokenMap.put(SignUpPlace.TOKEN, resetToken);
		return PageMappings.ResetPassword.getUrlForNamedArgs(tokenMap);
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new CheckPasswordResetTokenAction(resetToken));

	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();

		CheckPasswordResetTokenResult result = getResult(CheckPasswordResetTokenResult.class);
		if (!result.get()) {
			SignInPlace.createPlace().setMessage(getMessage()).go();
		} else {
			onPlaceDataFetched();
		}
	}

}
