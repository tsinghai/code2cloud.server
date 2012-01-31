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

/**
 * Holds constants used to talk about fields of tasks.
 * 
 */
public final class TaskFieldConstants {
	private TaskFieldConstants() {
		// static helper
	}

	public static final String TASK_ID_FIELD = "taskId";
	public static final String TASK_TYPE_FIELD = "tasktype";
	public static final String ITERATION_FIELD = "iteration";
	public static final String SUMMARY_FIELD = "summary";
	public static final String DESCRIPTION_FIELD = "description";
	public static final String COMMENT_FIELD = "comment";
	public static final String STATUS_FIELD = "status";
	public static final String PRIORITY_FIELD = "priority";
	public static final String SEVERITY_FIELD = "severity";
	public static final String RESOLUTION_FIELD = "resolution";
	public static final String ASSIGNEE_FIELD = "assignee";
	public static final String REPORTER_FIELD = "reporter";
	public static final String WATCHER_FIELD = "watcher";
	public static final String CREATION_TIME_FIELD = "creationDate";
	public static final String LAST_UPDATE_FIELD = "modificationDate";
	public static final String PRODUCT_FIELD = "product";
	public static final String COMPONENT_FIELD = "component";
	public static final String PRODUCT_NAME_FIELD = "productName";
	public static final String COMPONENT_NAME_FIELD = "componentName";
	public static final String MILESTONE_FIELD = "release";
	public static final String COMMENT_AUTHOR_FIELD = "commentAuthor";
	public static final String KEYWORDS_FIELD = "keywords";
	public static final String EXTERNAL_TASK_RELATIONS_FIELD = "task_relations";
}
