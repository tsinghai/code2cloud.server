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
import com.tasktop.c2c.server.profile.web.client.place.AbstractBatchFetchingPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class HelpPlace extends AbstractBatchFetchingPlace implements HeadingPlace {

	public static class Tokenizer implements PlaceTokenizer<HelpPlace> {

		@Override
		public HelpPlace getPlace(String token) {
			return HelpPlace.createPlace();
		}

		@Override
		public String getToken(HelpPlace place) {
			return place.getToken();
		}
	}

	private HelpPlace() {

	}

	public static HelpPlace createPlace() {
		return new HelpPlace();
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		return PageMappings.Help.getUrl();
	}

	@Override
	public String getHeading() {
		return "Help";
	}

	protected void handleBatchResults() {
		onPlaceDataFetched();
	}

}
