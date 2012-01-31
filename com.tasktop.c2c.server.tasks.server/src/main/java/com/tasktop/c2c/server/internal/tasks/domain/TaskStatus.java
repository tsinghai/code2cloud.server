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
package com.tasktop.c2c.server.internal.tasks.domain;

// Generated May 26, 2010 11:31:55 AM by Hibernate Tools 3.3.0.GA

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

/**
 * BugStatus generated by hbm2java
 */
@Entity
@Table(name = "bug_status")
@SuppressWarnings("serial")
public class TaskStatus extends AbstractReferenceEntity {

	private boolean isactive;
	private Short visibilityValueId;
	private boolean isOpen;
	private Set<StatusWorkflow> statusWorkflowsForOldStatus = new HashSet<StatusWorkflow>(0);
	private Set<StatusWorkflow> statusWorkflowsForNewStatus = new HashSet<StatusWorkflow>(0);

	public TaskStatus() {
	}

	@Column(name = "isactive", nullable = false)
	@Convert("booleanToByte")
	public boolean getIsactive() {
		return this.isactive;
	}

	public void setIsactive(boolean isactive) {
		this.isactive = isactive;
	}

	@Column(name = "visibility_value_id")
	public Short getVisibilityValueId() {
		return this.visibilityValueId;
	}

	public void setVisibilityValueId(Short visibilityValueId) {
		this.visibilityValueId = visibilityValueId;
	}

	@Column(name = "is_open", nullable = false)
	@Convert("booleanToByte")
	public boolean getIsOpen() {
		return this.isOpen;
	}

	public void setIsOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "oldStatus")
	public Set<StatusWorkflow> getStatusWorkflowsForOldStatus() {
		return this.statusWorkflowsForOldStatus;
	}

	public void setStatusWorkflowsForOldStatus(Set<StatusWorkflow> statusWorkflowsForOldStatus) {
		this.statusWorkflowsForOldStatus = statusWorkflowsForOldStatus;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "newStatus")
	public Set<StatusWorkflow> getStatusWorkflowsForNewStatus() {
		return this.statusWorkflowsForNewStatus;
	}

	public void setStatusWorkflowsForNewStatus(Set<StatusWorkflow> statusWorkflowsForNewStatus) {
		this.statusWorkflowsForNewStatus = statusWorkflowsForNewStatus;
	}

	@Override
	public String toString() {
		return getValue();
	}

}
