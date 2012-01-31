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

import javax.persistence.CascadeType;
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

import org.eclipse.persistence.annotations.Convert;

/**
 * Fielddefs generated by hbm2java
 */
@Entity
@Table(name = "fielddefs", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@SuppressWarnings("serial")
public class Fielddef implements java.io.Serializable {

	private Integer id;
	private Fielddef fielddefsByVisibilityFieldId;
	private Fielddef fielddefsByValueFieldId;
	private String name;
	private short type;
	private boolean custom;
	private String description;
	private boolean mailhead;
	private short sortkey;
	private boolean obsolete;
	private boolean enterBug;
	private boolean buglist;
	private Short visibilityValueId;
	private Set<TaskActivity> bugsActivities = new HashSet<TaskActivity>(0);
	private Set<Fielddef> fielddefsesForVisibilityFieldId = new HashSet<Fielddef>(0);
	private Set<Fielddef> fielddefsesForValueFieldId = new HashSet<Fielddef>(0);
	private Set<ProfileActivity> profilesActivities = new HashSet<ProfileActivity>(0);

	public Fielddef() {
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
	@JoinColumn(name = "visibility_field_id")
	public Fielddef getFielddefsByVisibilityFieldId() {
		return this.fielddefsByVisibilityFieldId;
	}

	public void setFielddefsByVisibilityFieldId(Fielddef fielddefsByVisibilityFieldId) {
		this.fielddefsByVisibilityFieldId = fielddefsByVisibilityFieldId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "value_field_id")
	public Fielddef getFielddefsByValueFieldId() {
		return this.fielddefsByValueFieldId;
	}

	public void setFielddefsByValueFieldId(Fielddef fielddefsByValueFieldId) {
		this.fielddefsByValueFieldId = fielddefsByValueFieldId;
	}

	@Column(name = "name", unique = true, nullable = false, length = 64)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "type", nullable = false)
	public short getType() {
		return this.type;
	}

	public void setType(short type) {
		this.type = type;
	}

	@Column(name = "custom", nullable = false)
	@Convert("booleanToByte")
	public boolean getCustom() {
		return this.custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	@Column(name = "description", nullable = false)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "mailhead", nullable = false)
	@Convert("booleanToByte")
	public boolean getMailhead() {
		return this.mailhead;
	}

	public void setMailhead(boolean mailhead) {
		this.mailhead = mailhead;
	}

	@Column(name = "sortkey", nullable = false)
	public short getSortkey() {
		return this.sortkey;
	}

	public void setSortkey(short sortkey) {
		this.sortkey = sortkey;
	}

	@Column(name = "obsolete", nullable = false)
	@Convert("booleanToByte")
	public boolean getObsolete() {
		return this.obsolete;
	}

	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	@Column(name = "enter_bug", nullable = false)
	@Convert("booleanToByte")
	public boolean getEnterBug() {
		return this.enterBug;
	}

	public void setEnterBug(boolean enterBug) {
		this.enterBug = enterBug;
	}

	@Column(name = "buglist", nullable = false)
	@Convert("booleanToByte")
	public boolean getBuglist() {
		return this.buglist;
	}

	public void setBuglist(boolean buglist) {
		this.buglist = buglist;
	}

	@Column(name = "visibility_value_id")
	public Short getVisibilityValueId() {
		return this.visibilityValueId;
	}

	public void setVisibilityValueId(Short visibilityValueId) {
		this.visibilityValueId = visibilityValueId;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fielddefs", cascade = CascadeType.REMOVE)
	public Set<TaskActivity> getBugsActivities() {
		return this.bugsActivities;
	}

	public void setBugsActivities(Set<TaskActivity> bugsActivities) {
		this.bugsActivities = bugsActivities;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fielddefsByVisibilityFieldId")
	public Set<Fielddef> getFielddefsesForVisibilityFieldId() {
		return this.fielddefsesForVisibilityFieldId;
	}

	public void setFielddefsesForVisibilityFieldId(Set<Fielddef> fielddefsesForVisibilityFieldId) {
		this.fielddefsesForVisibilityFieldId = fielddefsesForVisibilityFieldId;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fielddefsByValueFieldId")
	public Set<Fielddef> getFielddefsesForValueFieldId() {
		return this.fielddefsesForValueFieldId;
	}

	public void setFielddefsesForValueFieldId(Set<Fielddef> fielddefsesForValueFieldId) {
		this.fielddefsesForValueFieldId = fielddefsesForValueFieldId;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fielddefs")
	public Set<ProfileActivity> getProfilesActivities() {
		return this.profilesActivities;
	}

	public void setProfilesActivities(Set<ProfileActivity> profilesActivities) {
		this.profilesActivities = profilesActivities;
	}

}