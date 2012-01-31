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
package com.tasktop.c2c.server.tasks.shared.action;

import java.io.Serializable;

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.web.shared.CachableReadAction;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class GetTaskSummaryAction implements Action<GetTaskSummaryResult>, CachableReadAction, Serializable {
	private static final long serialVersionUID = 1L;
	private String projectIdentifier;
	private String criteria;
	private QueryRequest queryRequest;

	public GetTaskSummaryAction(String projectIdentifier, String criteria, QueryRequest queryRequest) {
		this.projectIdentifier = projectIdentifier;
		this.criteria = criteria;
		this.queryRequest = queryRequest;
	}

	protected GetTaskSummaryAction() {
	}

	public QueryRequest getQueryRequest() {
		return queryRequest;
	}

	public String getCriteria() {
		return criteria;
	}

	public String getProjectIdentifier() {
		return projectIdentifier;
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
		result = prime * result + ((projectIdentifier == null) ? 0 : projectIdentifier.hashCode());
		result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
		result = prime * result + ((queryRequest == null) ? 0 : queryRequest.hashCode());
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
		GetTaskSummaryAction other = (GetTaskSummaryAction) obj;
		if (projectIdentifier == null) {
			if (other.projectIdentifier != null)
				return false;
		} else if (!projectIdentifier.equals(other.projectIdentifier))
			return false;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
			return false;
		if (queryRequest == null) {
			if (other.queryRequest != null)
				return false;
		} else if (!queryRequest.equals(other.queryRequest))
			return false;
		return true;
	}

}
