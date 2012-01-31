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

import java.io.Serializable;

@SuppressWarnings("serial")
public class StateTransition implements Serializable {
	private String initialStatus;
	private String newStatus;
	private boolean commentRequired;

	public StateTransition() {
	}

	public StateTransition(String initialStatus, String newStatus, boolean commentRequired) {
		this.initialStatus = initialStatus;
		this.newStatus = newStatus;
		this.commentRequired = commentRequired;
	}

	public String getInitialStatus() {
		return initialStatus;
	}

	public void setInitialStatus(String initialStatus) {
		this.initialStatus = initialStatus;
	}

	public String getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(String newStatus) {
		this.newStatus = newStatus;
	}

	public boolean isCommentRequired() {
		return commentRequired;
	}

	public void setCommentRequired(boolean commentRequired) {
		this.commentRequired = commentRequired;
	}

}
