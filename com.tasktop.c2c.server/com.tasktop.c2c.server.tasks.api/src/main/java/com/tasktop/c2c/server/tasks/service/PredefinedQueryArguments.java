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
package com.tasktop.c2c.server.tasks.service;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;

public class PredefinedQueryArguments {
	private PredefinedTaskQuery predefinedTaskQuery;
	private QuerySpec querySpec;

	public PredefinedQueryArguments() {
		// Default constructor, does nothing.
	}

	public PredefinedQueryArguments(PredefinedTaskQuery predefinedTaskQuery, QuerySpec querySpec) {
		setPredefinedTaskQuery(predefinedTaskQuery);
		setQuerySpec(querySpec);
	}

	public PredefinedTaskQuery getPredefinedTaskQuery() {
		return predefinedTaskQuery;
	}

	public void setPredefinedTaskQuery(PredefinedTaskQuery predefinedTaskQuery) {
		this.predefinedTaskQuery = predefinedTaskQuery;
	}

	public QuerySpec getQuerySpec() {
		return querySpec;
	}

	public void setQuerySpec(QuerySpec querySpec) {
		this.querySpec = querySpec;
	}
}
