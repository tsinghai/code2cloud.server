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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Task;

public class NewTaskView extends AbstractEditTaskView implements NewTaskDisplay {

	interface NewTaskViewUiBinder extends UiBinder<Widget, NewTaskView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Task, NewTaskView> {
	}

	private static NewTaskViewUiBinder uiBinder = GWT.create(NewTaskViewUiBinder.class);

	private Driver driver = GWT.create(Driver.class);

	@UiField
	@Ignore
	protected Label newTaskHeader;

	public NewTaskView() {
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
		statusEditor.setNewTask(true);
	}

	@Override
	protected void setTask(Task task) {
		super.setTask(task);
		driver.edit(task);
		if (task.getSubTasks() != null && !task.getSubTasks().isEmpty()) {
			Task subtask = task.getSubTasks().get(0);
			newTaskHeader.setText("New Parent Task of " + subtask.getId());
		} else if (task.getParentTask() != null) {
			newTaskHeader.setText("New Subtask of " + task.getParentTask().getId());
		} else {
			newTaskHeader.setText("New Task");
		}

	}

	@Override
	protected void populateTask(Task task) {
		super.populateTask(task);
		driver.flush();
	}

	@Override
	protected boolean areEditorsDirty() {
		return super.areEditorsDirty() || driver.isDirty();
	}

	@Override
	protected List<FieldDescriptor> getRelevantCustomFields(List<FieldDescriptor> fieldDescriptors) {
		ArrayList<FieldDescriptor> customFieldsForNewTask = new ArrayList<FieldDescriptor>(fieldDescriptors.size());
		for (FieldDescriptor field : fieldDescriptors) {
			if (field.isAvailableForNewTasks() && !field.isObsolete()) {
				customFieldsForNewTask.add(field);
			}
		}
		return customFieldsForNewTask;
	}
}
