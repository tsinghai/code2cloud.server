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


import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.tasktop.c2c.server.tasks.domain.Task;

public class PriorityCell extends AbstractCell<Task> {

	private static Template template = GWT.create(Template.class);

	public PriorityCell() {
	}

	@Override
	public void render(Context context, Task task, SafeHtmlBuilder sb) {
		Integer id = task.getPriority().getId();
		switch (id) {
		case 1:
			sb.append(template.highest());
			break;
		case 2:
			sb.append(template.high());
			break;
		case 3:
			sb.append(template.normal());
			break;
		case 4:
			sb.append(template.low());
			break;
		case 5:
			sb.append(template.lowest());
			break;
		default:
			sb.append(template.normal());
			break;
		}
	}

	static interface Template extends SafeHtmlTemplates {
		@Template("<div class=\"priority one\" title=\"Lowest\">")
		SafeHtml lowest();

		@Template("<div class=\"priority two\" title=\"Low\">")
		SafeHtml low();

		@Template("<div class=\"priority three\" title=\"Normal\">")
		SafeHtml normal();

		@Template("<div class=\"priority four\" title=\"High\">")
		SafeHtml high();

		@Template("<div class=\"priority five\" title=\"Highest\">")
		SafeHtml highest();
	}
}
