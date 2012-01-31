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
package com.tasktop.c2c.server.common.service.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Qualifier;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.query.PagingStrategy;
import com.tasktop.c2c.server.common.service.query.QueryResultConverter;



/**
 * Use the JPA query interface to do paging. NOTE this does not work with eclipselink JPA and hypersonic.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 * @param <T>
 */
// @Component Don't expose this for autowiring as it is broken in test
@Qualifier("queryPagingStrategy")
public class QueryPagingStrategy<T> implements PagingStrategy<T> {

	@SuppressWarnings("unchecked")
	@Override
	public List<T> extractPageFromQuery(Region pageInfo, Query query, QueryResultConverter<T> resultConverter) {
		query.setFirstResult(pageInfo.getOffset());
		query.setMaxResults(pageInfo.getSize());

		List<T> resultList = new ArrayList<T>(pageInfo.getSize());
		for (Object[] queryResult : (List<Object[]>) query.getResultList()) {
			resultList.add(resultConverter.mapToDomainObject(queryResult));
		}

		return resultList;
	}

}
