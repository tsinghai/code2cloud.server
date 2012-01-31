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
package com.tasktop.c2c.server.monitoring.service.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.monitoring.domain.ErrorEvent;
import com.tasktop.c2c.server.monitoring.domain.MonitoringEvent;
import com.tasktop.c2c.server.monitoring.domain.SlowResponseEvent;
import com.tasktop.c2c.server.monitoring.service.MonitoringService;
import com.tasktop.c2c.server.monitoring.service.MonitoringServiceClient;
import com.tasktop.c2c.server.profile.service.provider.TaskServiceProvider;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.service.TaskService;


public class MonitioringServiceImpl implements MonitoringService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitioringServiceImpl.class.getName());

	private static final TaskUserProfile ASSIGNEE = new TaskUserProfile();

	private static final long MIN_UPDATE_PERIOD = 10 * 60 * 1000;
	static {
		ASSIGNEE.setLoginName("default-assignee");
		ASSIGNEE.setRealname("Default Assignee");
	}

	private TaskServiceProvider taskServiceProvider;

	// TODO, maybe make this async.
	@Override
	public void processEvent(MonitoringEvent event) {
		TaskService taskService = getTaskService(MonitoringServiceClient.getProjectIdentifier(event));
		String taskTitle = computeTaskTitle(event);
		Task existingTask = findOpenTaskWithTitle(taskService, taskTitle);
		if (existingTask == null) {
			createNewTask(taskService, taskTitle, event);
		} else {
			maybeUpdateTask(taskService, existingTask, event);
		}
	}

	private void maybeUpdateTask(TaskService taskService, Task existingTask, MonitoringEvent event) {
		if (System.currentTimeMillis() - existingTask.getModificationDate().getTime() < MIN_UPDATE_PERIOD) {
			LOGGER.info("Got event for task [" + existingTask.getId() + "], but is has been recently updated. Ignoring");
			return;
		}
		existingTask.addComment(computeTaskComment(event));
		try {
			taskService.updateTask(existingTask);
		} catch (Exception e) {
			LOGGER.warn("Could not update task [" + existingTask.getId() + "] reason: " + e.getMessage());
		}
	}

	private void createNewTask(TaskService taskService, String taskTitle, MonitoringEvent event) {
		RepositoryConfiguration repoConfig = taskService.getRepositoryContext();

		Task newTask = new Task();
		newTask.setTaskType(repoConfig.getDefaultType());
		newTask.setShortDescription(taskTitle);
		// FIXME How to make decision on these fields
		newTask.setProduct(repoConfig.getDefaultProduct());
		newTask.setComponent(repoConfig.getDefaultProduct().getDefaultComponent());
		newTask.setMilestone(repoConfig.getMilestones().get(
				repoConfig.getMilestones().indexOf(repoConfig.getDefaultProduct().getDefaultMilestone())));
		newTask.setSeverity(repoConfig.getDefaultSeverity());
		newTask.setStatus(repoConfig.getDefaultStatus());
		newTask.setPriority(repoConfig.getDefaultPriority());
		newTask.setDescription(computeTaskDescription(event));
		newTask.setAssignee(ASSIGNEE); // FIXME Only want to set if the component has not default component.

		if (event instanceof ErrorEvent && ((ErrorEvent) event).getExceptionString() != null) {
			newTask.addComment(((ErrorEvent) event).getExceptionString());
		}

		try {
			taskService.createTask(newTask);
		} catch (Exception e) {
			LOGGER.warn("Could not create task [" + taskTitle + "] reason: " + e.getMessage());
		}

	}

	private Task findOpenTaskWithTitle(TaskService taskService, String taskTitle) {
		// REVIEW could cache this crit.
		RepositoryConfiguration repoConfig = taskService.getRepositoryContext();
		NaryCriteria openCrit = new NaryCriteria(Operator.OR);
		for (TaskStatus stat : repoConfig.getStatuses()) {
			if (stat.isOpen()) {
				openCrit.addSubCriteria(new ColumnCriteria(TaskFieldConstants.STATUS_FIELD, stat.getValue()));
			}
		}

		Criteria crit = new CriteriaBuilder().column(TaskFieldConstants.SUMMARY_FIELD, taskTitle).and(openCrit)
				.toCriteria();

		QueryResult<Task> result = taskService
				.findTasksWithCriteria(crit, new QuerySpec(new Region(0, 2), null, false));
		if (result.getTotalResultSize() == 0) {
			return null;
		} else if (result.getTotalResultSize() > 1) {
			LOGGER.info("Found multiple open tasks with title [" + taskTitle + "], arbitrarily choosing first one.");
		}

		return result.getResultPage().get(0);
	}

	private String computeTaskTitle(MonitoringEvent event) {
		if (event instanceof SlowResponseEvent) {
			return "[Spring Insight] Slow response detected: " + event.getEventLabel();
		} else if (event instanceof ErrorEvent) {
			return "[Spring Insight] Error durring: " + event.getEventLabel();
		}
		throw new IllegalStateException("Unsupported monitoring event");
	}

	private String computeTaskDescription(MonitoringEvent event) {
		if (event instanceof SlowResponseEvent) {
			return descriptionOrComment((SlowResponseEvent) event);
		} else if (event instanceof ErrorEvent) {
			return ((ErrorEvent) event).getEventDescription();
		}
		throw new IllegalStateException("Unsupported monitoring event");
	}

	private String descriptionOrComment(SlowResponseEvent event) {
		return "Detected slow response. " + event.getEventLabel() + " took " + event.getDurationInMilliseconds()
				+ "ms.\n" + "Spring Insight context captured:\n" + ((SlowResponseEvent) event).getTraceString();
	}

	private String computeTaskComment(MonitoringEvent event) {
		if (event instanceof SlowResponseEvent) {
			return descriptionOrComment((SlowResponseEvent) event);
		} else if (event instanceof ErrorEvent) {
			ErrorEvent errorEvent = (ErrorEvent) event;
			return event.getEventDescription() + errorEvent.getExceptionString() == null ? "" : ("\n" + errorEvent
					.getExceptionString());
		}
		throw new IllegalStateException("Unsupported monitoring event");
	}

	private TaskService getTaskService(String projectId) {
		return taskServiceProvider.getTaskService(projectId);
	}

	public void setTaskServiceProvider(TaskServiceProvider taskServiceProvider) {
		this.taskServiceProvider = taskServiceProvider;
	}

}
