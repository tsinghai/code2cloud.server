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
package com.tasktop.c2c.server.tasks.client.widgets.chooser.task;

import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * Extension of {@link TaskSuggestion} that handles suggestions for the "Duplicate" field of a Task
 * 
 * @author Jennifer Hickey
 * 
 */
public class DuplicateTaskSuggestion extends TaskSuggestion {

	/**
	 * @param task
	 */
	public DuplicateTaskSuggestion(Task task) {
		super(task);
	}

	@Override
	public String getReplacementString() {
		// SuggestBox for duplicate tasks needs replacement string
		return getValue().getId().toString();
	}
}
