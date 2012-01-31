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
package com.tasktop.c2c.server.internal.tasks.service;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.internal.tasks.domain.Priority;
import com.tasktop.c2c.server.internal.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;


@Component
class TaskSortFieldMapper {

	public static final class QueryParts {
		private final String selectPart;
		private final String fromPart;
		private final String wherePart;
		private final String orderByPart;

		public QueryParts(String selectPart, String fromPart, String wherePart, String orderByPart) {
			this.selectPart = selectPart;
			this.fromPart = fromPart;
			this.wherePart = wherePart;
			this.orderByPart = orderByPart;
		}

		public String getSelectPart() {
			return selectPart;
		}

		public String getWherePart() {
			return wherePart;
		}

		public String getOrderByPart() {
			return orderByPart;
		}

		public String getFromPart() {
			return fromPart;
		}
	}

	public QueryParts mapSortFieldToDB(SortInfo sortInfo) {

		String taskField = sortInfo.getSortField();

		if (taskField.equals(TaskFieldConstants.SEVERITY_FIELD)) {
			return new QueryParts(", sev", ", " + TaskSeverity.class.getSimpleName() + " sev",
					"sev.value = task.severity", makeOrderBy("sev.sortkey", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.CREATION_TIME_FIELD)) {
			return new QueryParts("", "", "", makeOrderBy("task.creationTs", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.LAST_UPDATE_FIELD)) {
			return new QueryParts("", "", "", makeOrderBy("task.deltaTs", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.TASK_ID_FIELD)) {
			return new QueryParts("", "", "", makeOrderBy("task.id", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.SUMMARY_FIELD)) {
			return new QueryParts("", "", "", makeOrderBy("task.shortDesc", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.STATUS_FIELD)) {
			return new QueryParts(", stat", ", " + TaskStatus.class.getSimpleName() + " stat",
					"stat.value = task.status", makeOrderBy("stat.sortkey", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.PRIORITY_FIELD)) {
			return new QueryParts(", priority", ", " + Priority.class.getSimpleName() + " priority",
					"priority.value = task.priority", makeOrderBy("priority.sortkey", sortInfo));
		} else if (taskField.equals(TaskFieldConstants.ASSIGNEE_FIELD)) {
			return new QueryParts("", "", "", makeOrderBy("task.assignee.loginName", sortInfo));
		}

		else {
			throw new IllegalArgumentException("Unknown sort field" + taskField);
		}
	}

	private String makeOrderBy(String dBField, SortInfo sortInfo) {
		return " order by " + dBField + (sortInfo.getSortOrder() == SortInfo.Order.ASCENDING ? "" : " DESC");

	}
}
