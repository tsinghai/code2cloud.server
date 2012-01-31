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
package com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords;

import java.util.List;

import com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractSuggestOracle;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.domain.Keyword;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class KeywordSuggestOracle extends AbstractSuggestOracle {

	private final Boolean suggestNewValues;

	public KeywordSuggestOracle(KeywordSuggestService keywordService) {
		this(keywordService, true);
	}

	/**
	 * @param keywordService
	 * @param suggestNewValues
	 *            allow suggestion of new (non-existing tags) values
	 */
	public KeywordSuggestOracle(KeywordSuggestService keywordService, Boolean suggestNewValues) {
		super(keywordService);
		this.suggestNewValues = suggestNewValues;
	}

	/**
	 * Inject the current request as a suggestion if it is not found in the returned suggestions.
	 * 
	 * @see com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractSuggestOracle#computeSuggestions(com.google.gwt.user.client.ui.SuggestOracle.Request,
	 *      java.util.List)
	 */
	@Override
	protected List<Suggestion> computeSuggestions(Request request, List<?> values) {
		final List<Suggestion> suggestions = super.computeSuggestions(request, values);
		String query = request.getQuery();
		if (query != null && query.trim().length() > 0) {
			Boolean found = false;
			for (Suggestion suggestion : suggestions) {
				if (((KeywordSuggestion) suggestion).getValue().getName().equals(request.getQuery())) {
					found = true;
					break;
				}
			}

			// If we allow new (non-existing) suggestions inject it here
			if (!found && suggestNewValues) {
				suggestions.add(new KeywordSuggestion(new Keyword(request.getQuery(), null)));
			}
		}
		return suggestions;
	}

	@Override
	protected Suggestion createSuggestion(Object value) {
		return new KeywordSuggestion((Keyword) value);
	}
}
