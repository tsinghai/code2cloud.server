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
package com.tasktop.c2c.server.profile.web.ui.server.action;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.service.TaskService;
import com.tasktop.c2c.server.tasks.shared.QueryState;
import com.tasktop.c2c.server.tasks.shared.action.TaskQueryAction;
import com.tasktop.c2c.server.tasks.shared.action.TaskQueryResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class TaskQueryActionHandler extends AbstractTaskActionHandler<TaskQueryAction, TaskQueryResult> {
	@Override
	public TaskQueryResult execute(TaskQueryAction action, ExecutionContext context) throws DispatchException {
		String projectIdentifier = action.getProjectId();
		QueryState queryState = action.getQueryState();
		QuerySpec querySpec = new QuerySpec(queryState.getQueryRequest().getPageInfo(), queryState.getQueryRequest()
				.getSortInfo());

		TaskService service = getService(projectIdentifier);

		switch (queryState.getQueryType()) {
		case Criteria:
			return new TaskQueryResult(service.findTasksWithCriteria(CriteriaParser.parse(queryState.getQueryString()),
					querySpec));
		case Text:
			return new TaskQueryResult(service.findTasks(queryState.getQueryString(), querySpec));
		case Predefined:
			return new TaskQueryResult(service.findTasksWithQuery(queryState.getPredefinedQuery(), querySpec));
		case Saved:
			return new TaskQueryResult(service.findTasksWithCriteria(queryState.getSavedQuery().getQueryCriteria(),
					querySpec));
		default:
			throw new IllegalStateException();
		}

	}

}
