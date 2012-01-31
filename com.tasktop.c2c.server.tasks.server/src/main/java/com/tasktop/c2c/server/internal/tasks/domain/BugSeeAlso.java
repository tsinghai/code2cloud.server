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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * BugSeeAlso generated by hbm2java
 */
@Entity
@Table(name = "bug_see_also", uniqueConstraints = @UniqueConstraint(columnNames = { "bug_id", "value" }))
@SuppressWarnings("serial")
public class BugSeeAlso implements java.io.Serializable {

	private BugSeeAlsoId id;

	public BugSeeAlso() {
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "bugId", column = @Column(name = "bug_id", nullable = false)),
			@AttributeOverride(name = "value", column = @Column(name = "value", nullable = false)) })
	public BugSeeAlsoId getId() {
		return this.id;
	}

	public void setId(BugSeeAlsoId id) {
		this.id = id;
	}

}
