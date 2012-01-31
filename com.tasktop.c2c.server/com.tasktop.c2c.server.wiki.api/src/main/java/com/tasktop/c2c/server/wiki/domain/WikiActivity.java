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

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class WikiActivity implements Serializable {
	public enum Type {
		CREATED("Created"), UPDATED("Updated"), DELETED("Deleted");

		private final String label;

		private Type(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	private Date activityDate;
	private Type activityType;
	private Person author;
	private Page page;

	public Type getActivityType() {
		return activityType;
	}

	public void setActivityType(Type activityType) {
		this.activityType = activityType;
	}

	public Date getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public void setAuthor(Person author) {
		this.author = author;
	}

	public Page getPage() {
		return page;
	}

	public Person getAuthor() {
		return author;
	}

	@Override
	public String toString() {
		return "WikiActivity [page=" + (page == null ? null : page.getPath()) + ", author=" + author + ", timestamp="
				+ activityDate + "]";
	}

}
