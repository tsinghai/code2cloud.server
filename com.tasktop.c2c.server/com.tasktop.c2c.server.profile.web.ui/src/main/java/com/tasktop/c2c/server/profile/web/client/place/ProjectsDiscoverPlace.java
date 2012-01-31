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

import java.util.LinkedHashMap;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class ProjectsDiscoverPlace extends AbstractBatchFetchingPlace implements HeadingPlace, WindowTitlePlace {

	public static final String QUERY = "query";

	public static PageMapping Discover = new PageMapping(new ProjectsDiscoverPlace.Tokenizer(), "projects", "search/{"
			+ QUERY + "}");

	public static class Tokenizer implements PlaceTokenizer<ProjectsDiscoverPlace> {

		@Override
		public ProjectsDiscoverPlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			// Only one of the following three can be defined at a time, so use this to determine which place to create.
			String query = pathArgs.getString(QUERY);

			if (StringUtils.hasText(query)) {
				// We have a query string.
				return createPlaceForQuery(query);
			} else {
				return createPlace();
			}
		}

		@Override
		public String getToken(ProjectsDiscoverPlace place) {
			return place.getToken();
		}
	}

	private final String query;
	private final boolean assumeUserIsAnonymous;

	private ProjectsDiscoverPlace(boolean assumeUserIsAnon) {
		this.assumeUserIsAnonymous = assumeUserIsAnon;
		query = null;
	}

	private ProjectsDiscoverPlace(String query) {
		this.query = query;
		assumeUserIsAnonymous = false;
	}

	@Override
	public String getHeading() {
		return "Discover Projects";
	}

	@Override
	public String getToken() {
		return "";
	}

	public static ProjectsDiscoverPlace createPlace() {
		return new ProjectsDiscoverPlace(false);
	}

	public static ProjectsDiscoverPlace createPlaceForQuery(String query) {
		return new ProjectsDiscoverPlace(query);
	}

	public static ProjectsDiscoverPlace createPlaceForAfterLogout() {
		return new ProjectsDiscoverPlace(true);
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		if (query != null) {
			tokenMap.put(QUERY, query);
		}

		return Discover.getUrlForNamedArgs(tokenMap);
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Browse Projects");
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		if (assumeUserIsAnonymous) {
			ProfileGinjector.get.instance().getAppState().setCredentials(null);
		}
		onPlaceDataFetched();
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
}
