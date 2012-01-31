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
package com.tasktop.c2c.server.profile.domain.internal;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Agreement extends BaseEntity {
	private String title;
	private String text;
	private Integer rank;
	private Date dateCreated;
	private Boolean active;
	private List<AgreementProfile> agreementProfiles;

	/**
	 * title of agreement
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * The text of the agreement
	 */
	@Basic(optional = false)
	@Column(nullable = false, length = 5000)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * the rank order of the agreement
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	/**
	 * date agreement was created
	 */
	@Basic(optional = false)
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * active flag
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "agreement")
	public List<AgreementProfile> getAgreementProfiles() {
		return agreementProfiles;
	}

	public void setAgreementProfiles(List<AgreementProfile> agreementProfiles) {
		this.agreementProfiles = agreementProfiles;
	}
}
