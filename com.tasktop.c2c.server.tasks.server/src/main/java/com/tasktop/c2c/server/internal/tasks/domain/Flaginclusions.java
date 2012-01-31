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
 * Flaginclusions generated by hbm2java
 */
@Entity
@Table(name = "flaginclusions")
@SuppressWarnings("serial")
public class Flaginclusions implements java.io.Serializable {

	private FlaginclusionsId id;
	private Flagtypes flagtypes;
	private Product products;
	private Component components;

	public Flaginclusions() {
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "typeId", column = @Column(name = "type_id", nullable = false)),
			@AttributeOverride(name = "productId", column = @Column(name = "product_id")),
			@AttributeOverride(name = "componentId", column = @Column(name = "component_id")) })
	public FlaginclusionsId getId() {
		return this.id;
	}

	public void setId(FlaginclusionsId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_id", nullable = false, insertable = false, updatable = false)
	public Flagtypes getFlagtypes() {
		return this.flagtypes;
	}

	public void setFlagtypes(Flagtypes flagtypes) {
		this.flagtypes = flagtypes;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", insertable = false, updatable = false)
	public Product getProducts() {
		return this.products;
	}

	public void setProducts(Product products) {
		this.products = products;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "component_id", insertable = false, updatable = false)
	public Component getComponents() {
		return this.components;
	}

	public void setComponents(Component components) {
		this.components = components;
	}

}
