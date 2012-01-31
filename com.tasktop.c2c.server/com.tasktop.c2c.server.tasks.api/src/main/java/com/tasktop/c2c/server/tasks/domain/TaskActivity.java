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
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class TaskActivity implements Serializable {

	public enum Type {
		CREATED("Created"), COMMENTED("Commented on"), LOGGED_TIME("Logged time on"), ATTACHED("Attached to"), UPDATED(
				"Updated");

		private final String prettyName;

		Type(String prettyName) {
			this.prettyName = prettyName;
		}

		public String getPrettyName() {
			return prettyName;
		}
	}

	public static class FieldUpdate implements Serializable {
		private String fieldName;
		private String fieldDescription;
		private String oldValue;
		private String newValue;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getFieldDescription() {
			return fieldDescription;
		}

		public void setFieldDescription(String fieldDescription) {
			this.fieldDescription = fieldDescription;
		}

		public String getOldValue() {
			return oldValue;
		}

		public void setOldValue(String oldValue) {
			this.oldValue = oldValue;
		}

		public String getNewValue() {
			return newValue;
		}

		public void setNewValue(String newValue) {
			this.newValue = newValue;
		}

	}

	private Date activityDate;
	private Task task;
	private TaskUserProfile author;
	private Type activityType;
	/** In case of COMMENTED. */
	private Comment comment;
	/** In case of WORK_LOG. */
	private WorkLog workLog;

	/** In case of ATTACHED. */
	private Attachment attachment;
	/** In case of UPDATED */
	private List<FieldUpdate> fieldUpdates;
	private String description;

	public TaskActivity() {
		// nothing
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public void setActivityType(Type activityType) {
		this.activityType = activityType;
	}

	public Type getActivityType() {
		return activityType;
	}

	public TaskUserProfile getAuthor() {
		return author;
	}

	public void setAuthor(TaskUserProfile author) {
		this.author = author;
	}

	public Date getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}

	public List<FieldUpdate> getFieldUpdates() {
		return fieldUpdates;
	}

	public void setFieldUpdates(List<FieldUpdate> fieldUpdates) {
		this.fieldUpdates = fieldUpdates;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * @return the workLog
	 */
	public WorkLog getWorkLog() {
		return workLog;
	}

	/**
	 * @param workLog
	 *            the workLog to set
	 */
	public void setWorkLog(WorkLog workLog) {
		this.workLog = workLog;
	}

}
