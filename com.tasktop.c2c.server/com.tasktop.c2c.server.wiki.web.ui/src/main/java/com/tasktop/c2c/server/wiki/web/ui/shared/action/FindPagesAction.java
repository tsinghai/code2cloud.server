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
package com.tasktop.c2c.server.wiki.web.ui.shared.action;

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.web.shared.CachableReadAction;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class FindPagesAction implements Action<FindPagesResult>, CachableReadAction {

	private String projectId;
	private String searchTerm;
	private QueryRequest queryReuest;

	public FindPagesAction(String projectId, String searchTerm, QueryRequest queryRequest) {
		this.projectId = projectId;
		this.searchTerm = searchTerm;
		this.queryReuest = queryRequest;
	}

	protected FindPagesAction() {

	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @return the searchTerm
	 */
	public String getSearchTerm() {
		return searchTerm;
	}

	/**
	 * @return the queryReuest
	 */
	public QueryRequest getQueryReuest() {
		return queryReuest;
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
		result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
		result = prime * result + ((queryReuest == null) ? 0 : queryReuest.hashCode());
		result = prime * result + ((searchTerm == null) ? 0 : searchTerm.hashCode());
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
		FindPagesAction other = (FindPagesAction) obj;
		if (projectId == null) {
			if (other.projectId != null)
				return false;
		} else if (!projectId.equals(other.projectId))
			return false;
		if (queryReuest == null) {
			if (other.queryReuest != null)
				return false;
		} else if (!queryReuest.equals(other.queryReuest))
			return false;
		if (searchTerm == null) {
			if (other.searchTerm != null)
				return false;
		} else if (!searchTerm.equals(other.searchTerm))
			return false;
		return true;
	}

}
