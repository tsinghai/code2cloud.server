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


import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.MultiSelectionModel;
import com.tasktop.c2c.server.common.web.client.navigation.Navigation;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.presenters.AbstractTaskPresenter;
import com.tasktop.c2c.server.tasks.domain.Task;

public class TaskListColumnConfiguration {
	/**
	 * the {@link Navigation#getNavigationParameterMap() navigation parameter name} from which this is
	 * {@link #configureFromRequest() configured}.
	 */
	public static final String REQUEST_PARAM_COLUMN = "column";

	private List<TaskColumnDescriptor> columns = new ArrayList<TaskColumnDescriptor>();

	private final CellTable<Task> table;
	private final MultiSelectionModel<Task> selectionModel;
	private final AbstractTaskPresenter presenter;
	private final Map<Column, TaskColumnDescriptor> descriptorsByColumn = new HashMap<Column, TaskColumnDescriptor>();

	public TaskListColumnConfiguration(AbstractTaskPresenter presenter, CellTable<Task> table,
			MultiSelectionModel<Task> selectionModel) {
		this.presenter = presenter;
		this.table = table;
		this.selectionModel = selectionModel;
	}

	final FieldUpdater<Task, Boolean> selectionFieldUpdater = new FieldUpdater<Task, Boolean>() {
		@Override
		public void update(int index, Task task, Boolean value) {
			selectionModel.setSelected(task, value != null && value);
		}
	};

	final FieldUpdater<Task, Object> hyperlinkFieldUpdater = new FieldUpdater<Task, Object>() {
		@Override
		public void update(int index, Task task, Object value) {
			ProjectTaskPlace.createPlace(presenter.getProjectIdentifier(), task.getId()).go();
		}
	};

	FieldUpdater<Task, Object> parentHyperlinkFieldUpdater;

	public void apply() {
		descriptorsByColumn.clear();
		for (int i = 0; i < table.getColumnCount(); i++) {
			// Loop through and nuke any existing columns.
			table.removeColumn(0);
		}

		// add required columns that are not configured
		for (TaskColumnDescriptor columnDescriptor : TaskColumns.getInstance().getRequiredColumns()) {
			if (!columns.contains(columnDescriptor)) {
				addColumn(columnDescriptor);
			}
		}

		// add configured columns
		for (TaskColumnDescriptor columnDescriptor : columns) {
			addColumn(columnDescriptor);
		}
	}

	/**
	 * @param columnDescriptor
	 */
	private void addColumn(TaskColumnDescriptor columnDescriptor) {
		Column<Task, ?> column = columnDescriptor.getColumn();
		column.setSortable(columnDescriptor.isSortable());
		descriptorsByColumn.put(column, columnDescriptor);
		table.addColumn(column, columnDescriptor.getHeader());
		if (columnDescriptor.getColumnWidth() != null) {
			table.setColumnWidth(null, columnDescriptor.getColumnWidth());
		}
	}

	public void configureDefaultColumns() {
		columns.clear();

		// no selection right now: columns.add(new SelectionColumn());
		columns.add(new PriorityColumn());
		columns.add(new SeverityColumn());
		columns.add(new TaskIdColumn());
		columns.add(new SummaryColumn());
		columns.add(new ComponentColumn());
		columns.add(new StatusColumn());
		columns.add(new OwnerColumn());
		columns.add(new CreatedColumn());
		columns.add(new ModifiedColumn());
	}

	public void configureFromRequest() {
		List<String> columnLabels = Navigation.getNavigationParameterMap().get(REQUEST_PARAM_COLUMN);
		if (columnLabels != null && !columnLabels.isEmpty()) {
			for (String columnLabel : columnLabels) {
				TaskColumnDescriptor column = TaskColumns.getInstance().getColumnByLabel(columnLabel);
				if (column != null) {
					columns.add(column);
				}
			}
		}
		if (columns.isEmpty()) {
			configureDefaultColumns();
		}
	}

	public List<TaskColumnDescriptor> getColumns() {
		return columns;
	}

	public void setColumns(List<TaskColumnDescriptor> columns) {
		this.columns = columns;
	}

	public MultiSelectionModel<Task> getSelectionModel() {
		return selectionModel;
	}

	public FieldUpdater<Task, Boolean> getSelectionFieldUpdater() {
		return selectionFieldUpdater;
	}

	public FieldUpdater<Task, Object> getHyperlinkFieldUpdater() {
		return hyperlinkFieldUpdater;
	}

	public FieldUpdater<Task, Object> getParentHyperlinkFieldUpdater() {
		if (parentHyperlinkFieldUpdater == null) {
			parentHyperlinkFieldUpdater = new FieldUpdater<Task, Object>() {
				@Override
				public void update(int index, Task task, Object value) {
					if (task.getParentTask() != null) {
						ProjectTaskPlace.createPlace(presenter.getProjectIdentifier(), task.getParentTask().getId())
								.go();
					}
				}
			};
		}
		return parentHyperlinkFieldUpdater;
	}

	public CellTable<Task> getTable() {
		return table;
	}

	public AbstractTaskPresenter getPresenter() {
		return presenter;
	}

	public TaskColumnDescriptor getDescriptor(Column<?, ?> column) {
		return descriptorsByColumn.get(column);
	}

	public TaskColumnDescriptor getDescriptorById(String sortField) {
		for (TaskColumnDescriptor desc : columns) {
			if (desc.getTaskField().equals(sortField)) {
				return desc;
			}
		}
		return null;
	}
}
