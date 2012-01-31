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
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.LoggedInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class InvitationCreatorPlace extends LoggedInPlace implements HeadingPlace {

	@Override
	public String getHeading() {
		return "Invitation Creator";
	}

	public static class Tokenizer implements PlaceTokenizer<InvitationCreatorPlace> {

		@Override
		public InvitationCreatorPlace getPlace(String token) {
			return InvitationCreatorPlace.createPlace();
		}

		@Override
		public String getToken(InvitationCreatorPlace place) {
			return place.getToken();
		}
	}

	public static InvitationCreatorPlace createPlace() {
		return new InvitationCreatorPlace();
	}

	private InvitationCreatorPlace() {

	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		return PageMappings.InvitationCreator.getUrl();
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		onPlaceDataFetched();
	}

}
