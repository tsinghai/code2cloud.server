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

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * ProfileSettingId generated by hbm2java
 */
@Embeddable
@SuppressWarnings("serial")
public class ProfileSettingId implements java.io.Serializable {

	private int userId;
	private String settingName;
	private String settingValue;

	public ProfileSettingId() {
	}

	public ProfileSettingId(int userId, String settingName, String settingValue) {
		this.userId = userId;
		this.settingName = settingName;
		this.settingValue = settingValue;
	}

	@Column(name = "user_id", nullable = false)
	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Column(name = "setting_name", nullable = false, length = 32)
	public String getSettingName() {
		return this.settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	@Column(name = "setting_value", nullable = false, length = 32)
	public String getSettingValue() {
		return this.settingValue;
	}

	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof ProfileSettingId))
			return false;
		ProfileSettingId castOther = (ProfileSettingId) other;

		return (this.getUserId() == castOther.getUserId())
				&& ((this.getSettingName() == castOther.getSettingName()) || (this.getSettingName() != null
						&& castOther.getSettingName() != null && this.getSettingName().equals(
						castOther.getSettingName())))
				&& ((this.getSettingValue() == castOther.getSettingValue()) || (this.getSettingValue() != null
						&& castOther.getSettingValue() != null && this.getSettingValue().equals(
						castOther.getSettingValue())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getUserId();
		result = 37 * result + (getSettingName() == null ? 0 : this.getSettingName().hashCode());
		result = 37 * result + (getSettingValue() == null ? 0 : this.getSettingValue().hashCode());
		return result;
	}

}