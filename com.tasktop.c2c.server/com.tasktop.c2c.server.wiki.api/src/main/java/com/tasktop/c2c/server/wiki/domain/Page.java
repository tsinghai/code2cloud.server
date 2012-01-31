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
package com.tasktop.c2c.server.wiki.domain;

import java.util.Date;

@SuppressWarnings("serial")
public class Page extends PageHandle {

	public enum GroupAccess {
		ALL("Allow all users"), MEMBER_AND_OWNERS("Members + Owners"), OWNERS("Owners");
		private String friendlyName;

		private GroupAccess(String friendlyName) {
			this.friendlyName = friendlyName;
		}

		/**
		 * @return the friendlyName
		 */
		public String getFriendlyName() {
			return friendlyName;
		}
	}

	private String path;
	private Date creationDate;
	private Date modificationDate;
	private Person originalAuthor;
	private Person lastAuthor;
	private String url;
	private String attachmentsUrl;
	private GroupAccess editAccess;
	private GroupAccess deleteAccess;
	private boolean deleted;

	private String content;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public Person getOriginalAuthor() {
		return originalAuthor;
	}

	public void setOriginalAuthor(Person originalAuthor) {
		this.originalAuthor = originalAuthor;
	}

	public Person getLastAuthor() {
		return lastAuthor;
	}

	public void setLastAuthor(Person lastAuthor) {
		this.lastAuthor = lastAuthor;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAttachmentsUrl() {
		return attachmentsUrl;
	}

	public void setAttachmentsUrl(String attachmentsUrl) {
		this.attachmentsUrl = attachmentsUrl;
	}

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

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * This does not delete the page. Instead you must make a service call.
	 * 
	 * @param deleted
	 *            the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
