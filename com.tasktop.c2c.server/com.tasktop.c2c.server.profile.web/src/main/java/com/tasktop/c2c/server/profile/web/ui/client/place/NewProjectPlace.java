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
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.LoggedInPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectCreateAvailableAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectCreateAvailableResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class NewProjectPlace extends LoggedInPlace implements HeadingPlace, WindowTitlePlace {

	private boolean createAvailable = false;

	public static class Tokenizer implements PlaceTokenizer<NewProjectPlace> {

		@Override
		public NewProjectPlace getPlace(String token) {
			return NewProjectPlace.createPlace();
		}

		@Override
		public String getToken(NewProjectPlace place) {
			return place.getToken();
		}
	}

	@Override
	public String getHeading() {
		return "Create Project";
	}

	private NewProjectPlace() {
	}

	public static NewProjectPlace createPlace() {
		return new NewProjectPlace();
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		return PageMappings.NewProject.getUrl();
	}

	public boolean isCreateAvailable() {
		return createAvailable;
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Create Project");
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProjectCreateAvailableAction());
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		createAvailable = getResult(GetProjectCreateAvailableResult.class).get();
		onPlaceDataFetched();
	}
}
