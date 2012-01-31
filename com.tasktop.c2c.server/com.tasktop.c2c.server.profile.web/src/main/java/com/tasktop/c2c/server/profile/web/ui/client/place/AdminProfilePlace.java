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

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.LoggedInPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.ListProfilesAction;
import com.tasktop.c2c.server.profile.web.shared.actions.ListProfilesResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

public class AdminProfilePlace extends LoggedInPlace implements HeadingPlace, WindowTitlePlace {

	private List<Profile> profiles;

	@Override
	public String getHeading() {
		return "Administer Users";
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Administer Users");
	}

	public static class Tokenizer implements PlaceTokenizer<AdminProfilePlace> {

		@Override
		public AdminProfilePlace getPlace(String token) {
			return AdminProfilePlace.createPlace();
		}

		@Override
		public String getToken(AdminProfilePlace place) {
			return place.getToken();
		}
	}

	public static AdminProfilePlace createPlace() {
		return new AdminProfilePlace();
	}

	private AdminProfilePlace() {
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		return PageMappings.AdminProfiles.getUrl();
	}

	/** Override to add more actions. Don't forget to call super. */
	protected void addActions(List<Action<?>> actions) {
		actions.add(new ListProfilesAction());
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		profiles = getResult(ListProfilesResult.class).get();
		onPlaceDataFetched();
	}

	/**
	 * @return the profiles
	 */
	public List<Profile> getProfiles() {
		return profiles;
	}

}
