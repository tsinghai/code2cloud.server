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

public class Comment extends AbstractDomainObject implements Serializable {
	private static final long serialVersionUID = 1L;

	private Date creationDate;
	private String commentText;
	private TaskUserProfile author;
	private CommentType commentType;
	private String extraData;
	private String wikiRenderedText;

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

	public TaskUserProfile getAuthor() {
		return author;
	}

	public void setAuthor(TaskUserProfile author) {
		this.author = author;
	}

	public void setCommentType(CommentType commentType) {
		this.commentType = commentType;
	}

	public CommentType getCommentType() {
		return commentType;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setWikiRenderedText(String wikiRenderedText) {
		this.wikiRenderedText = wikiRenderedText;
	}

	public String getWikiRenderedText() {
		return wikiRenderedText;
	}
}
