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

import static com.tasktop.c2c.server.common.web.client.widgets.Format.stringValue;


import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public class SummaryColumn extends TaskColumnDescriptor {

	private static final String LABEL = "Summary";

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getTaskField() {
		return TaskFieldConstants.SUMMARY_FIELD;
	}

	@Override
	protected Column<Task, ?> createColumn() {
		TaskHyperlinkCell cell = new TaskHyperlinkCell();
		return new Column<Task, Task>(cell) {
			@Override
			public Task getValue(Task task) {
				return task;
			}
		};
	}

	@Override
	public String getColumnWidth() {
		return "197px";
	}

	public static class TaskHyperlinkCell extends AbstractCell<Task> {

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, Task value, SafeHtmlBuilder sb) {
			String urlString = value.getUrl().substring(value.getUrl().indexOf("#"));
			sb.append(SafeHtmlUtils.fromTrustedString("<a href=\"" + urlString + "\">"));
			sb.appendEscaped(stringValue(value.getShortDescription()));
			sb.append(SafeHtmlUtils.fromTrustedString("</a>"));
		}
	}
}
