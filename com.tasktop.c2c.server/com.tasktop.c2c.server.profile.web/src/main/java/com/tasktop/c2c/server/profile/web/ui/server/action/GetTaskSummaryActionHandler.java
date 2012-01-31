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

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.service.TaskService;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskSummaryAction;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskSummaryResult;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@Component
public class GetTaskSummaryActionHandler extends AbstractTaskActionHandler<GetTaskSummaryAction, GetTaskSummaryResult> {

	@Override
	public GetTaskSummaryResult execute(GetTaskSummaryAction action, ExecutionContext context) throws DispatchException {
		TaskService taskService = getService(action.getProjectIdentifier());
		Criteria criteria = CriteriaParser.parse(action.getCriteria());
		QuerySpec querySpec = new QuerySpec(action.getQueryRequest().getPageInfo(), action.getQueryRequest()
				.getSortInfo(), true);
		return new GetTaskSummaryResult(taskService.findTaskSummariesWithCriteria(criteria, querySpec).getResultPage());
	}
}
