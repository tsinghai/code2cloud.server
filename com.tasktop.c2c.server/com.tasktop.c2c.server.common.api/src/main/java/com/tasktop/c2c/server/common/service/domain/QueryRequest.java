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
package com.tasktop.c2c.server.common.service.domain;

import java.io.Serializable;

/**
 * Query metadata that specifies the nature of the requested result set, supporting result pagination and sorting.
 * 
 * @author David Green
 */
public class QueryRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Region pageInfo;
	private SortInfo sortInfo;

	public QueryRequest() {
	}

	public QueryRequest(Region pageInfo, SortInfo sortInfo) {
		this.pageInfo = pageInfo;
		this.sortInfo = sortInfo;
	}

	public Region getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(Region pageInfo) {
		this.pageInfo = pageInfo;
	}

	public SortInfo getSortInfo() {
		return sortInfo;
	}

	public void setSortInfo(SortInfo sortInfo) {
		this.sortInfo = sortInfo;
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
		result = prime * result + ((pageInfo == null) ? 0 : pageInfo.hashCode());
		result = prime * result + ((sortInfo == null) ? 0 : sortInfo.hashCode());
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
		QueryRequest other = (QueryRequest) obj;
		if (pageInfo == null) {
			if (other.pageInfo != null)
				return false;
		} else if (!pageInfo.equals(other.pageInfo))
			return false;
		if (sortInfo == null) {
			if (other.sortInfo != null)
				return false;
		} else if (!sortInfo.equals(other.sortInfo))
			return false;
		return true;
	}

}
