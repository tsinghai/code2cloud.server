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
package com.tasktop.c2c.server.profile.domain;

import java.io.Serializable;

/**
 * A representation of an out-bound email for use in email jobs.
 * 
 * @See com.tasktop.c2c.server.common.service.job.Job
 * 
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@SuppressWarnings("serial")
public class Email implements Serializable {

	private String to;
	private String subject;
	private String body;
	private String mimeType;

	public Email(String to, String subject, String body, String mimeType) {
		super();
		this.to = to;
		this.subject = subject;
		this.body = body;
		this.mimeType = mimeType;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getTo() {
		return to;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

}
