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
import javax.persistence.UniqueConstraint;

/**
 * ProfileSetting generated by hbm2java
 */
@Entity
@Table(name = "profile_setting", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "setting_name" }))
@SuppressWarnings("serial")
public class ProfileSetting implements java.io.Serializable {

	private ProfileSettingId id;
	private Profile profiles;
	private Setting setting;

	public ProfileSetting() {
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false)),
			@AttributeOverride(name = "settingName", column = @Column(name = "setting_name", nullable = false, length = 32)),
			@AttributeOverride(name = "settingValue", column = @Column(name = "setting_value", nullable = false, length = 32)) })
	public ProfileSettingId getId() {
		return this.id;
	}

	public void setId(ProfileSettingId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
	public Profile getProfiles() {
		return this.profiles;
	}

	public void setProfiles(Profile profiles) {
		this.profiles = profiles;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "setting_name", nullable = false, insertable = false, updatable = false)
	public Setting getSetting() {
		return this.setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}

}
