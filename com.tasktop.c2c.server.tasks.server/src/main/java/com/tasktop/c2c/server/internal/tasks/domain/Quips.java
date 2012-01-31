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
package com.tasktop.c2c.server.internal.tasks.domain;

// Generated May 26, 2010 11:31:55 AM by Hibernate Tools 3.3.0.GA

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

/**
 * Quips generated by hbm2java
 */
@Entity
@Table(name = "quips")
@SuppressWarnings("serial")
public class Quips implements java.io.Serializable {

	private Integer quipid;
	private Profile profiles;
	private String quip;
	private boolean approved;

	public Quips() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "quipid", unique = true, nullable = false)
	public Integer getQuipid() {
		return this.quipid;
	}

	public void setQuipid(Integer quipid) {
		this.quipid = quipid;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userid")
	public Profile getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Profile profiles) {
		this.profiles = profiles;
	}

	@Column(name = "quip", nullable = false, length = 16777215)
	public String getQuip() {
		return this.quip;
	}

	public void setQuip(String quip) {
		this.quip = quip;
	}

	@Column(name = "approved", nullable = false)
	@Convert("booleanToByte")
	public boolean getApproved() {
		return this.approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

}
