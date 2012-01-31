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
package com.tasktop.c2c.server.cloud.domain;

import java.net.URI;

public class Task {

	public enum Status {
		COMPLETE(true), RUNNING(false), QUEUED(false), ERROR(true), CANCELLED(true), UNKNOWN(true);

		private final boolean done;

		private Status(boolean done) {
			this.done = done;
		}

		/**
		 * indicate if the task is done, regardless of success or not. A task that is not done is either queued or
		 * running.
		 */
		public boolean isDone() {
			return done;
		}
	}

	private URI uri;
	private Status status;
	private String name;
	private String operation;

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Task [name=" + name + ", status=" + status + ", operation=" + operation + ", uri=" + uri + "]";
	}

}
