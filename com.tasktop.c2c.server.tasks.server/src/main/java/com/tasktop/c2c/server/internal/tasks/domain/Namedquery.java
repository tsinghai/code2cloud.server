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
import javax.persistence.UniqueConstraint;

/**
 * Namedqueries generated by hbm2java
 */
@Entity
@Table(name = "namedqueries", uniqueConstraints = @UniqueConstraint(columnNames = { "userid", "name" }))
@SuppressWarnings("serial")
public class Namedquery implements java.io.Serializable {

	private Integer id;
	private Profile profiles;
	private String name;
	private String query;
	private byte queryType;
	private Set<NamedqueryGroupMap> namedqueryGroupMaps = new HashSet<NamedqueryGroupMap>(0);
	private Set<NamedqueryLinkInFooter> namedqueriesLinkInFooters = new HashSet<NamedqueryLinkInFooter>(0);

	public Namedquery() {
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
	@JoinColumn(name = "userid", nullable = false)
	public Profile getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Profile profiles) {
		this.profiles = profiles;
	}

	@Column(name = "name", nullable = false, length = 64)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "query", nullable = false, length = 16777215)
	public String getQuery() {
		return this.query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Column(name = "query_type", nullable = false)
	public byte getQueryType() {
		return this.queryType;
	}

	public void setQueryType(byte queryType) {
		this.queryType = queryType;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "namedqueries")
	public Set<NamedqueryGroupMap> getNamedqueryGroupMaps() {
		return this.namedqueryGroupMaps;
	}

	public void setNamedqueryGroupMaps(Set<NamedqueryGroupMap> namedqueryGroupMaps) {
		this.namedqueryGroupMaps = namedqueryGroupMaps;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "namedqueries")
	public Set<NamedqueryLinkInFooter> getNamedqueriesLinkInFooters() {
		return this.namedqueriesLinkInFooters;
	}

	public void setNamedqueriesLinkInFooters(Set<NamedqueryLinkInFooter> namedqueriesLinkInFooters) {
		this.namedqueriesLinkInFooters = namedqueriesLinkInFooters;
	}

}