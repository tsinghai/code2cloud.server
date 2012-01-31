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
package com.tasktop.c2c.server.tasks.client.widgets.tasklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * all known task columns
 */
public class TaskColumns {

	private final List<TaskColumnDescriptor> columns = new ArrayList<TaskColumnDescriptor>();

	private final List<TaskColumnDescriptor> requiredColumns = new ArrayList<TaskColumnDescriptor>();

	private final List<TaskColumnDescriptor> defaultColumns = new ArrayList<TaskColumnDescriptor>();

	private final Map<String, TaskColumnDescriptor> columnByLabel = new HashMap<String, TaskColumnDescriptor>();
	private final Map<String, TaskColumnDescriptor> columnByTaskField = new HashMap<String, TaskColumnDescriptor>();

	private static TaskColumns instance = new TaskColumns();

	public static TaskColumns getInstance() {
		return instance;
	}

	private TaskColumns() {
		addColumn(new PriorityColumn(), false, true);
		addColumn(new SeverityColumn(), false, true);
		addColumn(new TaskIdColumn(), true, true);
		addColumn(new SummaryColumn(), true, true);
		addColumn(new StatusColumn(), false, true);
		addColumn(new OwnerColumn(), false, true);
		addColumn(new CreatedColumn(), false, true);

		addColumn(new ModifiedColumn(), false, false);
		addColumn(new ComponentColumn(), false, false);
	}

	private void addColumn(TaskColumnDescriptor column, boolean required, boolean displayDefault) {
		columns.add(column);
		if (required) {
			requiredColumns.add(column);
		}
		if (displayDefault) {
			defaultColumns.add(column);
		}
		if (column.getLabel() != null) {
			columnByLabel.put(column.getLabel(), column);
		}
		if (column.getTaskField() != null) {
			columnByTaskField.put(column.getTaskField(), column);
		}
	}

	public List<TaskColumnDescriptor> getColumns() {
		return columns;
	}

	public List<TaskColumnDescriptor> getDefaultColumns() {
		return defaultColumns;
	}

	public List<TaskColumnDescriptor> getRequiredColumns() {
		return requiredColumns;
	}

	public TaskColumnDescriptor getColumnByLabel(String label) {
		return columnByLabel.get(label);
	}

	public TaskColumnDescriptor getColumnByTaskField(String taskField) {
		return columnByTaskField.get(taskField);
	}
}
