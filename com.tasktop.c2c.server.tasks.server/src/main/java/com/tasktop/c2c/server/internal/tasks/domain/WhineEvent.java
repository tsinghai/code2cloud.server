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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

/**
 * WhineEvents generated by hbm2java
 */
@Entity
@Table(name = "whine_events")
@SuppressWarnings("serial")
public class WhineEvent implements java.io.Serializable {

	private Integer id;
	private Profile profiles;
	private String subject;
	private String body;
	private boolean mailifnobugs;
	private Set<WhineSchedule> whineScheduleses = new HashSet<WhineSchedule>(0);
	private Set<WhineQuery> whineQuerieses = new HashSet<WhineQuery>(0);

	public WhineEvent() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_userid", nullable = false)
	public Profile getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Profile profiles) {
		this.profiles = profiles;
	}

	@Column(name = "subject", length = 128)
	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Column(name = "body", length = 16777215)
	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Column(name = "mailifnobugs", nullable = false)
	@Convert("booleanToByte")
	public boolean getMailifnobugs() {
		return this.mailifnobugs;
	}

	public void setMailifnobugs(boolean mailifnobugs) {
		this.mailifnobugs = mailifnobugs;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "whineEvents")
	public Set<WhineSchedule> getWhineScheduleses() {
		return this.whineScheduleses;
	}

	public void setWhineScheduleses(Set<WhineSchedule> whineScheduleses) {
		this.whineScheduleses = whineScheduleses;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "whineEvents")
	public Set<WhineQuery> getWhineQuerieses() {
		return this.whineQuerieses;
	}

	public void setWhineQuerieses(Set<WhineQuery> whineQuerieses) {
		this.whineQuerieses = whineQuerieses;
	}

}
