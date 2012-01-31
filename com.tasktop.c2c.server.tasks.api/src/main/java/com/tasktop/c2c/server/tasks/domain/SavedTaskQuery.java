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
package com.tasktop.c2c.server.tasks.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;

/**
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * 
 */
public class SavedTaskQuery extends AbstractDomainObject {
	private static final long serialVersionUID = 1L;

	public enum Access {
		QUERY_OWNER, ALL, MEMBERS, OWNERS
	};

	private String queryString;
	private String name;
	private SortInfo defaultSort;
	private Access readAccess;
	private Access writeAccess;

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	@JsonIgnore
	public Criteria getQueryCriteria() {
		if (queryString == null) {
			return null;
		}
		return CriteriaParser.parse(queryString);
	}

	/**
	 * @param queryString
	 *            the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the defaultSort
	 */
	public SortInfo getDefaultSort() {
		return defaultSort;
	}

	/**
	 * @param defaultSort
	 *            the defaultSort to set
	 */
	public void setDefaultSort(SortInfo defaultSort) {
		this.defaultSort = defaultSort;
	}

	/**
	 * @return the readAccess
	 */
	public Access getReadAccess() {
		return readAccess;
	}

	/**
	 * @param readAccess
	 *            the readAccess to set
	 */
	public void setReadAccess(Access readAccess) {
		this.readAccess = readAccess;
	}

	/**
	 * @return the writeAccess
	 */
	public Access getWriteAccess() {
		return writeAccess;
	}

	/**
	 * @param writeAccess
	 *            the writeAccess to set
	 */
	public void setWriteAccess(Access writeAccess) {
		this.writeAccess = writeAccess;
	}

}
