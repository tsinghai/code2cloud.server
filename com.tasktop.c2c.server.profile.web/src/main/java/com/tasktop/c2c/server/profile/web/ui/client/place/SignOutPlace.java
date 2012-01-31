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
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AbstractPlace;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEvent;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class SignOutPlace extends AbstractPlace {

	public static PageMapping SignOut = new PageMapping(new Tokenizer(), "signout");

	private static class Tokenizer implements PlaceTokenizer<SignOutPlace> {

		@Override
		public SignOutPlace getPlace(String token) {
			return SignOutPlace.createPlace();
		}

		@Override
		public String getToken(SignOutPlace place) {
			return place.getToken();
		}
	}

	@Override
	public String getPrefix() {
		return SignOut.getUrl();
	}

	public static SignOutPlace createPlace() {
		return new SignOutPlace();
	}

	public SignOutPlace() {

	}

	// Hacky work around the logic in AppPlaceController.
	boolean ready = false;

	@Override
	public void go() {
		if (!ready) {
			ready = true;
			ProfileGinjector.get.instance().getEventBus().fireEvent(new LogoutEvent());
		}
		ProfileGinjector.get.instance().getPlaceController().goTo(this);
	}

	@Override
	public boolean isReadyToGo() {
		return ready;
	}
}
