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
 * GroupGroupMapId generated by hbm2java
 */
@Embeddable
@SuppressWarnings("serial")
public class GroupGroupMapId implements java.io.Serializable {

	private int memberId;
	private int grantorId;
	private byte grantType;

	public GroupGroupMapId() {
	}

	public GroupGroupMapId(int memberId, int grantorId, byte grantType) {
		this.memberId = memberId;
		this.grantorId = grantorId;
		this.grantType = grantType;
	}

	@Column(name = "member_id", nullable = false)
	public int getMemberId() {
		return this.memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	@Column(name = "grantor_id", nullable = false)
	public int getGrantorId() {
		return this.grantorId;
	}

	public void setGrantorId(int grantorId) {
		this.grantorId = grantorId;
	}

	@Column(name = "grant_type", nullable = false)
	public byte getGrantType() {
		return this.grantType;
	}

	public void setGrantType(byte grantType) {
		this.grantType = grantType;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof GroupGroupMapId))
			return false;
		GroupGroupMapId castOther = (GroupGroupMapId) other;

		return (this.getMemberId() == castOther.getMemberId()) && (this.getGrantorId() == castOther.getGrantorId())
				&& (this.getGrantType() == castOther.getGrantType());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getMemberId();
		result = 37 * result + this.getGrantorId();
		result = 37 * result + this.getGrantType();
		return result;
	}

}
