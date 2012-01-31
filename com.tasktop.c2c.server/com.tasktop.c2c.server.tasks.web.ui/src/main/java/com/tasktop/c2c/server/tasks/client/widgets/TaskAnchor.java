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
package com.tasktop.c2c.server.tasks.client.widgets;


import com.google.gwt.user.client.ui.Anchor;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * 
 * @author Ryan Slobojan (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 */
public class TaskAnchor extends Anchor {

	private String projectIdentifier = null;
	private Integer taskId = -1;
	private Task task = null;

	private TaskAnchor() {
	}

	public TaskAnchor(Task task) {
		// If our task URL doesn't contain a hash, then it's a relative URL so add one now.
		super(String.valueOf(task.getId()), task.getUrl().contains("#") ? task.getUrl() : "#" + task.getUrl());
		this.task = task;
	}

	public TaskAnchor(String projectIdentifier, Integer taskId, String text, String href) {
		super(text, href);
		this.taskId = taskId;
		this.projectIdentifier = projectIdentifier;
	}

	public String getProjectIdentifier() {
		return projectIdentifier;
	}

	public int getTaskId() {
		return taskId;
	}

	public Task getTask() {
		return task;
	}
}
