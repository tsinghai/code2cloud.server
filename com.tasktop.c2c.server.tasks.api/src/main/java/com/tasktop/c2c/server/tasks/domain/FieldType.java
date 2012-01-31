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
package com.tasktop.c2c.server.tasks.domain;

public enum FieldType {
	/**
	 * a single-line text field
	 */
	TEXT("Single line text input"),
	/**
	 * a single-select field
	 */
	SINGLE_SELECT("Single selection"),
	/**
	 * multi-select
	 */
	MULTI_SELECT("Multiple selection"),
	/**
	 * long text
	 */
	LONG_TEXT("Long text input"),
	/**
	 * for date/time
	 */
	TIMESTAMP("Time and date"),
	/**
	 * a reference to a task (ie: it's id)
	 */
	TASK_REFERENCE("Task reference"),
	/**
	 * a checkbox field
	 */
	CHECKBOX("Checkbox");

	private String friendlyName;

	private FieldType(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * @return the friendlyName
	 */
	public String getFriendlyName() {
		return friendlyName;
	}
}
