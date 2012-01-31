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

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.common.web.shared.CachableReadAction;
import com.tasktop.c2c.server.tasks.shared.QueryState;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskQueryAction implements Action<TaskQueryResult>, CachableReadAction {
	private String projectId;
	private QueryState queryState;

	public TaskQueryAction(String projectId, QueryState state) {
		this.projectId = projectId;
		this.queryState = state;
	}

	protected TaskQueryAction() {

	}

	/**
	 * @return the queryState
	 */
	public QueryState getQueryState() {
		return queryState;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
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
		result = prime * result + ((queryState == null) ? 0 : queryState.hashCode());
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
		TaskQueryAction other = (TaskQueryAction) obj;
		if (projectId == null) {
			if (other.projectId != null)
				return false;
		} else if (!projectId.equals(other.projectId))
			return false;
		if (queryState == null) {
			if (other.queryState != null)
				return false;
		} else if (!queryState.equals(other.queryState))
			return false;
		return true;
	}

}
