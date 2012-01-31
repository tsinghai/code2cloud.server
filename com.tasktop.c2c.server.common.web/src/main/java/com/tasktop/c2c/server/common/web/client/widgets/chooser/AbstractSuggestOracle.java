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
package com.tasktop.c2c.server.common.web.client.widgets.chooser;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractSuggestOracle extends SuggestOracle {

	protected ValueSuggestionService suggestionService;

	public AbstractSuggestOracle(ValueSuggestionService suggestionService) {
		this.suggestionService = suggestionService;
	}

	@Override
	public boolean isDisplayStringHTML() {
		return false;
	}

	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		suggestionService.suggest(request.getQuery(), request.getLimit(), new ValueSuggestionService.Callback() {

			@Override
			public void onSuggestionsReady(List<?> values) {
				Response response = computeResponse(request, values);
				callback.onSuggestionsReady(request, response);
			}

		});
	}

	protected Response computeResponse(Request request, List<?> values) {
		Response response = new Response();
		List<Suggestion> suggestions = computeSuggestions(request, values);
		response.setSuggestions(suggestions);
		return response;
	}

	protected List<Suggestion> computeSuggestions(Request request, List<?> values) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>(values.size());
		for (Object value : values) {
			suggestions.add(createSuggestion(value));
		}
		return suggestions;
	}

	protected abstract Suggestion createSuggestion(Object value);

	@Override
	public void requestDefaultSuggestions(final Request request, final Callback callback) {
		suggestionService.suggest(new ValueSuggestionService.Callback() {

			@Override
			public void onSuggestionsReady(List<?> values) {
				Response response = computeResponse(request, values);
				callback.onSuggestionsReady(request, response);
			}
		});

	}

	/**
	 * @param suggestionService
	 *            the suggestionService to set
	 */
	public void setSuggestionService(ValueSuggestionService suggestionService) {
		this.suggestionService = suggestionService;
	}

}
