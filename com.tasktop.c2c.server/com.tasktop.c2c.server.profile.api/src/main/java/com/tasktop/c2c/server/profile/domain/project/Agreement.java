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
package com.tasktop.c2c.server.profile.domain.project;

import java.util.Date;
import java.util.List;

/**
 * A legal agreement.
 * 
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@SuppressWarnings("serial")
public class Agreement extends AbstractEntity {

	private String title;
	private String text;
	private Integer rank;
	private Date creationDate;
	private List<AgreementProfile> agreementProfiles;

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Integer getRank() {
		return rank;
	}

	public void setCreationDate(Date dateCreated) {
		this.creationDate = dateCreated;
	}

	public Date getDateCreated() {
		return creationDate;
	}

	public void setAgreementProfiles(List<AgreementProfile> agreementProfiles) {
		this.agreementProfiles = agreementProfiles;
	}

	public List<AgreementProfile> getAgreementProfiles() {
		return agreementProfiles;
	}
}
