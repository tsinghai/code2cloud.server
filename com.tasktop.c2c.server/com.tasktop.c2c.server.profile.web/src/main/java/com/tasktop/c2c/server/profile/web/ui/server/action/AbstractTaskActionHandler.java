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

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.springframework.beans.factory.annotation.Autowired;

import com.tasktop.c2c.server.common.web.server.AbstractActionHandler;
import com.tasktop.c2c.server.profile.service.provider.TaskServiceProvider;
import com.tasktop.c2c.server.tasks.service.TaskService;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractTaskActionHandler<A extends Action<R>, R extends Result> extends
		AbstractActionHandler<A, R> {

	@Autowired
	protected TaskServiceProvider taskServiceProvider;

	public AbstractTaskActionHandler() {
		super();
	}

	protected TaskService getService(String projectIdentifier) {
		return taskServiceProvider.getTaskService(projectIdentifier);
	}

}
