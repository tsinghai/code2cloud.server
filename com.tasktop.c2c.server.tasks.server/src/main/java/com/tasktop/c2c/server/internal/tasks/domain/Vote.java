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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Votes generated by hbm2java
 */
@Entity
@Table(name = "votes")
@SuppressWarnings("serial")
public class Vote implements java.io.Serializable {

	private VoteId id;
	private Task bugs;
	private Profile profiles;

	public Vote() {
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "who", column = @Column(name = "who", nullable = false)),
			@AttributeOverride(name = "bugId", column = @Column(name = "bug_id", nullable = false)),
			@AttributeOverride(name = "voteCount", column = @Column(name = "vote_count", nullable = false)) })
	public VoteId getId() {
		return this.id;
	}

	public void setId(VoteId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bug_id", nullable = false, insertable = false, updatable = false)
	public Task getBugs() {
		return this.bugs;
	}

	public void setBugs(Task bugs) {
		this.bugs = bugs;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "who", nullable = false, insertable = false, updatable = false)
	public Profile getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Profile profiles) {
		this.profiles = profiles;
	}

}
