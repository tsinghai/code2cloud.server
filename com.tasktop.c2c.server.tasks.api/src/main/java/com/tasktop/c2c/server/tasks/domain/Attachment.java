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

import java.util.Date;

public class Attachment extends AttachmentHandle {
	private static final long serialVersionUID = 1L;

	private String description;
	private String filename;
	private String mimeType;
	private Integer byteSize;
	private Date creationDate;
	private TaskUserProfile submitter;
	private String url;

	// This seems to be the way to serialize across the wire for json purposes.
	private byte[] attachmentData;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public TaskUserProfile getSubmitter() {
		return submitter;
	}

	public void setSubmitter(TaskUserProfile submitter) {
		this.submitter = submitter;
	}

	public byte[] getAttachmentData() {
		return attachmentData;
	}

	public void setAttachmentData(byte[] attachmentData) {
		this.attachmentData = attachmentData;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getByteSize() {
		return byteSize;
	}

	public void setByteSize(Integer byteSize) {
		this.byteSize = byteSize;
	}
}
