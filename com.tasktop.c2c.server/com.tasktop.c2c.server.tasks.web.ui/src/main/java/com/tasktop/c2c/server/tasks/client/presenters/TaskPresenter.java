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
package com.tasktop.c2c.server.tasks.client.presenters;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

/**
 * @author jhickey (Tasktop Technologies Inc.)
 * 
 */
public interface TaskPresenter {

	void setPlace(Place place);

	void postComment(String comment);

	void saveStatus(TaskStatus status, TaskResolution resolution, Integer duplicateId);

	void saveOwner(TaskUserProfile owner);

	void savePriority(Priority priority);

	void saveSeverity(TaskSeverity severity);

	void saveEstimate(BigDecimal estimate);

	void saveIteration(Iteration iteration);

	void saveCC(List<TaskUserProfile> cc);

	void saveTags(List<Keyword> tags);

	void saveWorkLog(WorkLog value);

	void saveDueDate(Date value);

	void saveDescription(String value);

	void saveProduct(Product value);

	void saveComponent(Component value);

	void saveRelease(Milestone value);

	void saveFoundInRelease(String value);

	void saveSubTasks(List<Task> value);

	void saveBlocks(List<Task> value);

	void saveShortDescription(String text);

	void saveCustomField(String name, String value);

	void saveTaskType(String value);
}
