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

/**
 * A minimal representaiton of a task with enough information needed to retrieve a task, as well as check for concurrent
 * updates.
 * 
 */
@SuppressWarnings("serial")
public class TaskHandle extends AbstractDomainObject {
	private String version;

	public TaskHandle() {
		// nothing
	}

	public TaskHandle(Integer id, String version) {
		super.setId(id);
		this.version = version;
	}

	/**
	 * Get get version info used for concurrent update checks. This is intended to be an opaque object.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
