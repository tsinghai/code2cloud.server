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


import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface TaskResources extends ClientBundle {
	@Source("resources/sub-task-new.gif")
	ImageResource newSubtaskIcon();

	@Source("resources/pencil.png")
	ImageResource pencilIcon();

	@Source("css/task-new.css")
	TaskCssResources style();

	public static TaskResources resources = GWT.create(TaskResources.class);

}
