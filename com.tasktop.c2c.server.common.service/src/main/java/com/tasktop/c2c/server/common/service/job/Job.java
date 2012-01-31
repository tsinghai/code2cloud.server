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
package com.tasktop.c2c.server.common.service.job;

import java.io.Serializable;

import org.springframework.context.ApplicationContext;

/**
 * An abstract job, which represents a unit of work.
 * 
 */
public abstract class Job implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Type of job. Used to partition the listeners to adjust concurrency and responsiveness. */
	public enum Type {
		DEFAULT,
		/**
		 * Sending an email job.
		 */
		EMAIL,
		/**
		 * A short-running job that should be run quickly.
		 */
		SHORT
	};

	private Type type;

	public Job() {
		this(Type.DEFAULT);
	}

	public Job(Type type) {
		this.type = type;
	}

	/**
	 * run the job within the context of an application
	 * 
	 * @param applicationContext
	 */
	public abstract void execute(ApplicationContext applicationContext);

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
