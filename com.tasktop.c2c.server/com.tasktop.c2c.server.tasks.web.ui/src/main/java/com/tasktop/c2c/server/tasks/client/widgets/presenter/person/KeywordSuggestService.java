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
package com.tasktop.c2c.server.tasks.client.widgets.presenter.person;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.ValueSuggestionService;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationAction;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationResult;

public class KeywordSuggestService implements ValueSuggestionService {

	private String projectIdentifier;

	public KeywordSuggestService() {
	}

	public KeywordSuggestService(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	public String getProjectIdentifier() {
		return projectIdentifier;
	}

	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public void suggest(final String query, final int limit, final Callback callback) {
		CommonGinjector.get
				.instance()
				.getDispatchService()
				.execute(new GetRepositoryConfigurationAction(projectIdentifier),
						new AsyncCallback<GetRepositoryConfigurationResult>() {

							@Override
							public void onFailure(Throwable caught) {
								callback.onSuggestionsReady(new ArrayList<Keyword>());
							}

							@Override
							public void onSuccess(GetRepositoryConfigurationResult result) {
								String queryString = query.trim().toLowerCase();
								List<Keyword> results = new ArrayList<Keyword>(limit);
								for (Keyword keyword : result.get().getKeywords()) {
									if (keyword.getName().toLowerCase().contains(queryString)) {
										results.add(keyword);
									}
									if (results.size() > limit) {
										break;
									}
								}
								callback.onSuggestionsReady(results);
							}
						});

	}

	@Override
	public void suggest(final Callback callback) {
		CommonGinjector.get
				.instance()
				.getDispatchService()
				.execute(new GetRepositoryConfigurationAction(projectIdentifier),
						new AsyncCallback<GetRepositoryConfigurationResult>() {

							@Override
							public void onFailure(Throwable caught) {
								callback.onSuggestionsReady(new ArrayList<Keyword>());
							}

							@Override
							public void onSuccess(GetRepositoryConfigurationResult result) {
								callback.onSuggestionsReady(result.get().getKeywords());
							}
						});
	}

}
