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
package com.tasktop.c2c.server.wiki.service;

import com.tasktop.c2c.server.common.service.domain.QueryRequest;

public class SearchTermQueryArguments {
	private String searchTerm;

	private QueryRequest queryRequest;

	public SearchTermQueryArguments() {
		// Default constructor, does nothing.
	}

	public SearchTermQueryArguments(String searchTerm, QueryRequest queryRequest) {
		setSearchTerm(searchTerm);
		setQueryRequest(queryRequest);
	}

	public QueryRequest getQueryRequest() {
		return queryRequest;
	}

	public void setQueryRequest(QueryRequest queryRequest) {
		this.queryRequest = queryRequest;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
}
