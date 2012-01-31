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

/**
 * BzSchema generated by hbm2java
 */
@Entity
@Table(name = "bz_schema")
@SuppressWarnings("serial")
public class BzSchema implements java.io.Serializable {

	private BzSchemaId id;

	public BzSchema() {
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "schemaData", column = @Column(name = "schema_data", nullable = false)),
			@AttributeOverride(name = "version", column = @Column(name = "version", nullable = false, precision = 3)) })
	public BzSchemaId getId() {
		return this.id;
	}

	public void setId(BzSchemaId id) {
		this.id = id;
	}

}