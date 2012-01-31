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
package com.tasktop.c2c.server.profile.domain.activity;

@SuppressWarnings("serial")
public class TaskActivity extends ProjectActivity {

	private com.tasktop.c2c.server.tasks.domain.TaskActivity activity;

	public TaskActivity() {
		// nothing
	}

	public TaskActivity(com.tasktop.c2c.server.tasks.domain.TaskActivity activity) {
		this.activity = activity;
		super.setActivityDate(activity.getActivityDate());
	}

	public com.tasktop.c2c.server.tasks.domain.TaskActivity getActivity() {
		return activity;
	}

	public void setActivity(com.tasktop.c2c.server.tasks.domain.TaskActivity activity) {
		this.activity = activity;
	}

}