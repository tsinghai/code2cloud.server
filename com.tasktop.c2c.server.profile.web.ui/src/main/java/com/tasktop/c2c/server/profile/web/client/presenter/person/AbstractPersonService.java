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
package com.tasktop.c2c.server.profile.web.client.presenter.person;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.ValueSuggestionService.Callback;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.ProfileServiceAsync;
import com.tasktop.c2c.server.profile.web.shared.Profile;

public class AbstractPersonService {

	public static class ProfilesCallback implements AsyncCallback<List<Profile>> {
		private final Callback callback;

		public ProfilesCallback(Callback callback) {
			this.callback = callback;
		}

		@Override
		public void onSuccess(List<Profile> result) {
			callback.onSuggestionsReady(toPeople(result));
		}

		@Override
		public void onFailure(Throwable caught) {
			callback.onSuggestionsReady(new ArrayList<Person>());
		}
	}

	public AbstractPersonService() {
		super();
	}

	public static List<Person> toPeople(List<Profile> profiles) {
		List<Person> people = new ArrayList<Person>(profiles.size());
		for (Profile userProfile : profiles) {
			people.add(PersonUtil.toPerson(userProfile));
		}
		return people;
	}

	protected ProfileServiceAsync getProfileService() {
		return ProfileGinjector.get.instance().getProfileService();
	}

}
