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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.tasktop.c2c.server.wiki.domain.Page.GroupAccess;

@Entity
public class Page extends BaseEntity {

	public static final String DEFAULT_MARKUP_LANGUAGE = "Textile";

	private String path;
	private String markupLanguage = DEFAULT_MARKUP_LANGUAGE;

	private Person originalAuthor;
	private Person lastAuthor;
	private PageContent lastPageContent;
	private List<PageContent> pageContent = new ArrayList<PageContent>();
	private List<Attachment> attachments = new ArrayList<Attachment>();
	private Boolean deleted = false;
	private GroupAccess editAccess = GroupAccess.ALL;
	private GroupAccess deleteAccess = GroupAccess.ALL;

	@Basic(optional = false)
	@Column(length = 255, nullable = false, unique = true)
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Basic(optional = false)
	@Column(length = 32, nullable = false)
	public String getMarkupLanguage() {
		return markupLanguage;
	}

	public void setMarkupLanguage(String markupLanguage) {
		this.markupLanguage = markupLanguage;
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
	public PageContent getLastPageContent() {
		return lastPageContent;
	}

	public void setLastPageContent(PageContent lastPageContent) {
		this.lastPageContent = lastPageContent;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "page", cascade = CascadeType.PERSIST)
	@OrderBy("creationDate")
	public List<PageContent> getPageContent() {
		return pageContent;
	}

	public void setPageContent(List<PageContent> content) {
		this.pageContent = content;
	}

	public void addPageContent(PageContent content) {
		content.setPage(this);

		setLastAuthor(content.getAuthor());
		setLastPageContent(content);
		getPageContent().add(content);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "page", cascade = CascadeType.PERSIST)
	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	@Basic(optional = false)
	@Column(nullable = false)
	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(length = 32, nullable = false)
	/**
	 * @return the editAccess
	 */
	public GroupAccess getEditAccess() {
		return editAccess;
	}

	/**
	 * @param editAccess
	 *            the editAccess to set
	 */
	public void setEditAccess(GroupAccess editAccess) {
		this.editAccess = editAccess;
	}

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(length = 32, nullable = false)
	/**
	 * @return the deleteAccess
	 */
	public GroupAccess getDeleteAccess() {
		return deleteAccess;
	}

	/**
	 * @param deleteAccess
	 *            the deleteAccess to set
	 */
	public void setDeleteAccess(GroupAccess deleteAccess) {
		this.deleteAccess = deleteAccess;
	}

}
