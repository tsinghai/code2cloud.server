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


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.profile.web.client.place.AnonymousPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class RequestPasswordResetPlace extends AnonymousPlace implements HeadingPlace, WindowTitlePlace {

	public static class Tokenizer implements PlaceTokenizer<RequestPasswordResetPlace> {

		@Override
		public RequestPasswordResetPlace getPlace(String token) {
			return RequestPasswordResetPlace.createPlace();
		}

		@Override
		public String getToken(RequestPasswordResetPlace place) {
			return place.getToken();
		}
	}

	public static RequestPasswordResetPlace createPlace() {
		return new RequestPasswordResetPlace();
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
		return PageMappings.RequestPasswordReset.getUrl();
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Password Reset");
	}

	protected void handleBatchResults() {
		super.onPlaceDataFetched();
	}

}
