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
import java.util.List;

/**
 * Contains the a page's worth of results from a query.
 * 
 * @param <T>
 *            the type of result
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class QueryResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer offset;
	private Integer totalResultSize;
	private Integer pageSize;
	private List<T> resultPage;

	public QueryResult() {
		// no-op
	}

	public QueryResult(Region pageInfo, List<T> resultPage, int totalResultSize) {
		this.offset = pageInfo.getOffset();
		this.pageSize = pageInfo.getSize();
		this.totalResultSize = totalResultSize;
		this.resultPage = resultPage;
	}

	public QueryResult(Integer offset, Integer pageSize, List<T> resultPage, Integer totalResultSize) {
		this.offset = offset;
		this.pageSize = pageSize;
		this.resultPage = resultPage;
		this.totalResultSize = totalResultSize;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getTotalResultSize() {
		return totalResultSize;
	}

	public void setTotalResultSize(Integer totalResultSize) {
		this.totalResultSize = totalResultSize;
	}

	public List<T> getResultPage() {
		return resultPage;
	}

	public void setResultPage(List<T> resultPage) {
		this.resultPage = resultPage;
	}

	@Override
	public String toString() {
		return "QueryResult [offset=" + offset + ", totalResultSize=" + totalResultSize + ", pageSize=" + pageSize
				+ ", resultPage.size=" + resultPage.size() + "]";
	}
}
