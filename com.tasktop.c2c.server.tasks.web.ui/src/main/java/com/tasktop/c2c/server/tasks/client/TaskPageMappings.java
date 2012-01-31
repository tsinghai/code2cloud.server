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
package com.tasktop.c2c.server.tasks.client;


import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminCustomFieldsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminIterationsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminKeywordsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminProductsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminTasksPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectEditTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectNewTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskHistoryPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryListPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryPlace;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskPageMappings {

	public static PageMapping ProjectAdminTasks = ProjectAdminTasksPlace.ProjectTaskAdmin;
	public static PageMapping ProjectTask = ProjectTaskPlace.ProjectTask;
	public static PageMapping ProjectTaskHistory = ProjectTaskHistoryPlace.ProjectTaskHistory;
	public static PageMapping ProjectEditTask = ProjectEditTaskPlace.ProjectEditTask;
	public static PageMapping ProjectAdminTasksKeywords = ProjectAdminKeywordsPlace.ProjectTaskAdminKeywords;
	public static PageMapping ProjectAdminTasksProducts = ProjectAdminProductsPlace.ProjectTaskAdminProducts;
	public static PageMapping ProjectAdminTasksIterations = ProjectAdminIterationsPlace.ProjectTaskAdminIterations;
	public static PageMapping ProjectAdminTasksCustomFields = ProjectAdminCustomFieldsPlace.ProjectTaskAdminCustomFields;
	public static PageMapping ProjectNewTask = new PageMapping(new ProjectNewTaskPlace.Tokenizer(), Path.PROJECT_BASE
			+ "/{" + Path.PROJECT_ID + "}/task/new", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/task/{"
			+ ProjectNewTaskPlace.PARENT_TASK_ID + ":Integer}/newSubtask");
	public static PageMapping ProjectTasks = new PageMapping(new ProjectTasksPlace.Tokenizer(), Path.PROJECT_BASE
			+ "/{" + Path.PROJECT_ID + "}/tasks", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/{"
			+ ProjectTasksPlace.NAMED_Q + "}", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/q/{"
			+ ProjectTasksPlace.TEXT_Q + ":*}", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/s/{"
			+ ProjectTasksPlace.CRIT_Q + ":*}");
	public static PageMapping ProjectTaskSummaryList = new PageMapping(new ProjectTasksSummaryListPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/summary/products", Path.PROJECT_BASE + "/{"
					+ Path.PROJECT_ID + "}/tasks/summary/product/{" + ProjectTasksSummaryListPlace.PRODUCT
					+ ":Integer}", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/summary/product/{"
					+ ProjectTasksSummaryListPlace.PRODUCT + ":Integer}"
					+ ProjectTasksSummaryListPlace.COMPONENT_SUFFIX, Path.PROJECT_BASE + "/{" + Path.PROJECT_ID
					+ "}/tasks/summary/product/{" + ProjectTasksSummaryListPlace.PRODUCT + ":Integer}"
					+ ProjectTasksSummaryListPlace.RELEASE_SUFFIX);
	public static PageMapping ProjectTaskSummary = new PageMapping(new ProjectTasksSummaryPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/summary/product/{"
					+ ProjectTasksSummaryListPlace.PRODUCT + ":Integer}/release/{" + ProjectTasksSummaryPlace.RELEASE
					+ "}", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/tasks/summary/product/{"
					+ ProjectTasksSummaryListPlace.PRODUCT + ":Integer}/component/{"
					+ ProjectTasksSummaryPlace.COMPONENT + ":Integer}", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID
					+ "}/tasks/summary/product/{" + ProjectTasksSummaryListPlace.PRODUCT + ":Integer}/component/{"
					+ ProjectTasksSummaryPlace.COMPONENT + ":Integer}/release/{" + ProjectTasksSummaryPlace.RELEASE
					+ "}");

}
