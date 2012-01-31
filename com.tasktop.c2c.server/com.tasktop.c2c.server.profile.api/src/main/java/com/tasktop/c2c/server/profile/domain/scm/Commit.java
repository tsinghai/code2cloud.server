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
package com.tasktop.c2c.server.profile.domain.scm;

import java.io.Serializable;
import java.util.Date;

import com.tasktop.c2c.server.profile.domain.project.Profile;

@SuppressWarnings("serial")
public class Commit implements Serializable {
	private String repository;
	private String number;
	private Profile author;
	private Date date;
	private String comment;

	// TODO files and lines added/removed

	public Commit() {

	}

	public Commit(String number, Profile author, Date date, String comment) {
		this.number = number;
		this.author = author;
		this.date = date;
		this.comment = comment;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public Profile getAuthor() {
		return author;
	}

	public void setAuthor(Profile author) {
		this.author = author;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getRepository() {
		return repository;
	}
}
