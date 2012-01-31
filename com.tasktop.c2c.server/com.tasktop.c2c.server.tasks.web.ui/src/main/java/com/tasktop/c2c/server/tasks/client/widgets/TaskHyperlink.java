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


import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.Hyperlink;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class TaskHyperlink extends Hyperlink {

	private final String projectIdentity;
	private final String taskId;

	public TaskHyperlink(String projectIdentity, String taskId, String linkText, int offset, int length) {
		super(linkText, offset, length);
		this.projectIdentity = projectIdentity;
		this.taskId = taskId;
	}

	@Override
	public void open() {
		ProjectTaskPlace.createPlace(projectIdentity, Integer.valueOf(taskId)).go();
	}

}
