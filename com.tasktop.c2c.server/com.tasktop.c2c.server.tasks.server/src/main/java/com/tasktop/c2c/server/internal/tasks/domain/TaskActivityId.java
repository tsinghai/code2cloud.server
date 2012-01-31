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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * BugsActivityId generated by hbm2java
 */
@Embeddable
@SuppressWarnings("serial")
public class TaskActivityId implements java.io.Serializable {

	private int bugId;
	private int who;
	private Date bugWhen;
	private int fieldid;
	private String added;
	private String removed;

	public TaskActivityId() {
	}

	public TaskActivityId(int bugId, int who, Date bugWhen, int fieldid) {
		this.bugId = bugId;
		this.who = who;
		this.bugWhen = bugWhen;
		this.fieldid = fieldid;
	}

	public TaskActivityId(int bugId, int who, Date bugWhen, int fieldid, String added, String removed) {
		this(bugId, who, bugWhen, fieldid);
		this.added = added;
		this.removed = removed;
	}

	@Column(name = "bug_id", nullable = false)
	public int getBugId() {
		return this.bugId;
	}

	public void setBugId(int bugId) {
		this.bugId = bugId;
	}

	@Column(name = "who", nullable = false)
	public int getWho() {
		return this.who;
	}

	public void setWho(int who) {
		this.who = who;
	}

	@Column(name = "bug_when", nullable = false, length = 19)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getBugWhen() {
		return this.bugWhen;
	}

	public void setBugWhen(Date bugWhen) {
		this.bugWhen = bugWhen;
	}

	@Column(name = "fieldid", nullable = false)
	public int getFieldid() {
		return this.fieldid;
	}

	public void setFieldid(int fieldid) {
		this.fieldid = fieldid;
	}

	@Column(name = "added")
	public String getAdded() {
		return this.added;
	}

	public void setAdded(String added) {
		this.added = added;
	}

	@Column(name = "removed")
	public String getRemoved() {
		return this.removed;
	}

	public void setRemoved(String removed) {
		this.removed = removed;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof TaskActivityId))
			return false;
		TaskActivityId castOther = (TaskActivityId) other;

		return (this.getBugId() == castOther.getBugId())
				&& (this.getWho() == castOther.getWho())
				&& ((this.getBugWhen() == castOther.getBugWhen()) || (this.getBugWhen() != null
						&& castOther.getBugWhen() != null && this.getBugWhen().equals(castOther.getBugWhen())))
				&& (this.getFieldid() == castOther.getFieldid())
				&& ((this.getAdded() == castOther.getAdded()) || (this.getAdded() != null
						&& castOther.getAdded() != null && this.getAdded().equals(castOther.getAdded())))
				&& ((this.getRemoved() == castOther.getRemoved()) || (this.getRemoved() != null
						&& castOther.getRemoved() != null && this.getRemoved().equals(castOther.getRemoved())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getBugId();
		result = 37 * result + this.getWho();
		result = 37 * result + (getBugWhen() == null ? 0 : this.getBugWhen().hashCode());
		result = 37 * result + this.getFieldid();
		result = 37 * result + (getAdded() == null ? 0 : this.getAdded().hashCode());
		result = 37 * result + (getRemoved() == null ? 0 : this.getRemoved().hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "TaskActivityId [bugId=" + bugId + ", who=" + who + ", bugWhen=" + bugWhen + ", fieldid=" + fieldid
				+ ", added=" + added + ", removed=" + removed + "]";
	}

}