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
package com.tasktop.c2c.server.tasks.service;

import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;

public class SaveAttachmentArguments {
	private TaskHandle taskHandle;
	private Attachment attachment;
	private Comment comment;

	public SaveAttachmentArguments() {
		// Default constructor, does nothing.
	}

	public SaveAttachmentArguments(TaskHandle taskHandle, Attachment attachment) {
		setAttachment(attachment);
		setTaskHandle(taskHandle);
	}

	public SaveAttachmentArguments(TaskHandle taskHandle, Attachment attachment, Comment comment) {
		this(taskHandle, attachment);
		setComment(comment);
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public TaskHandle getTaskHandle() {
		return taskHandle;
	}

	public void setTaskHandle(TaskHandle taskHandle) {
		this.taskHandle = taskHandle;
	}
}
