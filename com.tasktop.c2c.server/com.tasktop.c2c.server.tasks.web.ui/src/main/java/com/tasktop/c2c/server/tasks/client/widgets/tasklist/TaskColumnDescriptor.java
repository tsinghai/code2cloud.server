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


import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public abstract class TaskColumnDescriptor {

	private Column<Task, ?> column = null;

	public boolean isSortable() {
		return true;
	}

	/**
	 * the user-visible label
	 */
	public abstract String getLabel();

	/**
	 * the corresponding {@link TaskFieldConstants task field}
	 */
	public abstract String getTaskField();

	public final Column<Task, ?> getColumn() {
		if (column == null) {
			column = createColumn();
		}
		return column;
	}

	protected abstract Column<Task, ?> createColumn();

	public String getColumnWidth() {
		return null;
	}

	public Header<?> getHeader() {
		// return new SafeHtmlHeader(template.header(getLabel()));
		return new TextHeader(getLabel());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof TaskColumnDescriptor) {
			return ((TaskColumnDescriptor) obj).getLabel().equals(getLabel());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

}
