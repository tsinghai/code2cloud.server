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
package com.tasktop.c2c.server.internal.wiki.server.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "page_Id" }) })
public class Attachment extends BaseEntity {
	private Page page;

	private String name;
	private String mimeType;

	private Person originalAuthor;
	private Person lastAuthor;
	private AttachmentContent lastAttachmentContent;
	private List<AttachmentContent> attachmentContent = new ArrayList<AttachmentContent>();

	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	public Person getOriginalAuthor() {
		return originalAuthor;
	}

	public void setOriginalAuthor(Person originalAuthor) {
		this.originalAuthor = originalAuthor;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	public Person getLastAuthor() {
		return lastAuthor;
	}

	public void setLastAuthor(Person lastAuthor) {
		this.lastAuthor = lastAuthor;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	public AttachmentContent getLastAttachmentContent() {
		return lastAttachmentContent;
	}

	public void setLastAttachmentContent(AttachmentContent lastAttachmentContent) {
		this.lastAttachmentContent = lastAttachmentContent;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "attachment", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@OrderBy("creationDate")
	public List<AttachmentContent> getAttachmentContent() {
		return attachmentContent;
	}

	public void setAttachmentContent(List<AttachmentContent> attachmentContent) {
		this.attachmentContent = attachmentContent;
	}

	public void addAttachmentContent(AttachmentContent content) {
		content.setAttachment(this);

		setModificationDate(new Date());
		setLastAuthor(content.getAuthor());
		setLastAttachmentContent(content);
		getAttachmentContent().add(content);
	}
}
