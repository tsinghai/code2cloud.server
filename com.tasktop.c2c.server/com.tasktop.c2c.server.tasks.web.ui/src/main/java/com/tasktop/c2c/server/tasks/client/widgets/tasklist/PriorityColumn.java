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


import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.tasktop.c2c.server.tasks.client.widgets.PriorityCell;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public class PriorityColumn extends TaskColumnDescriptor {

	private static final String LABEL = "Priority";

	private static Template template = GWT.create(Template.class);

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getTaskField() {
		return TaskFieldConstants.PRIORITY_FIELD;
	}

	static interface Template extends SafeHtmlTemplates {
		@Template("<div class=\"priority-head\" title=\"Priority\">Pri</div>")
		SafeHtml header();
	}

	@Override
	protected Column<Task, ?> createColumn() {
		return new IdentityColumn<Task>(new PriorityCell());
	}

	public Header<?> getHeader() {
		return new SafeHtmlHeader(template.header());
	}
}
