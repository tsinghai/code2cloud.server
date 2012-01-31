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
package com.tasktop.c2c.server.tasks.shared;

import java.io.Serializable;


import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

public class QueryState implements Serializable {
	public enum QueryType {
		/** User specifies the query name. This can be resolved to a predefined or saved query. */
		Named, //
		Predefined, Saved, //
		Text, Criteria, //
		/** Navigation to tasks page without a particular query specified. Can resore state in this case. */
		Default
	};

	private QueryState.QueryType queryType;
	private String queryString;
	private PredefinedTaskQuery predefinedQuery;
	private SavedTaskQuery savedQuery;
	private QueryRequest queryRequest;

	public QueryState(QueryState.QueryType type, String string) {
		this.queryType = type;
		this.queryString = string;
	}

	public QueryState(PredefinedTaskQuery predefined) {
		this.queryType = QueryType.Predefined;
		this.predefinedQuery = predefined;
	}

	public QueryState(SavedTaskQuery saved) {
		this.queryType = QueryType.Saved;
		this.savedQuery = saved;
	}

	protected QueryState() {
		// For serialization
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString
	 *            the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @return the queryRequest
	 */
	public QueryRequest getQueryRequest() {
		return queryRequest;
	}

	/**
	 * @param queryRequest
	 *            the queryRequest to set
	 */
	public void setQueryRequest(QueryRequest queryRequest) {
		this.queryRequest = queryRequest;
	}

	/**
	 * @return the queryType
	 */
	public QueryState.QueryType getQueryType() {
		return queryType;
	}

	/**
	 * @param queryType
	 *            the queryType to set
	 */
	public void setQueryType(QueryState.QueryType queryType) {
		this.queryType = queryType;
	}

	/**
	 * @return the predefinedQuery
	 */
	public PredefinedTaskQuery getPredefinedQuery() {
		return predefinedQuery;
	}

	/**
	 * @param predefinedQuery
	 *            the predefinedQuery to set
	 */
	public void setPredefinedQuery(PredefinedTaskQuery predefinedQuery) {
		this.predefinedQuery = predefinedQuery;
	}

	/**
	 * @return the savedQuery
	 */
	public SavedTaskQuery getSavedQuery() {
		return savedQuery;
	}

	/**
	 * @param savedQuery
	 *            the savedQuery to set
	 */
	public void setSavedQuery(SavedTaskQuery savedQuery) {
		this.savedQuery = savedQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((predefinedQuery == null) ? 0 : predefinedQuery.hashCode());
		result = prime * result + ((queryRequest == null) ? 0 : queryRequest.hashCode());
		result = prime * result + ((queryString == null) ? 0 : queryString.hashCode());
		result = prime * result + ((queryType == null) ? 0 : queryType.hashCode());
		result = prime * result + ((savedQuery == null) ? 0 : savedQuery.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryState other = (QueryState) obj;
		if (predefinedQuery != other.predefinedQuery)
			return false;
		if (queryRequest == null) {
			if (other.queryRequest != null)
				return false;
		} else if (!queryRequest.equals(other.queryRequest))
			return false;
		if (queryString == null) {
			if (other.queryString != null)
				return false;
		} else if (!queryString.equals(other.queryString))
			return false;
		if (queryType != other.queryType)
			return false;
		if (savedQuery == null) {
			if (other.savedQuery != null)
				return false;
		} else if (!savedQuery.equals(other.savedQuery))
			return false;
		return true;
	}
}
