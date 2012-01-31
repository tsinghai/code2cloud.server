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
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class Person extends BaseEntity {

	private String name;
	private String identity;

	private List<Page> originalAuthorPages = new ArrayList<Page>();
	private List<PageContent> authorContent = new ArrayList<PageContent>();

	private List<Attachment> originalAuthorAttachments = new ArrayList<Attachment>();
	private List<AttachmentContent> authorAttachmentContent = new ArrayList<AttachmentContent>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Basic(optional = false)
	@Column(nullable = false, unique = true, length = 255)
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "originalAuthor")
	public List<Page> getOriginalAuthorPages() {
		return originalAuthorPages;
	}

	public void setOriginalAuthorPages(List<Page> originalAuthorPages) {
		this.originalAuthorPages = originalAuthorPages;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
	public List<PageContent> getAuthorContent() {
		return authorContent;
	}

	public void setAuthorContent(List<PageContent> authorContent) {
		this.authorContent = authorContent;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "originalAuthor")
	public List<Attachment> getOriginalAuthorAttachments() {
		return originalAuthorAttachments;
	}

	public void setOriginalAuthorAttachments(List<Attachment> originalAuthorAttachments) {
		this.originalAuthorAttachments = originalAuthorAttachments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
	public List<AttachmentContent> getAuthorAttachmentContent() {
		return authorAttachmentContent;
	}

	public void setAuthorAttachmentContent(List<AttachmentContent> authorAttachmentContent) {
		this.authorAttachmentContent = authorAttachmentContent;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", identity=" + identity + ", id=" + getId() + "]";
	}

}
