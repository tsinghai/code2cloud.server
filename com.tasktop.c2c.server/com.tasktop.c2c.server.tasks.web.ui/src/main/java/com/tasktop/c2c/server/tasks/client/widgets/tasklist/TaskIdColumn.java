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


import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.tasktop.c2c.server.tasks.client.widgets.AbstractHoverCell;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public class TaskIdColumn extends TaskColumnDescriptor {

	private static final String LABEL = "ID";

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getTaskField() {
		return TaskFieldConstants.TASK_ID_FIELD;
	}

	@Override
	protected Column<Task, ?> createColumn() {

		TaskHyperlinkCell cell = new TaskHyperlinkCell();
		cell.setHoverListener(TaskDetailsShowingHoverListener.getInstance());

		return new Column<Task, Task>(cell) {

			@Override
			public Task getValue(Task task) {
				return task;
			}
		};
	}

	public static class TaskHyperlinkCell extends AbstractHoverCell<Task> {

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, Task value, SafeHtmlBuilder sb) {
			String urlString = value.getUrl().substring(value.getUrl().indexOf("#"));
			sb.append(SafeHtmlUtils.fromTrustedString("<a href=\"" + urlString + "\">" + value.getId() + "</a>"));
		}

	}

}
