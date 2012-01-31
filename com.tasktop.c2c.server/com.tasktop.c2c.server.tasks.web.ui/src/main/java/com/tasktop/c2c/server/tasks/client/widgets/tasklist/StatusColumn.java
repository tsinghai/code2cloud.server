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
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public class StatusColumn extends TaskColumnDescriptor {

	static interface Template extends SafeHtmlTemplates {
		@Template("{0}")
		SafeHtml status(String status);

		@Template("{0}<br>{1}")
		SafeHtml statusAndResolution(String status, String resolution);

	}

	private static Template template = GWT.create(Template.class);

	public static final String LABEL = "Status";

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getTaskField() {
		return TaskFieldConstants.STATUS_FIELD;
	}

	@Override
	protected Column<Task, ?> createColumn() {
		AbstractCell<Task> cell = new AbstractCell<Task>() {

			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, Task task, SafeHtmlBuilder sb) {
				if (task.getStatus().isOpen() || task.getResolution() == null
						|| task.getResolution().getValue().isEmpty()) {
					sb.append(template.status(stringValue(task.getStatus())));
				} else {
					sb.append(template.statusAndResolution(stringValue(task.getStatus()),
							stringValue(task.getResolution())));
				}

			}
		};
		return new IdentityColumn<Task>(cell);
	}

}
