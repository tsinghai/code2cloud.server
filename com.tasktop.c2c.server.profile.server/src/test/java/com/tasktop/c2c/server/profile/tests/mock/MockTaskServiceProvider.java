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
package com.tasktop.c2c.server.profile.tests.mock;


import com.tasktop.c2c.server.profile.service.provider.TaskServiceProvider;
import com.tasktop.c2c.server.tasks.service.TaskService;


public class MockTaskServiceProvider extends TaskServiceProvider {

	private static TaskService taskService;

	public static void setTaskService(TaskService taskService) {
		MockTaskServiceProvider.taskService = taskService;
	}

	@Override
	public TaskService getTaskService(String projectIdentifier) {
		return taskService;
	}
}
